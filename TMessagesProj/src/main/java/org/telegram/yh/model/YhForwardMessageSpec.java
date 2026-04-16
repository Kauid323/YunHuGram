package org.telegram.yh.model;

public class YhForwardMessageSpec {

    private final String messageId;
    private final int chatType;

    public YhForwardMessageSpec(String messageId, int chatType) {
        this.messageId = messageId;
        this.chatType = chatType;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getChatType() {
        return chatType;
    }
}
