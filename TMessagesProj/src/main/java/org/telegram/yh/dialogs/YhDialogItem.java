package org.telegram.yh.dialogs;

import org.telegram.yh.model.YhConversation;

public class YhDialogItem {

    private final String chatId;
    private final int chatType;
    private final String title;
    private final String previewText;
    private final long timestampSeconds;
    private final boolean unread;
    private final boolean muted;
    private final boolean verified;
    private final String avatarUrl;

    public YhDialogItem(String chatId, int chatType, String title, String previewText, long timestampSeconds, boolean unread, boolean muted, boolean verified, String avatarUrl) {
        this.chatId = chatId;
        this.chatType = chatType;
        this.title = title;
        this.previewText = previewText;
        this.timestampSeconds = timestampSeconds;
        this.unread = unread;
        this.muted = muted;
        this.verified = verified;
        this.avatarUrl = avatarUrl;
    }

    public static YhDialogItem fromConversation(YhConversation conversation) {
        long timestampSeconds = conversation.getTimestampSeconds();
        if (timestampSeconds <= 0 && conversation.getTimestampMs() > 0) {
            timestampSeconds = conversation.getTimestampMs() / 1000L;
        }
        return new YhDialogItem(
                conversation.getChatId(),
                conversation.getChatType(),
                conversation.getName(),
                conversation.getPreviewText(),
                timestampSeconds,
                conversation.isUnread(),
                conversation.isDoNotDisturb(),
                conversation.getCertificationLevel() > 0,
                conversation.getAvatarUrl()
        );
    }

    public String getChatId() {
        return chatId;
    }

    public int getChatType() {
        return chatType;
    }

    public String getTitle() {
        return title;
    }

    public String getPreviewText() {
        return previewText;
    }

    public long getTimestampSeconds() {
        return timestampSeconds;
    }

    public boolean isUnread() {
        return unread;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public long getStableKey() {
        long typePart = ((long) chatType) << 32;
        long chatPart = chatId == null ? 0L : chatId.hashCode() & 0xffffffffL;
        return typePart ^ chatPart;
    }

    public int getDisplayId() {
        int value = (int) getStableKey();
        if (value == 0) {
            return 1;
        }
        return value;
    }
}
