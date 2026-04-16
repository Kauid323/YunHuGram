package org.telegram.yh.chat;

import android.text.TextUtils;

public class YhChatMessage {

    public static final int CONTENT_TYPE_TEXT = 1;
    public static final int CONTENT_TYPE_IMAGE = 2;
    public static final int CONTENT_TYPE_MARKDOWN = 3;
    public static final int CONTENT_TYPE_FILE = 4;
    public static final int CONTENT_TYPE_FORM = 5;
    public static final int CONTENT_TYPE_POST = 6;
    public static final int CONTENT_TYPE_STICKER = 7;
    public static final int CONTENT_TYPE_HTML = 8;
    public static final int CONTENT_TYPE_AUDIO = 11;
    public static final int CONTENT_TYPE_CALL = 13;

    private final String messageId;
    private final long sequence;
    private final int contentType;
    private final String direction;
    private final long sendTimeMs;
    private final long editTimeMs;
    private final long deleteTimeMs;
    private final String quoteMessageId;
    private final String text;
    private final String quoteText;
    private final String imageUrl;
    private final String fileName;
    private final String fileUrl;
    private final String videoUrl;
    private final String audioUrl;
    private final String callText;
    private final String callStatusText;
    private final String tip;
    private final long mediaWidth;
    private final long mediaHeight;
    private final String senderId;
    private final int senderType;
    private final String senderName;
    private final String senderAvatarUrl;

    public YhChatMessage(String messageId, long sequence, int contentType, String direction, long sendTimeMs,
            long editTimeMs, long deleteTimeMs, String quoteMessageId, String text, String quoteText, String imageUrl,
            String fileName, String fileUrl, String videoUrl, String audioUrl, String callText, String callStatusText,
            String tip, long mediaWidth, long mediaHeight, String senderId, int senderType, String senderName,
            String senderAvatarUrl) {
        this.messageId = messageId;
        this.sequence = sequence;
        this.contentType = contentType;
        this.direction = direction;
        this.sendTimeMs = sendTimeMs;
        this.editTimeMs = editTimeMs;
        this.deleteTimeMs = deleteTimeMs;
        this.quoteMessageId = quoteMessageId;
        this.text = text;
        this.quoteText = quoteText;
        this.imageUrl = imageUrl;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.videoUrl = videoUrl;
        this.audioUrl = audioUrl;
        this.callText = callText;
        this.callStatusText = callStatusText;
        this.tip = tip;
        this.mediaWidth = mediaWidth;
        this.mediaHeight = mediaHeight;
        this.senderId = senderId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.senderAvatarUrl = senderAvatarUrl;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getSequence() {
        return sequence;
    }

    public int getContentType() {
        return contentType;
    }

    public String getDirection() {
        return direction;
    }

    public long getSendTimeMs() {
        return sendTimeMs;
    }

    public long getEditTimeMs() {
        return editTimeMs;
    }

    public long getDeleteTimeMs() {
        return deleteTimeMs;
    }

    public String getQuoteMessageId() {
        return quoteMessageId;
    }

    public String getText() {
        return text;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getCallText() {
        return callText;
    }

    public String getCallStatusText() {
        return callStatusText;
    }

    public String getTip() {
        return tip;
    }

    public long getMediaWidth() {
        return mediaWidth;
    }

    public long getMediaHeight() {
        return mediaHeight;
    }

    public String getSenderId() {
        return senderId;
    }

    public int getSenderType() {
        return senderType;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public boolean isOutgoing(String selfUserId) {
        if ("right".equalsIgnoreCase(direction)) {
            return true;
        }
        return !TextUtils.isEmpty(selfUserId) && TextUtils.equals(selfUserId, senderId);
    }
}
