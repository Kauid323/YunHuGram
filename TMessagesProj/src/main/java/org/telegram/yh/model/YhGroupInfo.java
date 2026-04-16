package org.telegram.yh.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YhGroupInfo {

    private final String groupId;
    private final String name;
    private final String avatarUrl;
    private final long avatarId;
    private final String introduction;
    private final long memberCount;
    private final String createBy;
    private final boolean directJoin;
    private final int permissionLevel;
    private final boolean historyMsgEnabled;
    private final String categoryName;
    private final long categoryId;
    private final boolean privateGroup;
    private final boolean doNotDisturb;
    private final long communityId;
    private final String communityName;
    private final boolean top;
    private final List<String> adminIds;
    private final String ownerId;
    private final String limitedMsgType;
    private final boolean recommendation;
    private final String myGroupNickname;
    private final String groupCode;
    private final boolean hideGroupMembers;
    private final long autoDeleteMessage;
    private final boolean denyMembersUploadToGroupDisk;

    public YhGroupInfo(String groupId, String name, String avatarUrl, long avatarId, String introduction,
            long memberCount, String createBy, boolean directJoin, int permissionLevel, boolean historyMsgEnabled,
            String categoryName, long categoryId, boolean privateGroup, boolean doNotDisturb, long communityId,
            String communityName, boolean top, List<String> adminIds, String ownerId, String limitedMsgType,
            boolean recommendation, String myGroupNickname, String groupCode, boolean hideGroupMembers,
            long autoDeleteMessage, boolean denyMembersUploadToGroupDisk) {
        this.groupId = groupId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.avatarId = avatarId;
        this.introduction = introduction;
        this.memberCount = memberCount;
        this.createBy = createBy;
        this.directJoin = directJoin;
        this.permissionLevel = permissionLevel;
        this.historyMsgEnabled = historyMsgEnabled;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.privateGroup = privateGroup;
        this.doNotDisturb = doNotDisturb;
        this.communityId = communityId;
        this.communityName = communityName;
        this.top = top;
        this.adminIds = adminIds == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(adminIds));
        this.ownerId = ownerId;
        this.limitedMsgType = limitedMsgType;
        this.recommendation = recommendation;
        this.myGroupNickname = myGroupNickname;
        this.groupCode = groupCode;
        this.hideGroupMembers = hideGroupMembers;
        this.autoDeleteMessage = autoDeleteMessage;
        this.denyMembersUploadToGroupDisk = denyMembersUploadToGroupDisk;
    }

    public String getGroupId() {
        return groupId;
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

    public String getIntroduction() {
        return introduction;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public String getCreateBy() {
        return createBy;
    }

    public boolean isDirectJoin() {
        return directJoin;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public boolean isHistoryMsgEnabled() {
        return historyMsgEnabled;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public boolean isPrivateGroup() {
        return privateGroup;
    }

    public boolean isDoNotDisturb() {
        return doNotDisturb;
    }

    public long getCommunityId() {
        return communityId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public boolean isTop() {
        return top;
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getLimitedMsgType() {
        return limitedMsgType;
    }

    public boolean isRecommendation() {
        return recommendation;
    }

    public String getMyGroupNickname() {
        return myGroupNickname;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public boolean isHideGroupMembers() {
        return hideGroupMembers;
    }

    public long getAutoDeleteMessage() {
        return autoDeleteMessage;
    }

    public boolean isDenyMembersUploadToGroupDisk() {
        return denyMembersUploadToGroupDisk;
    }
}
