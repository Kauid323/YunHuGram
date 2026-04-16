package org.telegram.yh.model;

public class YhBotDetail {

    private final String botId;
    private final String name;
    private final long nameId;
    private final String avatarUrl;
    private final long avatarId;
    private final String introduction;
    private final String createBy;
    private final long createTime;
    private final long headcount;
    private final boolean privateBot;
    private final boolean stopped;
    private final boolean alwaysAgree;
    private final boolean canNotDelete;
    private final boolean doNotDisturb;
    private final boolean top;
    private final boolean groupLimit;

    public YhBotDetail(String botId, String name, long nameId, String avatarUrl, long avatarId, String introduction,
            String createBy, long createTime, long headcount, boolean privateBot, boolean stopped,
            boolean alwaysAgree, boolean canNotDelete, boolean doNotDisturb, boolean top, boolean groupLimit) {
        this.botId = botId;
        this.name = name;
        this.nameId = nameId;
        this.avatarUrl = avatarUrl;
        this.avatarId = avatarId;
        this.introduction = introduction;
        this.createBy = createBy;
        this.createTime = createTime;
        this.headcount = headcount;
        this.privateBot = privateBot;
        this.stopped = stopped;
        this.alwaysAgree = alwaysAgree;
        this.canNotDelete = canNotDelete;
        this.doNotDisturb = doNotDisturb;
        this.top = top;
        this.groupLimit = groupLimit;
    }

    public String getBotId() {
        return botId;
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

    public String getIntroduction() {
        return introduction;
    }

    public String getCreateBy() {
        return createBy;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getHeadcount() {
        return headcount;
    }

    public boolean isPrivateBot() {
        return privateBot;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isAlwaysAgree() {
        return alwaysAgree;
    }

    public boolean isCanNotDelete() {
        return canNotDelete;
    }

    public boolean isDoNotDisturb() {
        return doNotDisturb;
    }

    public boolean isTop() {
        return top;
    }

    public boolean isGroupLimit() {
        return groupLimit;
    }
}
