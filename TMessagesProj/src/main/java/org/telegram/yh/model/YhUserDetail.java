package org.telegram.yh.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YhUserDetail {

    private final String id;
    private final String name;
    private final long nameId;
    private final String avatarUrl;
    private final long avatarId;
    private final List<String> medals;
    private final String registerTime;
    private final long banTime;
    private final int onlineDay;
    private final int continuousOnlineDay;
    private final boolean vip;
    private final long vipExpiredTime;
    private final String remarkName;
    private final String phoneNumber;
    private final String extraRemark;
    private final String lastActiveTime;
    private final String introduction;
    private final int gender;
    private final long birthday;
    private final String city;
    private final String district;
    private final String address;
    private final String ipGeo;

    public YhUserDetail(String id, String name, long nameId, String avatarUrl, long avatarId, List<String> medals,
            String registerTime, long banTime, int onlineDay, int continuousOnlineDay, boolean vip,
            long vipExpiredTime, String remarkName, String phoneNumber, String extraRemark, String lastActiveTime,
            String introduction, int gender, long birthday, String city, String district, String address,
            String ipGeo) {
        this.id = id;
        this.name = name;
        this.nameId = nameId;
        this.avatarUrl = avatarUrl;
        this.avatarId = avatarId;
        this.medals = medals == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(medals));
        this.registerTime = registerTime;
        this.banTime = banTime;
        this.onlineDay = onlineDay;
        this.continuousOnlineDay = continuousOnlineDay;
        this.vip = vip;
        this.vipExpiredTime = vipExpiredTime;
        this.remarkName = remarkName;
        this.phoneNumber = phoneNumber;
        this.extraRemark = extraRemark;
        this.lastActiveTime = lastActiveTime;
        this.introduction = introduction;
        this.gender = gender;
        this.birthday = birthday;
        this.city = city;
        this.district = district;
        this.address = address;
        this.ipGeo = ipGeo;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getNameId() {
        return nameId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public long getAvatarId() {
        return avatarId;
    }

    public List<String> getMedals() {
        return medals;
    }

    public String getRegisterTime() {
        return registerTime;
    }

    public long getBanTime() {
        return banTime;
    }

    public int getOnlineDay() {
        return onlineDay;
    }

    public int getContinuousOnlineDay() {
        return continuousOnlineDay;
    }

    public boolean isVip() {
        return vip;
    }

    public long getVipExpiredTime() {
        return vipExpiredTime;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getExtraRemark() {
        return extraRemark;
    }

    public String getLastActiveTime() {
        return lastActiveTime;
    }

    public String getIntroduction() {
        return introduction;
    }

    public int getGender() {
        return gender;
    }

    public long getBirthday() {
        return birthday;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getAddress() {
        return address;
    }

    public String getIpGeo() {
        return ipGeo;
    }
}
