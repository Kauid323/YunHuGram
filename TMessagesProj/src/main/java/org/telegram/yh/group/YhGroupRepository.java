package org.telegram.yh.group;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.yh.YhApiFacade;
import org.telegram.yh.model.YhGroupInfo;
import org.telegram.yh.model.YhGroupMember;

import java.util.ArrayList;

public class YhGroupRepository {

    public static final int DEFAULT_GROUP_MEMBER_PAGE_SIZE = 50;

    public interface Callback {
        void onSuccess(YhGroupInfo groupInfo);
        void onError(String error);
    }

    public interface MembersCallback {
        void onSuccess(ArrayList<YhGroupMember> members);
        void onError(String error);
    }

    private final int account;

    public YhGroupRepository(int account) {
        this.account = account;
    }

    public void loadGroupInfo(String groupId, Callback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                YhGroupInfo groupInfo = YhApiFacade.getInstance(account).groups().getGroupInfo(groupId);
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(groupInfo));
            } catch (Exception e) {
                String error = e == null || e.getMessage() == null || e.getMessage().length() == 0 ? "群信息加载失败" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }

    public void loadGroupMembers(String groupId, int page, int size, String keywords, MembersCallback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                ArrayList<YhGroupMember> members = YhApiFacade.getInstance(account)
                        .groups()
                        .getGroupMembers(groupId, size, page, keywords);
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(members));
            } catch (Exception e) {
                String error = e == null || e.getMessage() == null || e.getMessage().length() == 0 ? "群成员加载失败" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }
}
