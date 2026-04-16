package org.telegram.yh.model;

public class YhTextMessageSpec {

    private final String chatId;
    private final int chatType;
    private final String text;
    private final String messageId;
    private final String quoteMessageId;
    private final String quoteMessageText;

    public YhTextMessageSpec(String chatId, int chatType, String text, String messageId, String quoteMessageId, String quoteMessageText) {
        this.chatId = chatId;
        this.chatType = chatType;
        this.text = text;
        this.messageId = messageId;
        this.quoteMessageId = quoteMessageId;
        this.quoteMessageText = quoteMessageText;
    }

    public String getChatId() {
        return chatId;
    }

    public int getChatType() {
        return chatType;
    }

    public String getText() {
        return text;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getQuoteMessageId() {
        return quoteMessageId;
    }

    public String getQuoteMessageText() {
        return quoteMessageText;
    }
}
