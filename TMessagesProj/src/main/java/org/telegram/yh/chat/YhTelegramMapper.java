package org.telegram.yh.chat;

import android.os.Bundle;
import android.text.TextUtils;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;
import org.telegram.yh.YhApiFacade;
import org.telegram.yh.YhChatType;
import org.telegram.yh.YhImageUrlHelper;
import org.telegram.yh.YhMediaRegistry;
import org.telegram.yh.YhPeerRegistry;
import org.telegram.yh.YhSession;
import org.telegram.yh.dialogs.YhDialogItem;
import org.telegram.yh.model.YhBotDetail;
import org.telegram.yh.model.YhGroupInfo;
import org.telegram.yh.model.YhGroupMember;
import org.telegram.yh.model.YhSelfUser;
import org.telegram.yh.model.YhUserDetail;

import java.text.SimpleDateFormat;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class YhTelegramMapper {

    public static final String ARG_YH_CHAT = "yh_chat";
    public static final String ARG_YH_CHAT_ID = "yh_chat_id";
    public static final String ARG_YH_CHAT_TYPE = "yh_chat_type";
    public static final String ARG_YH_CHAT_TITLE = "yh_chat_title";
    public static final String ARG_YH_CHAT_AVATAR_URL = "yh_chat_avatar_url";
    public static final String PARAM_YH_MESSAGE_ID = "yh_message_id";

    private static final long USER_BASE = 1_800_000_000L;
    private static final long CHAT_BASE = 1_600_000_000L;
    private static final long BOT_BASE = 1_900_000_000L;

    private YhTelegramMapper() {
    }

    public static Bundle buildChatArguments(int account, YhDialogItem dialog) {
        MessagesController messagesController = MessagesController.getInstance(account);
        Bundle args = new Bundle();
        args.putBoolean(ARG_YH_CHAT, true);
        args.putString(ARG_YH_CHAT_ID, dialog.getChatId());
        args.putInt(ARG_YH_CHAT_TYPE, dialog.getChatType());
        args.putString(ARG_YH_CHAT_TITLE, safeDialogTitle(dialog.getTitle()));
        args.putString(ARG_YH_CHAT_AVATAR_URL, dialog.getAvatarUrl());

        if (dialog.getChatType() == YhChatType.GROUP) {
            TLRPC.Chat chat = ensureDialogChat(dialog);
            messagesController.putChat(chat, true);
            args.putLong("chat_id", chat.id);
        } else {
            TLRPC.User user = ensureDialogUser(dialog);
            messagesController.putUser(user, true);
            args.putLong("user_id", user.id);
        }
        return args;
    }

    public static ArrayList<MessageObject> toMessageObjects(int account, String dialogChatId, int dialogChatType,
            List<YhChatMessage> yhMessages) {
        YhSession session = YhApiFacade.getInstance(account).getSession();
        String selfUserId = session != null ? session.getUserId() : null;
        MessagesController messagesController = MessagesController.getInstance(account);
        ensureSelfUser(messagesController, selfUserId);

        long dialogPeerId = syntheticPeerId(dialogChatId, dialogChatType);
        long dialogId = dialogChatType == YhChatType.GROUP ? -dialogPeerId : dialogPeerId;
        ArrayList<MessageObject> result = new ArrayList<>();
        if (yhMessages == null) {
            return result;
        }
        for (int i = 0; i < yhMessages.size(); i++) {
            YhChatMessage yhMessage = yhMessages.get(i);
            if (yhMessage == null) {
                continue;
            }
            registerSender(messagesController, yhMessage, selfUserId);
            TLRPC.Message message = buildMessage(dialogId, dialogPeerId, dialogChatType, yhMessage, selfUserId);
            result.add(new MessageObject(account, message, true, false));
        }
        return result;
    }

    public static TLRPC.User ensureSelfUser(int account, YhSelfUser yhUser) {
        YhSession session = YhApiFacade.getInstance(account).getSession();
        String selfUserId = session != null ? session.getUserId() : null;
        if (TextUtils.isEmpty(selfUserId) && (yhUser == null || TextUtils.isEmpty(yhUser.getId()))) {
            return null;
        }
        String sourceId = !TextUtils.isEmpty(selfUserId) ? selfUserId : yhUser.getId();
        TLRPC.TL_user user = new TLRPC.TL_user();
        user.id = syntheticPeerId(sourceId, YhChatType.USER);
        user.self = true;
        user.first_name = yhUser != null && !TextUtils.isEmpty(yhUser.getName()) ? yhUser.getName() : "云湖账号";
        user.last_name = "";
        user.phone = normalizePhone(yhUser != null ? yhUser.getPhone() : null);
        user.premium = yhUser != null && yhUser.isVip();
        user.status = new TLRPC.TL_userStatusRecently();
        user.photo = new TLRPC.TL_userProfilePhotoEmpty();
        YhPeerRegistry.registerPeer(user.id, sourceId, YhChatType.USER);
        YhPeerRegistry.registerAvatarUrl(user.id, yhUser != null ? YhImageUrlHelper.getDetailImageUrl(yhUser.getAvatarUrl()) : null);
        MessagesController.getInstance(account).putUser(user, true);
        return user;
    }

    public static long syntheticPeerId(String chatId, int chatType) {
        long hash = chatId == null ? 1L : positiveHash(chatId);
        if (hash == 0) {
            hash = 1L;
        }
        long suffix = hash % 100_000_000L;
        if (chatType == YhChatType.GROUP) {
            return CHAT_BASE + suffix;
        } else if (chatType == YhChatType.BOT) {
            return BOT_BASE + suffix;
        }
        return USER_BASE + suffix;
    }

    public static String getYhMessageId(MessageObject messageObject) {
        if (messageObject == null || messageObject.messageOwner == null || messageObject.messageOwner.params == null) {
            return null;
        }
        return messageObject.messageOwner.params.get(PARAM_YH_MESSAGE_ID);
    }

    public static MessageObject buildLocalOutgoingTextMessageObject(int account, String dialogChatId, int dialogChatType,
            String messageId, String text, String quoteMessageId, String quoteText) {
        YhSession session = YhApiFacade.getInstance(account).getSession();
        String selfUserId = session != null ? session.getUserId() : null;
        if (TextUtils.isEmpty(selfUserId)) {
            selfUserId = "__yh_self__";
        }

        MessagesController messagesController = MessagesController.getInstance(account);
        ensureSelfUser(messagesController, selfUserId);

        long dialogPeerId = syntheticPeerId(dialogChatId, dialogChatType);
        long dialogId = dialogChatType == YhChatType.GROUP ? -dialogPeerId : dialogPeerId;
        long now = System.currentTimeMillis();

        YhChatMessage localMessage = new YhChatMessage(
                messageId,
                0L,
                YhChatMessage.CONTENT_TYPE_TEXT,
                "right",
                now,
                0L,
                0L,
                quoteMessageId,
                emptyIfNull(text),
                quoteText,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0L,
                0L,
                selfUserId,
                YhChatType.USER,
                "我",
                null
        );

        TLRPC.Message message = buildMessage(dialogId, dialogPeerId, dialogChatType, localMessage, selfUserId);
        message.id = -Math.max(1, syntheticMessageId(localMessage));
        message.send_state = MessageObject.MESSAGE_SEND_STATE_SENDING;
        message.unread = false;
        if (message.params == null) {
            message.params = new HashMap<>();
        }
        message.params.put(PARAM_YH_MESSAGE_ID, emptyIfNull(messageId));

        MessageObject messageObject = new MessageObject(account, message, true, false);
        messageObject.wasJustSent = false;
        return messageObject;
    }

    private static void ensureSelfUser(MessagesController messagesController, String selfUserId) {
        if (TextUtils.isEmpty(selfUserId)) {
            return;
        }
        long id = syntheticPeerId(selfUserId, YhChatType.USER);
        TLRPC.User existingUser = messagesController.getUser(id);
        if (existingUser != null) {
            existingUser.self = true;
            if (TextUtils.isEmpty(existingUser.first_name)) {
                existingUser.first_name = "我";
            }
            if (existingUser.photo == null) {
                existingUser.photo = new TLRPC.TL_userProfilePhotoEmpty();
            }
            YhPeerRegistry.registerPeer(existingUser.id, selfUserId, YhChatType.USER);
            messagesController.putUser(existingUser, true);
            return;
        }
        TLRPC.TL_user user = new TLRPC.TL_user();
        user.id = id;
        user.self = true;
        user.first_name = "我";
        user.last_name = "";
        user.status = new TLRPC.TL_userStatusRecently();
        user.photo = new TLRPC.TL_userProfilePhotoEmpty();
        YhPeerRegistry.registerPeer(user.id, selfUserId, YhChatType.USER);
        messagesController.putUser(user, true);
    }

    private static void registerSender(MessagesController messagesController, YhChatMessage message, String selfUserId) {
        if (message.isOutgoing(selfUserId) && !TextUtils.isEmpty(selfUserId)) {
            ensureSelfUser(messagesController, selfUserId);
            return;
        }
        if (message.getSenderType() == YhChatType.GROUP) {
            TLRPC.TL_chat chat = new TLRPC.TL_chat();
            chat.id = syntheticPeerId(message.getSenderId(), YhChatType.GROUP);
            YhPeerRegistry.registerGroup(chat.id, message.getSenderId());
            YhPeerRegistry.registerAvatarUrl(chat.id, YhImageUrlHelper.getDetailImageUrl(message.getSenderAvatarUrl()));
            chat.title = safeDialogTitle(message.getSenderName());
            chat.megagroup = false;
            chat.photo = new TLRPC.TL_chatPhotoEmpty();
            chat.default_banned_rights = buildDefaultBannedRights(null);
            messagesController.putChat(chat, true);
        } else {
            TLRPC.TL_user user = new TLRPC.TL_user();
            user.id = syntheticPeerId(message.getSenderId(), message.getSenderType());
            YhPeerRegistry.registerPeer(user.id, message.getSenderId(), message.getSenderType());
            YhPeerRegistry.registerAvatarUrl(user.id, YhImageUrlHelper.getDetailImageUrl(message.getSenderAvatarUrl()));
            user.first_name = safeDialogTitle(message.getSenderName());
            user.last_name = "";
            user.bot = message.getSenderType() == YhChatType.BOT;
            user.status = new TLRPC.TL_userStatusRecently();
            user.photo = new TLRPC.TL_userProfilePhotoEmpty();
            messagesController.putUser(user, true);
        }
    }

    private static TLRPC.Message buildMessage(long dialogId, long dialogPeerId, int dialogChatType,
            YhChatMessage yhMessage, String selfUserId) {
        TLRPC.TL_message message = new TLRPC.TL_message();
        message.id = syntheticMessageId(yhMessage);
        message.date = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, yhMessage.getSendTimeMs() / 1000L));
        message.edit_date = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, yhMessage.getEditTimeMs() / 1000L));
        message.dialog_id = dialogId;
        message.flags = TLRPC.MESSAGE_FLAG_HAS_FROM_ID;
        message.from_id = buildSenderPeer(yhMessage, selfUserId);
        message.peer_id = buildDialogPeer(dialogPeerId, dialogChatType);
        message.out = yhMessage.isOutgoing(selfUserId);
        message.unread = !message.out;
        message.send_state = MessageObject.MESSAGE_SEND_STATE_SENT;
        message.media = buildNativeMedia(yhMessage);
        message.message = buildMessageText(yhMessage, message.media);
        message.entities = new ArrayList<>();
        message.params = new HashMap<>();
        message.params.put(PARAM_YH_MESSAGE_ID, emptyIfNull(yhMessage.getMessageId()));
        return message;
    }

    private static TLRPC.MessageMedia buildNativeMedia(YhChatMessage message) {
        if (message == null || message.getDeleteTimeMs() > 0) {
            return new TLRPC.TL_messageMediaEmpty();
        }
        if (!TextUtils.isEmpty(message.getImageUrl()) || message.getContentType() == YhChatMessage.CONTENT_TYPE_IMAGE) {
            return buildPhotoMedia(message);
        }
        if (!TextUtils.isEmpty(message.getVideoUrl())) {
            return buildVideoMedia(message);
        }
        if (!TextUtils.isEmpty(message.getAudioUrl()) || message.getContentType() == YhChatMessage.CONTENT_TYPE_AUDIO) {
            return buildAudioMedia(message);
        }
        if (!TextUtils.isEmpty(message.getFileUrl()) || message.getContentType() == YhChatMessage.CONTENT_TYPE_FILE) {
            return buildFileMedia(message);
        }
        if (message.getContentType() == YhChatMessage.CONTENT_TYPE_STICKER) {
            return buildStickerMedia(message);
        }
        if (message.getContentType() == YhChatMessage.CONTENT_TYPE_MARKDOWN
                || message.getContentType() == YhChatMessage.CONTENT_TYPE_HTML
                || message.getContentType() == YhChatMessage.CONTENT_TYPE_POST
                || message.getContentType() == YhChatMessage.CONTENT_TYPE_FORM) {
            return buildWebPageMedia(message);
        }
        return new TLRPC.TL_messageMediaEmpty();
    }

    private static String buildMessageText(YhChatMessage message, TLRPC.MessageMedia media) {
        if (message == null) {
            return "";
        }
        if (message.getDeleteTimeMs() > 0) {
            return "消息已撤回";
        }
        if (media instanceof TLRPC.TL_messageMediaPhoto || media instanceof TLRPC.TL_messageMediaDocument) {
            return emptyIfNull(firstNonEmpty(message.getText(), message.getTip())).trim();
        }
        if (media instanceof TLRPC.TL_messageMediaWebPage) {
            return emptyIfNull(firstNonEmpty(message.getText(), resolveDisplayText(message))).trim();
        }
        return resolveDisplayText(message);
    }

    private static TLRPC.MessageMedia buildPhotoMedia(YhChatMessage message) {
        TLRPC.TL_messageMediaPhoto media = new TLRPC.TL_messageMediaPhoto();
        media.flags = 1;
        media.photo = buildPhoto(message);
        return media;
    }

    private static TLRPC.MessageMedia buildVideoMedia(YhChatMessage message) {
        TLRPC.TL_messageMediaDocument media = new TLRPC.TL_messageMediaDocument();
        media.flags = 1;
        media.document = buildVideoDocument(message);
        return media;
    }

    private static TLRPC.MessageMedia buildAudioMedia(YhChatMessage message) {
        TLRPC.TL_messageMediaDocument media = new TLRPC.TL_messageMediaDocument();
        media.flags = 1;
        media.document = buildAudioDocument(message);
        return media;
    }

    private static TLRPC.MessageMedia buildFileMedia(YhChatMessage message) {
        TLRPC.TL_messageMediaDocument media = new TLRPC.TL_messageMediaDocument();
        media.flags = 1;
        media.document = buildFileDocument(message);
        return media;
    }

    private static TLRPC.MessageMedia buildStickerMedia(YhChatMessage message) {
        TLRPC.TL_messageMediaDocument media = new TLRPC.TL_messageMediaDocument();
        media.flags = 1;
        media.document = buildStickerDocument(message);
        return media;
    }

    private static TLRPC.MessageMedia buildWebPageMedia(YhChatMessage message) {
        TLRPC.TL_messageMediaWebPage media = new TLRPC.TL_messageMediaWebPage();
        TLRPC.TL_webPage webPage = new TLRPC.TL_webPage();
        webPage.id = syntheticMediaId(message, "web");
        webPage.url = firstNonEmpty(message.getFileUrl(), firstNonEmpty(message.getImageUrl(), firstNonEmpty(message.getVideoUrl(), "")));
        webPage.display_url = emptyIfNull(webPage.url);
        webPage.hash = 0;
        webPage.site_name = "云湖";
        webPage.title = resolveWebPageTitle(message);
        webPage.description = firstNonEmpty(message.getText(), firstNonEmpty(message.getTip(), message.getQuoteText()));
        webPage.flags = 2 | 4 | 8;
        if (!TextUtils.isEmpty(message.getImageUrl())) {
            webPage.photo = buildPhoto(message);
            webPage.type = "photo";
            webPage.flags |= 1 | 16;
        } else if (!TextUtils.isEmpty(message.getVideoUrl())) {
            webPage.document = buildVideoDocument(message);
            webPage.type = "video";
            webPage.flags |= 1 | 512;
        } else if (!TextUtils.isEmpty(message.getFileUrl())) {
            webPage.document = buildFileDocument(message);
            webPage.type = "file";
            webPage.flags |= 1 | 512;
        } else {
            webPage.type = "article";
            webPage.flags |= 1;
        }
        media.webpage = webPage;
        return media;
    }

    private static TLRPC.Photo buildPhoto(YhChatMessage message) {
        TLRPC.TL_photo photo = new TLRPC.TL_photo();
        photo.id = syntheticMediaId(message, "photo");
        photo.access_hash = 0L;
        photo.file_reference = new byte[0];
        photo.date = safeDate(message.getSendTimeMs());
        photo.dc_id = YhMediaRegistry.YH_SYNTHETIC_DC_ID;
        photo.sizes = new ArrayList<>();
        YhMediaRegistry.registerPhoto(
                photo.id,
                message.getImageUrl(),
                message.getImageUrl(),
                guessMimeType(message.getImageUrl(), "image/jpeg"),
                null,
                0L
        );
        photo.sizes.add(buildRemotePhotoSize(photo.id, "y", safeDimension(message.getMediaWidth()), safeDimension(message.getMediaHeight())));
        return photo;
    }

    private static TLRPC.Document buildVideoDocument(YhChatMessage message) {
        TLRPC.TL_document document = buildBaseDocument(
                message,
                "video",
                guessMimeType(message.getVideoUrl(), "video/mp4"),
                1L,
                message.getVideoUrl(),
                firstNonEmpty(message.getImageUrl(), message.getVideoUrl()),
                resolveDocumentFileName(message, "video")
        );
        TLRPC.TL_documentAttributeVideo attribute = new TLRPC.TL_documentAttributeVideo();
        attribute.supports_streaming = true;
        attribute.w = safeDimension(message.getMediaWidth());
        attribute.h = safeDimension(message.getMediaHeight());
        attribute.duration = 0;
        document.attributes.add(attribute);
        document.thumbs.add(buildRemotePhotoSize(document.id, "v", attribute.w, attribute.h));
        document.flags = 1;
        return document;
    }

    private static TLRPC.Document buildAudioDocument(YhChatMessage message) {
        TLRPC.TL_document document = buildBaseDocument(
                message,
                "audio",
                guessMimeType(message.getAudioUrl(), "audio/ogg"),
                1L,
                message.getAudioUrl(),
                message.getAudioUrl(),
                resolveDocumentFileName(message, "audio")
        );
        TLRPC.TL_documentAttributeAudio attribute = new TLRPC.TL_documentAttributeAudio();
        attribute.voice = true;
        attribute.duration = 0;
        attribute.title = firstNonEmpty(message.getFileName(), "语音");
        document.attributes.add(attribute);
        return document;
    }

    private static TLRPC.Document buildFileDocument(YhChatMessage message) {
        String fileName = firstNonEmpty(message.getFileName(), "file");
        TLRPC.TL_document document = buildBaseDocument(
                message,
                "file",
                guessMimeType(fileName, "application/octet-stream"),
                1L,
                message.getFileUrl(),
                firstNonEmpty(message.getImageUrl(), message.getFileUrl()),
                fileName
        );
        TLRPC.TL_documentAttributeFilename attribute = new TLRPC.TL_documentAttributeFilename();
        attribute.file_name = fileName;
        document.attributes.add(attribute);
        if (isImageMime(document.mime_type)) {
            TLRPC.TL_documentAttributeImageSize size = new TLRPC.TL_documentAttributeImageSize();
            size.w = safeDimension(message.getMediaWidth());
            size.h = safeDimension(message.getMediaHeight());
            document.attributes.add(size);
            document.thumbs.add(buildRemotePhotoSize(document.id, "f", size.w, size.h));
            document.flags = 1;
        }
        return document;
    }

    private static TLRPC.Document buildStickerDocument(YhChatMessage message) {
        TLRPC.TL_document document = buildBaseDocument(
                message,
                "sticker",
                "image/webp",
                1L,
                firstNonEmpty(message.getFileUrl(), message.getImageUrl()),
                firstNonEmpty(message.getImageUrl(), message.getFileUrl()),
                resolveDocumentFileName(message, "sticker")
        );
        TLRPC.TL_documentAttributeSticker attribute = new TLRPC.TL_documentAttributeSticker();
        attribute.alt = emptyIfNull(firstNonEmpty(message.getText(), message.getTip()));
        attribute.stickerset = new TLRPC.TL_inputStickerSetEmpty();
        document.attributes.add(attribute);
        TLRPC.TL_documentAttributeImageSize size = new TLRPC.TL_documentAttributeImageSize();
        size.w = safeDimension(message.getMediaWidth());
        size.h = safeDimension(message.getMediaHeight());
        document.attributes.add(size);
        document.thumbs.add(buildRemotePhotoSize(document.id, "s", size.w, size.h));
        document.flags = 1;
        return document;
    }

    private static TLRPC.TL_document buildBaseDocument(YhChatMessage message, String suffix, String mimeType, long size,
            String mediaUrl, String previewUrl, String fileName) {
        TLRPC.TL_document document = new TLRPC.TL_document();
        document.id = syntheticMediaId(message, suffix);
        document.access_hash = 0L;
        document.file_reference = new byte[0];
        document.date = safeDate(message.getSendTimeMs());
        document.mime_type = mimeType;
        document.size = size;
        document.dc_id = YhMediaRegistry.YH_SYNTHETIC_DC_ID;
        document.attributes = new ArrayList<>();
        document.thumbs = new ArrayList<>();
        document.video_thumbs = new ArrayList<>();
        YhMediaRegistry.registerDocument(
                document.id,
                mediaUrl,
                previewUrl,
                mimeType,
                fileName,
                size
        );
        return document;
    }

    private static TLRPC.PhotoSize buildRemotePhotoSize(long ownerId, String type, int width, int height) {
        TLRPC.TL_photoSize size = new TLRPC.TL_photoSize();
        size.type = type;
        size.w = Math.max(width, 1);
        size.h = Math.max(height, 1);
        size.size = 0;
        size.location = new TLRPC.TL_fileLocationToBeDeprecated();
        size.location.volume_id = ownerId;
        size.location.local_id = Math.max(1, positiveHash(type + "_" + ownerId));
        size.location.dc_id = YhMediaRegistry.YH_SYNTHETIC_DC_ID;
        size.location.secret = 0L;
        return size;
    }

    private static String resolveDocumentFileName(YhChatMessage message, String suffix) {
        String fileName = firstNonEmpty(message.getFileName(), null);
        if (!TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        String messageId = !TextUtils.isEmpty(message.getMessageId()) ? message.getMessageId() : String.valueOf(Math.max(message.getSendTimeMs(), 1L));
        return "yh_" + suffix + "_" + messageId;
    }

    private static String resolveWebPageTitle(YhChatMessage message) {
        switch (message.getContentType()) {
            case YhChatMessage.CONTENT_TYPE_MARKDOWN:
                return "Markdown";
            case YhChatMessage.CONTENT_TYPE_HTML:
                return "HTML";
            case YhChatMessage.CONTENT_TYPE_POST:
                return "文章";
            case YhChatMessage.CONTENT_TYPE_FORM:
                return "表单";
            default:
                return "云湖内容";
        }
    }

    private static TLRPC.Peer buildSenderPeer(YhChatMessage message, String selfUserId) {
        if (message.isOutgoing(selfUserId) && !TextUtils.isEmpty(selfUserId)) {
            TLRPC.TL_peerUser peer = new TLRPC.TL_peerUser();
            peer.user_id = syntheticPeerId(selfUserId, YhChatType.USER);
            return peer;
        }
        if (message.getSenderType() == YhChatType.GROUP) {
            TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
            peer.chat_id = syntheticPeerId(message.getSenderId(), YhChatType.GROUP);
            return peer;
        }
        TLRPC.TL_peerUser peer = new TLRPC.TL_peerUser();
        peer.user_id = syntheticPeerId(message.getSenderId(), message.getSenderType());
        return peer;
    }

    private static TLRPC.Peer buildDialogPeer(long dialogPeerId, int dialogChatType) {
        if (dialogChatType == YhChatType.GROUP) {
            TLRPC.TL_peerChat peer = new TLRPC.TL_peerChat();
            peer.chat_id = dialogPeerId;
            return peer;
        }
        TLRPC.TL_peerUser peer = new TLRPC.TL_peerUser();
        peer.user_id = dialogPeerId;
        return peer;
    }

    private static TLRPC.User ensureDialogUser(YhDialogItem dialog) {
        TLRPC.TL_user user = new TLRPC.TL_user();
        user.id = syntheticPeerId(dialog.getChatId(), dialog.getChatType());
        YhPeerRegistry.registerPeer(user.id, dialog.getChatId(), dialog.getChatType());
        YhPeerRegistry.registerAvatarUrl(user.id, YhImageUrlHelper.getDetailImageUrl(dialog.getAvatarUrl()));
        user.first_name = safeDialogTitle(dialog.getTitle());
        user.last_name = "";
        user.bot = dialog.getChatType() == YhChatType.BOT;
        user.verified = dialog.isVerified();
        user.status = new TLRPC.TL_userStatusRecently();
        user.photo = new TLRPC.TL_userProfilePhotoEmpty();
        return user;
    }

    private static TLRPC.Chat ensureDialogChat(YhDialogItem dialog) {
        TLRPC.TL_chat chat = new TLRPC.TL_chat();
        chat.id = syntheticPeerId(dialog.getChatId(), dialog.getChatType());
        if (dialog.getChatType() == YhChatType.GROUP) {
            YhPeerRegistry.registerGroup(chat.id, dialog.getChatId());
        }
        YhPeerRegistry.registerAvatarUrl(chat.id, YhImageUrlHelper.getDetailImageUrl(dialog.getAvatarUrl()));
        chat.title = safeDialogTitle(dialog.getTitle());
        chat.verified = dialog.isVerified();
        chat.megagroup = false;
        chat.photo = new TLRPC.TL_chatPhotoEmpty();
        chat.default_banned_rights = buildDefaultBannedRights(null);
        return chat;
    }

    public static TLRPC.Chat applyGroupInfo(int account, TLRPC.Chat existingChat, YhGroupInfo groupInfo) {
        TLRPC.TL_chat chat = existingChat instanceof TLRPC.TL_chat ? (TLRPC.TL_chat) existingChat : new TLRPC.TL_chat();
        chat.id = syntheticPeerId(groupInfo.getGroupId(), YhChatType.GROUP);
        YhPeerRegistry.registerGroup(chat.id, groupInfo.getGroupId());
        YhPeerRegistry.registerAvatarUrl(chat.id, YhImageUrlHelper.getDetailImageUrl(groupInfo.getAvatarUrl()));
        chat.title = safeDialogTitle(groupInfo.getName());
        chat.photo = chat.photo == null ? new TLRPC.TL_chatPhotoEmpty() : chat.photo;
        chat.megagroup = false;
        chat.broadcast = false;
        chat.left = false;
        chat.kicked = false;
        chat.deactivated = false;
        chat.participants_count = safeInt(groupInfo.getMemberCount());
        chat.default_banned_rights = buildDefaultBannedRights(groupInfo);
        chat.banned_rights = null;

        String selfUserId = getSelfUserId(account);
        boolean owner = (!TextUtils.isEmpty(selfUserId) && TextUtils.equals(selfUserId, groupInfo.getOwnerId()))
                || groupInfo.getPermissionLevel() == 100;
        boolean admin = owner || groupInfo.getPermissionLevel() == 2
                || groupInfo.getAdminIds().contains(selfUserId);
        chat.creator = owner;
        chat.admin_rights = admin ? buildAdminRights(owner) : null;
        return chat;
    }

    public static TLRPC.ChatFull buildGroupChatFull(int account, TLRPC.Chat chat, YhGroupInfo groupInfo) {
        TLRPC.TL_chatFull chatFull = new TLRPC.TL_chatFull();
        chatFull.id = chat.id;
        chatFull.about = buildGroupAbout(groupInfo);
        chatFull.participants_count = Math.max(safeInt(groupInfo.getMemberCount()), chat.participants_count);
        Set<String> adminIds = new HashSet<>(groupInfo.getAdminIds());
        if (!TextUtils.isEmpty(groupInfo.getOwnerId())) {
            adminIds.add(groupInfo.getOwnerId());
        }
        chatFull.admins_count = adminIds.size();
        chatFull.kicked_count = 0;
        chatFull.banned_count = 0;
        chatFull.online_count = 0;
        chatFull.exported_invite = null;
        chatFull.chat_photo = new TLRPC.TL_photoEmpty();
        chatFull.notify_settings = buildGroupNotifySettings(groupInfo);
        TLRPC.TL_chatParticipants participants = new TLRPC.TL_chatParticipants();
        participants.chat_id = chat.id;
        participants.version = 1;
        chatFull.participants = participants;
        chatFull.hidden_prehistory = !groupInfo.isHistoryMsgEnabled();
        chatFull.participants_hidden = groupInfo.isHideGroupMembers();
        chatFull.can_view_participants = !groupInfo.isHideGroupMembers() || isSelfOwnerOrAdmin(account, groupInfo);
        chatFull.can_set_username = false;
        chatFull.ttl_period = safeInt(groupInfo.getAutoDeleteMessage());
        return chatFull;
    }

    public static void applyGroupMembers(int account, TLRPC.Chat chat, TLRPC.ChatFull chatFull, YhGroupInfo groupInfo,
            List<YhGroupMember> members, boolean reset) {
        if (chat == null || chatFull == null) {
            return;
        }
        if (chatFull.participants == null || reset) {
            TLRPC.TL_chatParticipants participants = new TLRPC.TL_chatParticipants();
            participants.chat_id = chat.id;
            participants.version = 1;
            chatFull.participants = participants;
        } else {
            chatFull.participants.chat_id = chat.id;
        }
        MessagesController messagesController = MessagesController.getInstance(account);
        String selfUserId = getSelfUserId(account);
        if (members == null) {
            return;
        }
        for (int i = 0; i < members.size(); i++) {
            YhGroupMember member = members.get(i);
            if (member == null || TextUtils.isEmpty(member.getUserId())) {
                continue;
            }
            long peerId = syntheticPeerId(member.getUserId(), YhChatType.USER);
            if (containsParticipant(chatFull.participants.participants, peerId)) {
                continue;
            }
            upsertGroupMemberUser(messagesController, member, selfUserId);
            TLRPC.ChatParticipant participant = buildGroupMemberParticipant(member, selfUserId, groupInfo);
            chatFull.participants.participants.add(participant);
            if (TextUtils.equals(member.getUserId(), selfUserId)) {
                chatFull.participants.self_participant = participant;
            }
        }
        int loadedParticipantsCount = chatFull.participants != null ? chatFull.participants.participants.size() : 0;
        int remoteParticipantsCount = groupInfo != null ? safeInt(groupInfo.getMemberCount()) : 0;
        chatFull.participants_count = Math.max(Math.max(chatFull.participants_count, loadedParticipantsCount), remoteParticipantsCount);
        chat.participants_count = Math.max(Math.max(chat.participants_count, loadedParticipantsCount), remoteParticipantsCount);
    }

    public static int resolveDisplayedMemberCount(TLRPC.Chat chat, TLRPC.ChatFull chatInfo) {
        int count = chat != null ? Math.max(0, chat.participants_count) : 0;
        if (chatInfo == null) {
            return count;
        }
        count = Math.max(count, Math.max(0, chatInfo.participants_count));
        if (chatInfo.participants != null) {
            count = Math.max(count, chatInfo.participants.participants.size());
        }
        return count;
    }

    public static TLRPC.User applyUserDetail(int account, TLRPC.User existingUser, YhUserDetail detail) {
        TLRPC.TL_user user = existingUser instanceof TLRPC.TL_user ? (TLRPC.TL_user) existingUser : new TLRPC.TL_user();
        user.id = syntheticPeerId(detail.getId(), YhChatType.USER);
        YhPeerRegistry.registerPeer(user.id, detail.getId(), YhChatType.USER);
        YhPeerRegistry.registerAvatarUrl(user.id, YhImageUrlHelper.getDetailImageUrl(detail.getAvatarUrl()));
        String displayName = !TextUtils.isEmpty(detail.getRemarkName()) ? detail.getRemarkName() : detail.getName();
        user.first_name = !TextUtils.isEmpty(displayName) ? displayName : "云湖用户";
        user.last_name = "";
        user.phone = normalizePhone(detail.getPhoneNumber());
        user.bot = false;
        user.self = TextUtils.equals(getSelfUserId(account), detail.getId());
        user.contact = !TextUtils.isEmpty(user.phone);
        user.mutual_contact = user.contact;
        user.deleted = false;
        user.premium = detail.isVip();
        user.status = new TLRPC.TL_userStatusRecently();
        user.photo = user.photo == null ? new TLRPC.TL_userProfilePhotoEmpty() : user.photo;
        return user;
    }

    public static TLRPC.User applyBotDetail(TLRPC.User existingUser, YhBotDetail detail) {
        TLRPC.TL_user user = existingUser instanceof TLRPC.TL_user ? (TLRPC.TL_user) existingUser : new TLRPC.TL_user();
        user.id = syntheticPeerId(detail.getBotId(), YhChatType.BOT);
        YhPeerRegistry.registerPeer(user.id, detail.getBotId(), YhChatType.BOT);
        YhPeerRegistry.registerAvatarUrl(user.id, YhImageUrlHelper.getDetailImageUrl(detail.getAvatarUrl()));
        user.first_name = !TextUtils.isEmpty(detail.getName()) ? detail.getName() : "云湖机器人";
        user.last_name = "";
        user.bot = true;
        user.bot_nochats = detail.isGroupLimit();
        user.deleted = false;
        user.status = new TLRPC.TL_userStatusRecently();
        user.photo = user.photo == null ? new TLRPC.TL_userProfilePhotoEmpty() : user.photo;
        return user;
    }

    public static TLRPC.UserFull buildUserFull(TLRPC.User user, YhUserDetail detail) {
        TLRPC.TL_userFull userFull = new TLRPC.TL_userFull();
        userFull.id = user.id;
        userFull.user = user;
        userFull.about = buildUserAbout(detail);
        userFull.common_chats_count = 0;
        userFull.phone_calls_available = false;
        userFull.phone_calls_private = true;
        userFull.video_calls_available = false;
        userFull.voice_messages_forbidden = false;
        userFull.settings = new TLRPC.TL_peerSettings();
        userFull.notify_settings = new TLRPC.TL_peerNotifySettings();
        if (!TextUtils.isEmpty(userFull.about)) {
            userFull.flags |= 2;
        }
        return userFull;
    }

    public static TLRPC.UserFull buildBotUserFull(TLRPC.User user, YhBotDetail detail) {
        TLRPC.TL_userFull userFull = new TLRPC.TL_userFull();
        userFull.id = user.id;
        userFull.user = user;
        userFull.about = buildBotAbout(detail);
        userFull.common_chats_count = 0;
        userFull.phone_calls_available = false;
        userFull.phone_calls_private = true;
        userFull.video_calls_available = false;
        userFull.voice_messages_forbidden = false;
        userFull.settings = new TLRPC.TL_peerSettings();
        userFull.notify_settings = new TLRPC.TL_peerNotifySettings();
        userFull.bot_info = buildBotInfo(user.id, detail);
        if (!TextUtils.isEmpty(userFull.about)) {
            userFull.flags |= 2;
        }
        if (userFull.bot_info != null) {
            userFull.flags |= 8;
        }
        return userFull;
    }

    public static TLRPC.BotInfo buildBotInfo(long userId, YhBotDetail detail) {
        TLRPC.TL_botInfo botInfo = new TLRPC.TL_botInfo();
        botInfo.user_id = userId;
        botInfo.description = buildBotAbout(detail);
        botInfo.flags = 1;
        if (!TextUtils.isEmpty(botInfo.description)) {
            botInfo.flags |= 2;
        }
        return botInfo;
    }

    public static String buildUserBirthday(YhUserDetail detail) {
        if (detail == null || detail.getBirthday() <= 0) {
            return null;
        }
        long millis = detail.getBirthday() < 1_000_000_000_000L ? detail.getBirthday() * 1000L : detail.getBirthday();
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(millis));
        } catch (Exception ignore) {
            return String.valueOf(detail.getBirthday());
        }
    }

    public static String buildUserProfileId(YhUserDetail detail) {
        if (detail == null || detail.getNameId() <= 0) {
            return null;
        }
        return String.valueOf(detail.getNameId());
    }

    public static String buildBotProfileId(YhBotDetail detail) {
        if (detail == null || TextUtils.isEmpty(detail.getBotId())) {
            return null;
        }
        return detail.getBotId().trim();
    }

    private static String resolveDisplayText(YhChatMessage message) {
        if (message.getDeleteTimeMs() > 0) {
            return "消息已撤回";
        }
        if (!TextUtils.isEmpty(message.getText())) {
            return message.getText();
        }
        switch (message.getContentType()) {
            case YhChatMessage.CONTENT_TYPE_IMAGE:
                return "[图片]";
            case YhChatMessage.CONTENT_TYPE_MARKDOWN:
            case YhChatMessage.CONTENT_TYPE_HTML:
                return !TextUtils.isEmpty(message.getText()) ? message.getText() : "[富文本]";
            case YhChatMessage.CONTENT_TYPE_FILE:
                return !TextUtils.isEmpty(message.getFileName()) ? "[文件] " + message.getFileName() : "[文件]";
            case YhChatMessage.CONTENT_TYPE_FORM:
                return "[表单]";
            case YhChatMessage.CONTENT_TYPE_POST:
                return "[文章]";
            case YhChatMessage.CONTENT_TYPE_STICKER:
                return "[表情]";
            case YhChatMessage.CONTENT_TYPE_AUDIO:
                return "[语音]";
            case YhChatMessage.CONTENT_TYPE_CALL:
                if (!TextUtils.isEmpty(message.getCallText())) {
                    return message.getCallText();
                }
                if (!TextUtils.isEmpty(message.getCallStatusText())) {
                    return message.getCallStatusText();
                }
                return "[语音通话]";
            default:
                if (!TextUtils.isEmpty(message.getTip())) {
                    return message.getTip();
                }
                if (!TextUtils.isEmpty(message.getQuoteText())) {
                    return message.getQuoteText();
                }
                if (!TextUtils.isEmpty(message.getImageUrl())) {
                    return "[图片]";
                }
                if (!TextUtils.isEmpty(message.getVideoUrl())) {
                    return "[视频]";
                }
                if (!TextUtils.isEmpty(message.getAudioUrl())) {
                    return "[语音]";
                }
                if (!TextUtils.isEmpty(message.getFileUrl())) {
                    return "[文件]";
                }
                return "[云湖消息]";
        }
    }

    private static int syntheticMessageId(YhChatMessage message) {
        if (message.getSequence() > 0 && message.getSequence() <= Integer.MAX_VALUE) {
            return (int) message.getSequence();
        }
        int value = message.getMessageId() == null ? 0 : positiveHash(message.getMessageId());
        if (value == 0) {
            long fallback = Math.max(message.getSendTimeMs(), 1L) % Integer.MAX_VALUE;
            value = (int) Math.max(fallback, 1L);
        }
        return value;
    }

    private static String safeDialogTitle(String value) {
        return TextUtils.isEmpty(value) ? "云湖会话" : value;
    }

    private static String normalizePhone(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        return value.replace("+", "").replace(" ", "");
    }

    private static void upsertGroupMemberUser(MessagesController messagesController, YhGroupMember member, String selfUserId) {
        if (TextUtils.equals(member.getUserId(), selfUserId)) {
            YhPeerRegistry.registerAvatarUrl(syntheticPeerId(selfUserId, YhChatType.USER), YhImageUrlHelper.getDetailImageUrl(member.getAvatarUrl()));
            ensureSelfUser(messagesController, selfUserId);
            TLRPC.User selfUser = messagesController.getUser(syntheticPeerId(selfUserId, YhChatType.USER));
            if (selfUser != null && !TextUtils.isEmpty(member.getName())) {
                selfUser.first_name = member.getName();
                selfUser.last_name = "";
                messagesController.putUser(selfUser, true);
            }
            return;
        }
        long peerId = syntheticPeerId(member.getUserId(), YhChatType.USER);
        YhPeerRegistry.registerPeer(peerId, member.getUserId(), YhChatType.USER);
        YhPeerRegistry.registerAvatarUrl(peerId, YhImageUrlHelper.getDetailImageUrl(member.getAvatarUrl()));
        TLRPC.User existingUser = messagesController.getUser(peerId);
        TLRPC.TL_user user = existingUser instanceof TLRPC.TL_user ? (TLRPC.TL_user) existingUser : new TLRPC.TL_user();
        user.id = peerId;
        user.first_name = !TextUtils.isEmpty(member.getName()) ? member.getName() : "云湖成员";
        user.last_name = "";
        user.status = new TLRPC.TL_userStatusRecently();
        user.photo = new TLRPC.TL_userProfilePhotoEmpty();
        messagesController.putUser(user, true);
    }

    private static TLRPC.ChatParticipant buildGroupMemberParticipant(YhGroupMember member, String selfUserId, YhGroupInfo groupInfo) {
        long peerId = syntheticPeerId(member.getUserId(), YhChatType.USER);
        boolean isOwner = member.getPermissionLevel() == 100
                || groupInfo != null && !TextUtils.isEmpty(groupInfo.getOwnerId()) && TextUtils.equals(groupInfo.getOwnerId(), member.getUserId());
        if (isOwner) {
            TLRPC.TL_chatParticipantCreator creator = new TLRPC.TL_chatParticipantCreator();
            creator.user_id = peerId;
            return creator;
        }
        if (member.getPermissionLevel() == 2 || groupInfo != null && groupInfo.getAdminIds().contains(member.getUserId())) {
            TLRPC.TL_chatParticipantAdmin admin = new TLRPC.TL_chatParticipantAdmin();
            admin.user_id = peerId;
            admin.inviter_id = !TextUtils.isEmpty(selfUserId) ? syntheticPeerId(selfUserId, YhChatType.USER) : peerId;
            admin.date = resolveParticipantDate(member);
            return admin;
        }
        TLRPC.TL_chatParticipant regular = new TLRPC.TL_chatParticipant();
        regular.user_id = peerId;
        regular.inviter_id = !TextUtils.isEmpty(selfUserId) ? syntheticPeerId(selfUserId, YhChatType.USER) : peerId;
        regular.date = resolveParticipantDate(member);
        return regular;
    }

    private static boolean containsParticipant(List<TLRPC.ChatParticipant> participants, long userId) {
        if (participants == null) {
            return false;
        }
        for (int i = 0; i < participants.size(); i++) {
            TLRPC.ChatParticipant participant = participants.get(i);
            if (participant != null && participant.user_id == userId) {
                return true;
            }
        }
        return false;
    }

    private static int resolveParticipantDate(YhGroupMember member) {
        long gagTime = member.getGagTime();
        if (gagTime > 0 && gagTime <= Integer.MAX_VALUE) {
            return (int) gagTime;
        }
        return (int) (System.currentTimeMillis() / 1000L);
    }

    private static TLRPC.TL_chatAdminRights buildAdminRights(boolean owner) {
        TLRPC.TL_chatAdminRights rights = new TLRPC.TL_chatAdminRights();
        rights.change_info = true;
        rights.delete_messages = true;
        rights.ban_users = true;
        rights.invite_users = true;
        rights.pin_messages = true;
        rights.add_admins = owner;
        rights.manage_call = true;
        rights.other = true;
        rights.manage_topics = true;
        return rights;
    }

    private static TLRPC.TL_chatBannedRights buildDefaultBannedRights(YhGroupInfo groupInfo) {
        TLRPC.TL_chatBannedRights rights = new TLRPC.TL_chatBannedRights();
        rights.view_messages = false;
        rights.send_media = false;
        rights.send_messages = false;
        rights.send_stickers = false;
        rights.send_gifs = false;
        rights.send_games = false;
        rights.send_inline = false;
        rights.embed_links = false;
        rights.send_polls = false;
        rights.change_info = false;
        rights.invite_users = false;
        rights.pin_messages = false;
        rights.manage_topics = false;
        rights.send_photos = false;
        rights.send_videos = false;
        rights.send_roundvideos = false;
        rights.send_audios = false;
        rights.send_voices = false;
        rights.send_docs = false;
        rights.send_plain = false;
        rights.until_date = 0;

        if (groupInfo == null || TextUtils.isEmpty(groupInfo.getLimitedMsgType())) {
            return rights;
        }

        Set<Integer> limitedTypes = parseLimitedTypes(groupInfo.getLimitedMsgType());
        for (Integer type : limitedTypes) {
            if (type == null) {
                continue;
            }
            switch (type) {
                case 1:
                    rights.send_plain = true;
                    break;
                case 2:
                    rights.send_photos = true;
                    break;
                case 4:
                    rights.send_docs = true;
                    break;
                case 6:
                    rights.embed_links = true;
                    break;
                case 7:
                    rights.send_stickers = true;
                    rights.send_gifs = true;
                    rights.send_games = true;
                    rights.send_inline = true;
                    break;
                case 10:
                    rights.send_videos = true;
                    rights.send_roundvideos = true;
                    break;
                case 11:
                    rights.send_audios = true;
                    rights.send_voices = true;
                    break;
            }
        }
        return rights;
    }

    private static Set<Integer> parseLimitedTypes(String value) {
        Set<Integer> result = new HashSet<>();
        if (TextUtils.isEmpty(value)) {
            return result;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            if (TextUtils.isEmpty(part)) {
                continue;
            }
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (Exception ignore) {
            }
        }
        return result;
    }

    private static String getSelfUserId(int account) {
        YhSession session = YhApiFacade.getInstance(account).getSession();
        return session != null ? session.getUserId() : null;
    }

    private static boolean isSelfOwnerOrAdmin(int account, YhGroupInfo groupInfo) {
        String selfUserId = getSelfUserId(account);
        if (TextUtils.isEmpty(selfUserId)) {
            return false;
        }
        return TextUtils.equals(selfUserId, groupInfo.getOwnerId())
                || groupInfo.getPermissionLevel() == 100
                || groupInfo.getPermissionLevel() == 2
                || groupInfo.getAdminIds().contains(selfUserId);
    }

    private static TLRPC.TL_peerNotifySettings buildGroupNotifySettings(YhGroupInfo groupInfo) {
        TLRPC.TL_peerNotifySettings notifySettings = new TLRPC.TL_peerNotifySettings();
        notifySettings.flags = 1 | 2 | 4;
        notifySettings.show_previews = true;
        notifySettings.silent = groupInfo.isDoNotDisturb();
        notifySettings.mute_until = groupInfo.isDoNotDisturb() ? Integer.MAX_VALUE : 0;
        return notifySettings;
    }

    private static String buildGroupAbout(YhGroupInfo groupInfo) {
        return emptyIfNull(groupInfo.getIntroduction()).trim();
    }

    private static String buildUserAbout(YhUserDetail detail) {
        StringBuilder builder = new StringBuilder();
        appendAboutLine(builder, detail.getIntroduction());
        if (detail.getNameId() > 0) {
            appendAboutLine(builder, "云湖号：" + detail.getNameId());
        }
        appendAboutLine(builder, "手机号：", detail.getPhoneNumber());
        appendAboutLine(builder, "地区：", firstNonEmpty(detail.getIpGeo(), joinAddress(detail)));
        appendAboutLine(builder, "最近活跃：", detail.getLastActiveTime());
        appendAboutLine(builder, "注册时间：", detail.getRegisterTime());
        appendAboutLine(builder, "性别：", formatGender(detail.getGender()));
        appendAboutLine(builder, "备注：", detail.getExtraRemark());
        if (detail.getOnlineDay() > 0) {
            appendAboutLine(builder, "在线天数：" + detail.getOnlineDay());
        }
        if (detail.getContinuousOnlineDay() > 0) {
            appendAboutLine(builder, "连续在线：" + detail.getContinuousOnlineDay());
        }
        if (detail.isVip()) {
            appendAboutLine(builder, "会员：是");
            appendAboutLine(builder, "会员到期：", formatTimestamp(detail.getVipExpiredTime()));
        }
        appendAboutLine(builder, "封禁截止：", formatTimestamp(detail.getBanTime()));
        if (!detail.getMedals().isEmpty()) {
            appendAboutLine(builder, "勋章：" + TextUtils.join("、", detail.getMedals()));
        }
        return builder.toString().trim();
    }

    private static String buildBotAbout(YhBotDetail detail) {
        StringBuilder builder = new StringBuilder();
        appendAboutLine(builder, detail.getIntroduction());
        if (!TextUtils.isEmpty(detail.getBotId())) {
            appendAboutLine(builder, "Bot ID：" + detail.getBotId());
        }
        if (detail.getHeadcount() > 0) {
            appendAboutLine(builder, "使用人数：" + detail.getHeadcount());
        }
        appendAboutLine(builder, "创建者：", detail.getCreateBy());
        appendAboutLine(builder, "创建时间：", formatTimestamp(detail.getCreateTime()));
        appendAboutLine(builder, "私有机器人：", formatBoolean(detail.isPrivateBot()));
        appendAboutLine(builder, "停用状态：", formatBoolean(detail.isStopped()));
        appendAboutLine(builder, "自动进群：", formatBoolean(detail.isAlwaysAgree()));
        appendAboutLine(builder, "不可删除：", formatBoolean(detail.isCanNotDelete()));
        appendAboutLine(builder, "免打扰：", formatBoolean(detail.isDoNotDisturb()));
        appendAboutLine(builder, "置顶：", formatBoolean(detail.isTop()));
        appendAboutLine(builder, "限制进群：", formatBoolean(detail.isGroupLimit()));
        return builder.toString().trim();
    }

    private static void appendAboutLine(StringBuilder builder, String line) {
        if (TextUtils.isEmpty(line)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(line.trim());
    }

    private static void appendAboutLine(StringBuilder builder, String label, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        appendAboutLine(builder, label + value);
    }

    private static String formatAutoDeleteMessage(long seconds) {
        if (seconds <= 0) {
            return "关闭";
        }
        if (seconds % 86400L == 0) {
            return (seconds / 86400L) + " 天";
        }
        if (seconds % 3600L == 0) {
            return (seconds / 3600L) + " 小时";
        }
        if (seconds % 60L == 0) {
            return (seconds / 60L) + " 分钟";
        }
        return seconds + " 秒";
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private static String firstNonEmpty(String first, String second) {
        return !TextUtils.isEmpty(first) ? first : second;
    }

    private static long syntheticMediaId(YhChatMessage message, String suffix) {
        String key = (message != null ? message.getMessageId() : "") + "_" + suffix;
        long value = key.hashCode();
        if (value == Long.MIN_VALUE) {
            value = Long.MAX_VALUE;
        }
        value = Math.abs(value);
        return value == 0 ? 1L : value;
    }

    private static int safeDate(long timeMs) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, timeMs / 1000L));
    }

    private static int safeDimension(long value) {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, value > 0 ? value : 512L));
    }

    private static String guessMimeType(String nameOrUrl, String fallback) {
        if (TextUtils.isEmpty(nameOrUrl)) {
            return fallback;
        }
        String mimeType = URLConnection.guessContentTypeFromName(nameOrUrl);
        return TextUtils.isEmpty(mimeType) ? fallback : mimeType;
    }

    private static boolean isImageMime(String mimeType) {
        return !TextUtils.isEmpty(mimeType) && mimeType.startsWith("image/");
    }

    private static String joinAddress(YhUserDetail detail) {
        StringBuilder builder = new StringBuilder();
        appendAddress(builder, detail.getCity());
        appendAddress(builder, detail.getDistrict());
        appendAddress(builder, detail.getAddress());
        return builder.toString();
    }

    private static void appendAddress(StringBuilder builder, String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }

    private static String formatGender(int gender) {
        switch (gender) {
            case 1:
                return "男";
            case 2:
                return "女";
            case 3:
                return "其他";
            default:
                return null;
        }
    }

    private static String formatBoolean(boolean value) {
        return value ? "是" : "否";
    }

    private static String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            return null;
        }
        long millis = timestamp < 1_000_000_000_000L ? timestamp * 1000L : timestamp;
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date(millis));
        } catch (Exception ignore) {
            return String.valueOf(timestamp);
        }
    }

    private static int safeInt(long value) {
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, value));
    }

    private static int positiveHash(String value) {
        int hash = value == null ? 0 : value.hashCode();
        if (hash == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(hash);
    }
}
