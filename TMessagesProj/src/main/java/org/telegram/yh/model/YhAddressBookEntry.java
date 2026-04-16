package org.telegram.yh.model;

public class YhAddressBookEntry {

    private final String listName;
    private final String chatId;
    private final int chatType;
    private final String name;
    private final String avatarUrl;
    private final int permissionLevel;
    private final boolean noDisturb;

    public YhAddressBookEntry(String listName, String chatId, int chatType, String name, String avatarUrl,
            int permissionLevel, boolean noDisturb) {
        this.listName = listName;
        this.chatId = chatId;
        this.chatType = chatType;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.permissionLevel = permissionLevel;
        this.noDisturb = noDisturb;
    }

    public String getListName() {
        return listName;
    }

    public String getChatId() {
        return chatId;
    }

    public int getChatType() {
        return chatType;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public boolean isNoDisturb() {
        return noDisturb;
    }
}
