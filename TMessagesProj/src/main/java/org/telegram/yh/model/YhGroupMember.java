package org.telegram.yh.model;

public class YhGroupMember {

    private final String userId;
    private final String name;
    private final String avatarUrl;
    private final boolean vip;
    private final int permissionLevel;
    private final long gagTime;
    private final boolean gag;

    public YhGroupMember(String userId, String name, String avatarUrl, boolean vip, int permissionLevel,
            long gagTime, boolean gag) {
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.vip = vip;
        this.permissionLevel = permissionLevel;
        this.gagTime = gagTime;
        this.gag = gag;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isVip() {
        return vip;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public long getGagTime() {
        return gagTime;
    }

    public boolean isGag() {
        return gag;
    }
}
