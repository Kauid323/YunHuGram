package org.telegram.yh.model;

public class YhForwardReceiveTarget {

    private final String chatId;
    private final int chatType;

    public YhForwardReceiveTarget(String chatId, int chatType) {
        this.chatId = chatId;
        this.chatType = chatType;
    }

    public String getChatId() {
        return chatId;
    }

    public int getChatType() {
        return chatType;
    }
}
