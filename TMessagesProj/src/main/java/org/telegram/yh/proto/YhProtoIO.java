package org.telegram.yh.proto;

import com.yhchat.canary.proto.list_message_by_seq_send;
import com.yhchat.canary.proto.list_message_by_seq;
import com.yhchat.canary.proto.list_message;
import com.yhchat.canary.proto.list_message_send;
import com.yhchat.canary.proto.Msg;
import com.yhchat.canary.proto.send_message;
import com.yhchat.canary.proto.send_message_send;
import com.yhchat.canary.proto.group.info_send;
import com.yhchat.canary.proto.group.list_member;
import com.yhchat.canary.proto.group.list_member_send;

import org.telegram.yh.YhApiStatus;
import org.telegram.yh.YhChatType;
import org.telegram.yh.chat.YhChatMessage;
import org.telegram.yh.model.YhAddressBookEntry;
import org.telegram.yh.model.YhBotDetail;
import org.telegram.yh.model.YhConversation;
import org.telegram.yh.model.YhConversationList;
import org.telegram.yh.model.YhGroupInfo;
import org.telegram.yh.model.YhGroupMember;
import org.telegram.yh.model.YhMention;
import org.telegram.yh.model.YhMessageListSpec;
import org.telegram.yh.model.YhSelfUser;
import org.telegram.yh.model.YhTextMessageSpec;
import org.telegram.yh.model.YhUserDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import yh_conversation.ConversationList;
import yh_conversation.Status;

public final class YhProtoIO {

    private static final long CONTENT_TYPE_TEXT = 1L;

    private YhProtoIO() {
    }

    public static byte[] buildSendTextMessage(YhTextMessageSpec spec) throws IOException {
        send_message_send.Content content = new send_message_send.Content.Builder()
                .text(protoString(spec.getText()))
                .quote_msg_text(protoString(spec.getQuoteMessageText()))
                .build();
        send_message_send request = new send_message_send.Builder()
                .msg_id(protoString(spec.getMessageId()))
                .chat_id(protoString(spec.getChatId()))
                .chat_type((long) spec.getChatType())
                .content(content)
                .content_type(CONTENT_TYPE_TEXT)
                .quote_msg_id(protoString(spec.getQuoteMessageId()))
                .build();
        return send_message_send.ADAPTER.encode(request);
    }

    public static byte[] buildListMessagesRequest(YhMessageListSpec spec) throws IOException {
        list_message_send request = new list_message_send.Builder()
                .msg_count((long) spec.getMessageCount())
                .msg_id(protoString(spec.getMessageId()))
                .chat_type((long) spec.getChatType())
                .chat_id(protoString(spec.getChatId()))
                .build();
        return list_message_send.ADAPTER.encode(request);
    }

    public static byte[] buildListMessagesBySeqRequest(String chatId, int chatType, long messageStart) throws IOException {
        list_message_by_seq_send request = new list_message_by_seq_send.Builder()
                .msg_seq(messageStart)
                .chat_type((long) chatType)
                .chat_id(protoString(chatId))
                .build();
        return list_message_by_seq_send.ADAPTER.encode(request);
    }

    public static byte[] buildGroupInfoRequest(String groupId) throws IOException {
        info_send request = new info_send.Builder()
                .group_id(protoString(groupId))
                .build();
        return info_send.ADAPTER.encode(request);
    }

    public static byte[] buildGroupMembersRequest(String groupId, int size, int page, String keywords) throws IOException {
        android.util.Log.d("YHProtoIO", "buildGroupMembersRequest: groupId=" + groupId + " size=" + size + " page=" + page);
        list_member_send.Data data = new list_member_send.Data.Builder()
                .size(size)
                .page(page)
                .build();
        list_member_send.Builder requestBuilder = new list_member_send.Builder()
                .group_id(protoString(groupId))
                .data(data);
        if (keywords != null && keywords.length() > 0) {
            requestBuilder.keywords(keywords);
        }
        list_member_send built = requestBuilder.build();
        android.util.Log.d("YHProtoIO", "Request built: groupId=" + built.group_id + " data.size=" + (built.data != null ? built.data.size : "null") + " data.page=" + (built.data != null ? built.data.page : "null"));
        return list_member_send.ADAPTER.encode(built);
    }

    public static byte[] buildUserDetailRequest(String userId) throws IOException {
        yh_user.get_user_send request = new yh_user.get_user_send.Builder()
                .id(protoString(userId))
                .build();
        return yh_user.get_user_send.ADAPTER.encode(request);
    }

    public static byte[] buildAddressBookListRequest(String requestId) throws IOException {
        yh_user.address_book_list_send request = new yh_user.address_book_list_send.Builder()
                .number(protoString(requestId))
                .build();
        return yh_user.address_book_list_send.ADAPTER.encode(request);
    }

    public static byte[] buildBotInfoRequest(String botId) throws IOException {
        yh_bot.bot_info_send request = new yh_bot.bot_info_send.Builder()
                .id(protoString(botId))
                .build();
        return yh_bot.bot_info_send.ADAPTER.encode(request);
    }

    public static YhApiStatus parseStatusResponse(byte[] body) throws IOException {
        send_message response = send_message.ADAPTER.decode(body);
        return mapStatus(response != null ? response.status : null);
    }

    public static YhConversationList parseConversationList(byte[] body) throws IOException {
        ConversationList response = ConversationList.ADAPTER.decode(body);
        YhApiStatus status = mapStatus(response != null ? response.status : null);
        List<YhConversation> conversations = new ArrayList<>();
        Integer total = null;
        String requestId = null;
        if (response != null) {
            if (response.data != null) {
                for (ConversationList.ConversationData item : response.data) {
                    if (item != null) {
                        conversations.add(mapConversation(item));
                    }
                }
            }
            total = response.total;
            requestId = response.request_id;
        }
        return new YhConversationList(status, conversations, total == null ? 0L : total.longValue(), requestId);
    }

    public static ArrayList<YhChatMessage> parseMessageList(byte[] body) throws IOException {
        list_message response = list_message.ADAPTER.decode(body);
        return parseMessages(response != null ? response.status : null, response != null ? response.msg : null);
    }

    public static ArrayList<YhChatMessage> parseMessageListBySeq(byte[] body) throws IOException {
        list_message_by_seq response = list_message_by_seq.ADAPTER.decode(body);
        return parseMessages(response != null ? response.status : null, response != null ? response.msg : null);
    }

    public static YhSelfUser parseSelfUser(byte[] body) throws IOException {
        yh_user.info response = yh_user.info.ADAPTER.decode(body);
        if (response == null || response.status == null) {
            throw new IOException("Missing user info response");
        }
        if (response.status.code != 1) {
            throw new IOException(response.status.msg == null || response.status.msg.length() == 0 ? "加载用户信息失败" : response.status.msg);
        }
        if (response.data == null) {
            throw new IOException("Missing user info data");
        }
        return new YhSelfUser(
                response.data.id,
                response.data.name,
                response.data.avatar_url,
                response.data.avatar_id,
                response.data.phone,
                response.data.email,
                response.data.coin,
                response.data.is_vip != 0,
                response.data.vip_expired_time,
                response.data.invitation_code
        );
    }

    public static YhUserDetail parseUserDetail(byte[] body) throws IOException {
        yh_user.get_user response = yh_user.get_user.ADAPTER.decode(body);
        YhApiStatus status = mapStatus(response != null ? response.status : null);
        if (!status.isSuccess()) {
            throw new IOException(status.getMessage() == null || status.getMessage().length() == 0 ? "加载用户详情失败" : status.getMessage());
        }
        if (response == null || response.data == null) {
            throw new IOException("Missing user detail data");
        }
        yh_user.get_user.Data data = response.data;
        List<String> medals = new ArrayList<>();
        if (data.yh_user_medal != null) {
            for (yh_user.Medal_info medal : data.yh_user_medal) {
                if (medal != null && medal.name != null && medal.name.length() > 0) {
                    medals.add(medal.name);
                }
            }
        }
        yh_user.RemarkInfo remarkInfo = data.remark_info;
        yh_user.ProfileInfo profileInfo = data.profile_info;
        return new YhUserDetail(
                data.id,
                data.name,
                data.name_id,
                data.avatar_url,
                data.avatar_id,
                medals,
                data.register_time,
                data.ban_time,
                data.online_day,
                data.continuous_online_day,
                data.is_vip != 0,
                data.vip_expired_time,
                remarkInfo != null ? remarkInfo.remark_name : null,
                remarkInfo != null ? remarkInfo.phone_number : null,
                remarkInfo != null ? remarkInfo.extra_remark : null,
                profileInfo != null ? profileInfo.last_active_time : null,
                profileInfo != null ? profileInfo.introduction : null,
                profileInfo != null ? profileInfo.gender : 0,
                profileInfo != null ? profileInfo.birthday : 0L,
                profileInfo != null ? profileInfo.city : null,
                profileInfo != null ? profileInfo.district : null,
                profileInfo != null ? profileInfo.address : null,
                data.ipGeo
        );
    }

    public static YhBotDetail parseBotDetail(byte[] body) throws IOException {
        yh_bot.bot_info response = yh_bot.bot_info.ADAPTER.decode(body);
        YhApiStatus status = mapStatus(response != null ? response.status : null);
        if (!status.isSuccess()) {
            throw new IOException(status.getMessage() == null || status.getMessage().length() == 0 ? "加载机器人详情失败" : status.getMessage());
        }
        if (response == null || response.data == null) {
            throw new IOException("Missing bot detail data");
        }
        yh_bot.bot_info.Bot_data data = response.data;
        return new YhBotDetail(
                data.bot_id,
                data.name,
                data.name_id,
                data.avatar_url,
                data.avatar_id,
                data.introduction,
                data.create_by,
                data.create_time,
                data.headcount,
                data.private_ != 0,
                data.is_stop != 0,
                data.always_agree != 0,
                data.no_detele != 0,
                data.do_not_disturb != 0,
                data.top != 0,
                data.group_limit != 0
        );
    }

    public static ArrayList<YhAddressBookEntry> parseAddressBookList(byte[] body) throws IOException {
        yh_user.address_book_list response = yh_user.address_book_list.ADAPTER.decode(body);
        YhApiStatus status = mapStatus(response != null ? response.status : null);
        if (!status.isSuccess()) {
            throw new IOException(status.getMessage() == null || status.getMessage().length() == 0 ? "加载通讯录失败" : status.getMessage());
        }
        ArrayList<YhAddressBookEntry> entries = new ArrayList<>();
        if (response == null || response.data == null) {
            return entries;
        }
        for (yh_user.address_book_list.Data section : response.data) {
            if (section == null || section.data == null) {
                continue;
            }
            String listName = section.list_name;
            for (yh_user.address_book_list.Data.Data_list item : section.data) {
                if (item == null) {
                    continue;
                }
                entries.add(new YhAddressBookEntry(
                        listName,
                        item.chat_id,
                        resolveAddressBookChatType(listName, item.permisson_level),
                        item.name,
                        item.avatar_url,
                        item.permisson_level,
                        item.noDisturb
                ));
            }
        }
        return entries;
    }

    public static YhGroupInfo parseGroupInfo(byte[] body) throws IOException {
        com.yhchat.canary.proto.group.info response = com.yhchat.canary.proto.group.info.ADAPTER.decode(body);
        YhApiStatus status = mapGroupStatus(response != null ? response.status : null);
        if (!status.isSuccess()) {
            throw new IOException(status.getMessage() == null || status.getMessage().length() == 0 ? "群信息加载失败" : status.getMessage());
        }
        if (response == null || response.data == null) {
            throw new IOException("Missing group info data");
        }
        com.yhchat.canary.proto.group.info.Group_data data = response.data;
        return new YhGroupInfo(
                data.group_id,
                data.name,
                data.avatar_url,
                data.avatar_id,
                data.introduction,
                data.member,
                data.create_by,
                data.direct_join != 0,
                data.permisson_level,
                data.history_msg != 0,
                data.category_name,
                data.category_id,
                data.private_ != 0,
                data.do_not_disturb != 0,
                data.community_id,
                data.community_name,
                data.top != 0,
                data.admin,
                data.owner,
                data.limited_msg_type,
                data.recommandation != 0,
                data.my_group_nickname,
                data.group_code,
                data.hide_group_members != 0,
                data.auto_delete_message,
                data.deny_members_upload_to_group_disk != 0
        );
    }

    public static ArrayList<YhGroupMember> parseGroupMembers(byte[] body) throws IOException {
        list_member response = list_member.ADAPTER.decode(body);
        YhApiStatus status = mapGroupStatus(response != null ? response.status : null);
        if (!status.isSuccess()) {
            throw new IOException(status.getMessage() == null || status.getMessage().length() == 0 ? "群成员加载失败" : status.getMessage());
        }
        ArrayList<YhGroupMember> members = new ArrayList<>();
        if (response == null || response.user == null) {
            android.util.Log.d("YHProtoIO", "parseGroupMembers: response.user is null, returning empty list");
            return members;
        }
        android.util.Log.d("YHProtoIO", "parseGroupMembers: user count = " + response.user.size());
        for (int i = 0; i < response.user.size(); i++) {
            com.yhchat.canary.proto.group.User user = response.user.get(i);
            if (user == null || user.user_info == null) {
                continue;
            }
            YhGroupMember member = new YhGroupMember(
                    user.user_info.user_id,
                    user.user_info.name,
                    user.user_info.avatar_url,
                    user.user_info.is_vip != 0,
                    user.permission_level,
                    user.gag_time,
                    user.is_gag != 0
            );
            members.add(member);
            if (i < 3) {
                android.util.Log.d("YHProtoIO", "Member " + i + ": userId=" + user.user_info.user_id + " name=" + user.user_info.name);
            }
        }
        android.util.Log.d("YHProtoIO", "parseGroupMembers: returning " + members.size() + " members");
        return members;
    }

    private static YhConversation mapConversation(ConversationList.ConversationData item) {
        YhConversation conversation = new YhConversation();
        conversation.setChatId(item.chat_id);
        conversation.setChatType(item.chat_type);
        conversation.setName(item.name);
        conversation.setPreviewText(item.chat_content);
        conversation.setTimestampMs(item.timestamp_ms);
        conversation.setUnread(item.unread_message != 0);
        conversation.setMentioned(item.at != 0);
        conversation.setAvatarId(item.avatar_id);
        conversation.setAvatarUrl(item.avatar_url);
        conversation.setDoNotDisturb(item.do_not_disturb != 0);
        conversation.setTimestampSeconds(item.timestamp);
        conversation.setMention(mapMention(item.at_data));
        conversation.setCertificationLevel(item.certification_level);
        return conversation;
    }

    private static YhMention mapMention(ConversationList.ConversationData.AtData atData) {
        if (atData == null) {
            return null;
        }
        YhMention mention = new YhMention();
        mention.setUnknown(atData.unknown);
        mention.setMentionedId(atData.mentioned_id);
        mention.setMentionedName(atData.mentioned_name);
        mention.setMentionedIn(atData.mentioned_in);
        mention.setMentionerId(atData.mentioner_id);
        mention.setMentionerName(atData.mentioner_name);
        mention.setMessageSequence(atData.msg_seq);
        return mention;
    }

    private static ArrayList<YhChatMessage> parseMessages(com.yhchat.canary.proto.Status status, List<Msg> items) throws IOException {
        YhApiStatus apiStatus = mapStatus(status);
        if (apiStatus.getCode() != 1) {
            throw new IOException(apiStatus.getMessage() == null || apiStatus.getMessage().length() == 0 ? "消息加载失败" : apiStatus.getMessage());
        }
        ArrayList<YhChatMessage> result = new ArrayList<>();
        if (items == null) {
            return result;
        }
        for (Msg item : items) {
            if (item == null) {
                continue;
            }
            result.add(mapChatMessage(item));
        }
        return result;
    }

    private static YhChatMessage mapChatMessage(Msg item) {
        Msg.Content content = item.content;
        Msg.Sender sender = item.sender;
        return new YhChatMessage(
                item.msg_id,
                item.msg_seq,
                item.content_type,
                item.direction,
                item.send_time,
                item.edit_time,
                item.msg_delete_time,
                item.quote_msg_id,
                content != null ? content.text : null,
                content != null ? content.quote_msg_text : null,
                content != null ? content.image_url : null,
                content != null ? content.file_name : null,
                content != null ? content.file_url : null,
                content != null ? content.video_url : null,
                content != null ? content.audio_url : null,
                content != null ? content.call_text : null,
                content != null ? content.call_status_text : null,
                content != null ? content.tip : null,
                content != null ? content.width : 0L,
                content != null ? content.height : 0L,
                sender != null ? sender.chat_id : null,
                sender != null ? sender.chat_type : 0,
                sender != null ? sender.name : null,
                sender != null ? sender.avatar_url : null
        );
    }

    private static YhApiStatus mapStatus(Status status) {
        if (status == null) {
            return new YhApiStatus(0, -1, "Missing status field");
        }
        return new YhApiStatus(
                status.number,
                status.code,
                status.msg
        );
    }

    private static YhApiStatus mapStatus(com.yhchat.canary.proto.Status status) {
        if (status == null) {
            return new YhApiStatus(0, -1, "Missing status field");
        }
        return new YhApiStatus(
                status.number,
                status.code,
                status.msg
        );
    }

    private static YhApiStatus mapStatus(yh_user.Status status) {
        if (status == null) {
            return new YhApiStatus(0, -1, "Missing status field");
        }
        return new YhApiStatus(
                status.number,
                status.code,
                status.msg
        );
    }

    private static YhApiStatus mapStatus(yh_bot.Status status) {
        if (status == null) {
            return new YhApiStatus(0, -1, "Missing status field");
        }
        return new YhApiStatus(
                status.request_id,
                status.code,
                status.msg
        );
    }

    private static YhApiStatus mapGroupStatus(com.yhchat.canary.proto.group.Status status) {
        if (status == null) {
            return new YhApiStatus(0, -1, "Missing status field");
        }
        return new YhApiStatus(
                status.request_id,
                status.code,
                status.msg
        );
    }

    private static int resolveAddressBookChatType(String listName, int permissionLevel) {
        String section = listName == null ? "" : listName.toLowerCase(Locale.US);
        if (section.contains("bot") || section.contains("\u673A\u5668")) {
            return YhChatType.BOT;
        }
        if (section.contains("group") || section.contains("\u7FA4")) {
            return YhChatType.GROUP;
        }
        if (section.contains("user") || section.contains("\u7528\u6237")) {
            return YhChatType.USER;
        }
        // permission_level is not a reliable chat type indicator for address-book entries.
        // Default to user to keep contacts visible in TG contacts/forward pickers.
        return YhChatType.USER;
    }

    private static String protoString(String value) {
        return value == null ? "" : value;
    }
}
