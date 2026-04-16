package org.telegram.yh.model;

public class YhSelfUser {

    private final String id;
    private final String name;
    private final String avatarUrl;
    private final long avatarId;
    private final String phone;
    private final String email;
    private final double coin;
    private final boolean vip;
    private final long vipExpiredTime;
    private final String invitationCode;

    public YhSelfUser(String id, String name, String avatarUrl, long avatarId, String phone, String email, double coin, boolean vip, long vipExpiredTime, String invitationCode) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.avatarId = avatarId;
        this.phone = phone;
        this.email = email;
        this.coin = coin;
        this.vip = vip;
        this.vipExpiredTime = vipExpiredTime;
        this.invitationCode = invitationCode;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public long getAvatarId() {
        return avatarId;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public double getCoin() {
        return coin;
    }

    public boolean isVip() {
        return vip;
    }

    public long getVipExpiredTime() {
        return vipExpiredTime;
    }

    public String getInvitationCode() {
        return invitationCode;
    }
}
