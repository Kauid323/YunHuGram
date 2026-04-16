package org.telegram.yh.model;

public class YhMessageListSpec {

    private final String chatId;
    private final int chatType;
    private final int messageCount;
    private final String messageId;

    public YhMessageListSpec(String chatId, int chatType, int messageCount, String messageId) {
        this.chatId = chatId;
        this.chatType = chatType;
        this.messageCount = messageCount;
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public int getChatType() {
        return chatType;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getMessageId() {
        return messageId;
    }
}
