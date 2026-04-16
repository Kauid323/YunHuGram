package org.telegram.yh.group;

import android.text.TextUtils;

import org.telegram.yh.model.YhGroupInfo;
import org.telegram.yh.model.YhGroupMember;

import java.util.ArrayList;

public class YhGroupViewModel {

    public interface MembersCallback {
        void onSuccess(ArrayList<YhGroupMember> members, boolean reset, boolean hasMore, int loadedPage);
        void onError(String error);
    }

    private final YhGroupRepository repository;
    private boolean loading;
    private String currentGroupId;
    private YhGroupInfo cachedGroupInfo;
    private boolean membersLoading;
    private String currentMembersGroupId;
    private int currentMembersPage;
    private boolean hasMoreMembers = true;
    private final ArrayList<YhGroupMember> cachedMembers = new ArrayList<>();

    public YhGroupViewModel(int account) {
        repository = new YhGroupRepository(account);
    }

    public boolean isLoading() {
        return loading;
    }

    public YhGroupInfo getCachedGroupInfo() {
        return cachedGroupInfo;
    }

    public boolean isMembersLoading() {
        return membersLoading;
    }

    public boolean hasMoreMembers() {
        return hasMoreMembers;
    }

    public ArrayList<YhGroupMember> getCachedMembers() {
        return new ArrayList<>(cachedMembers);
    }

    public void loadGroupInfo(String groupId, boolean force, YhGroupRepository.Callback callback) {
        if (loading || TextUtils.isEmpty(groupId)) {
            return;
        }
        if (!force && TextUtils.equals(currentGroupId, groupId) && cachedGroupInfo != null) {
            callback.onSuccess(cachedGroupInfo);
            return;
        }
        loading = true;
        repository.loadGroupInfo(groupId, new YhGroupRepository.Callback() {
            @Override
            public void onSuccess(YhGroupInfo groupInfo) {
                loading = false;
                currentGroupId = groupId;
                cachedGroupInfo = groupInfo;
                callback.onSuccess(groupInfo);
            }

            @Override
            public void onError(String error) {
                loading = false;
                callback.onError(error);
            }
        });
    }

    public void loadGroupMembers(String groupId, int page, boolean reset, MembersCallback callback) {
        if (membersLoading || TextUtils.isEmpty(groupId)) {
            return;
        }
        boolean groupChanged = !TextUtils.equals(currentMembersGroupId, groupId);
        if (groupChanged) {
            resetMembersState();
        }
        currentMembersGroupId = groupId;
        if (reset) {
            currentMembersPage = 0;
            hasMoreMembers = true;
            cachedMembers.clear();
        }
        if (!reset && !hasMoreMembers) {
            callback.onSuccess(new ArrayList<>(), false, false, currentMembersPage);
            return;
        }
        int normalizedPage = Math.max(1, page);
        if (!reset) {
            normalizedPage = Math.max(normalizedPage, currentMembersPage + 1);
        }
        final int requestedPage = normalizedPage;
        membersLoading = true;
        repository.loadGroupMembers(groupId, requestedPage, YhGroupRepository.DEFAULT_GROUP_MEMBER_PAGE_SIZE, "", new YhGroupRepository.MembersCallback() {
            @Override
            public void onSuccess(ArrayList<YhGroupMember> members) {
                membersLoading = false;
                currentMembersGroupId = groupId;
                if (requestedPage == 1) {
                    cachedMembers.clear();
                }
                if (members != null && !members.isEmpty()) {
                    cachedMembers.addAll(members);
                }
                hasMoreMembers = members != null && members.size() >= YhGroupRepository.DEFAULT_GROUP_MEMBER_PAGE_SIZE;
                currentMembersPage = requestedPage;
                callback.onSuccess(members == null ? new ArrayList<>() : members, requestedPage == 1, hasMoreMembers, requestedPage);
            }

            @Override
            public void onError(String error) {
                membersLoading = false;
                callback.onError(error);
            }
        });
    }

    private void resetMembersState() {
        currentMembersGroupId = null;
        currentMembersPage = 0;
        hasMoreMembers = true;
        cachedMembers.clear();
    }
}
