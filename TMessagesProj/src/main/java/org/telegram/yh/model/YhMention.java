package org.telegram.yh.model;

public class YhMention {

    private long unknown;
    private String mentionedId;
    private String mentionedName;
    private String mentionedIn;
    private String mentionerId;
    private String mentionerName;
    private long messageSequence;

    public long getUnknown() {
        return unknown;
    }

    public void setUnknown(long unknown) {
        this.unknown = unknown;
    }

    public String getMentionedId() {
        return mentionedId;
    }

    public void setMentionedId(String mentionedId) {
        this.mentionedId = mentionedId;
    }

    public String getMentionedName() {
        return mentionedName;
    }

    public void setMentionedName(String mentionedName) {
        this.mentionedName = mentionedName;
    }

    public String getMentionedIn() {
        return mentionedIn;
    }

    public void setMentionedIn(String mentionedIn) {
        this.mentionedIn = mentionedIn;
    }

    public String getMentionerId() {
        return mentionerId;
    }

    public void setMentionerId(String mentionerId) {
        this.mentionerId = mentionerId;
    }

    public String getMentionerName() {
        return mentionerName;
    }

    public void setMentionerName(String mentionerName) {
        this.mentionerName = mentionerName;
    }

    public long getMessageSequence() {
        return messageSequence;
    }

    public void setMessageSequence(long messageSequence) {
        this.messageSequence = messageSequence;
    }
}
