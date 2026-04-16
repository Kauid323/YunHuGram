package org.telegram.yh.user;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.yh.YhApiFacade;
import org.telegram.yh.model.YhBotDetail;
import org.telegram.yh.model.YhSelfUser;
import org.telegram.yh.model.YhUserDetail;

public class YhUserRepository {

    public interface Callback {
        void onSuccess(YhSelfUser user);
        void onError(String error);
    }

    public interface DetailCallback<T> {
        void onSuccess(T value);
        void onError(String error);
    }

    private final int account;

    public YhUserRepository(int account) {
        this.account = account;
    }

    public void loadSelfUser(Callback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                YhSelfUser user = YhApiFacade.getInstance(account).users().getSelfUser();
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(user));
            } catch (Exception e) {
                String error = e.getMessage() == null ? "加载用户信息失败" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }

    public void loadUserDetail(String userId, DetailCallback<YhUserDetail> callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                YhUserDetail user = YhApiFacade.getInstance(account).users().getUserDetail(userId);
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(user));
            } catch (Exception e) {
                String error = e.getMessage() == null ? "加载用户详情失败" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }

    public void loadBotDetail(String botId, DetailCallback<YhBotDetail> callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                YhBotDetail bot = YhApiFacade.getInstance(account).users().getBotDetail(botId);
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(bot));
            } catch (Exception e) {
                String error = e.getMessage() == null ? "加载机器人详情失败" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }
}
