package org.telegram.yh.user;

import org.telegram.yh.model.YhBotDetail;
import org.telegram.yh.model.YhSelfUser;
import org.telegram.yh.model.YhUserDetail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class YhUserViewModel {

    private final YhUserRepository repository;
    private YhSelfUser selfUser;
    private boolean selfLoading;
    private final Map<String, YhUserDetail> userDetails = new HashMap<>();
    private final Map<String, YhBotDetail> botDetails = new HashMap<>();
    private final Set<String> loadingUserDetails = new HashSet<>();
    private final Set<String> loadingBotDetails = new HashSet<>();

    public YhUserViewModel(int account) {
        repository = new YhUserRepository(account);
    }

    public YhSelfUser getUser() {
        return selfUser;
    }

    public boolean isLoading() {
        return selfLoading;
    }

    public YhUserDetail getUserDetail(String userId) {
        return userDetails.get(userId);
    }

    public YhBotDetail getBotDetail(String botId) {
        return botDetails.get(botId);
    }

    public void loadSelfUser(boolean force, YhUserRepository.Callback callback) {
        if (selfLoading) {
            return;
        }
        if (!force && selfUser != null) {
            callback.onSuccess(selfUser);
            return;
        }
        selfLoading = true;
        repository.loadSelfUser(new YhUserRepository.Callback() {
            @Override
            public void onSuccess(YhSelfUser userResult) {
                selfLoading = false;
                selfUser = userResult;
                callback.onSuccess(userResult);
            }

            @Override
            public void onError(String error) {
                selfLoading = false;
                callback.onError(error);
            }
        });
    }

    public void loadUserDetail(String userId, boolean force, YhUserRepository.DetailCallback<YhUserDetail> callback) {
        if (userId == null || userId.length() == 0) {
            callback.onError("用户ID为空");
            return;
        }
        if (loadingUserDetails.contains(userId)) {
            return;
        }
        YhUserDetail cached = userDetails.get(userId);
        if (!force && cached != null) {
            callback.onSuccess(cached);
            return;
        }
        loadingUserDetails.add(userId);
        repository.loadUserDetail(userId, new YhUserRepository.DetailCallback<YhUserDetail>() {
            @Override
            public void onSuccess(YhUserDetail value) {
                loadingUserDetails.remove(userId);
                userDetails.put(userId, value);
                callback.onSuccess(value);
            }

            @Override
            public void onError(String error) {
                loadingUserDetails.remove(userId);
                callback.onError(error);
            }
        });
    }

    public void loadBotDetail(String botId, boolean force, YhUserRepository.DetailCallback<YhBotDetail> callback) {
        if (botId == null || botId.length() == 0) {
            callback.onError("机器人ID为空");
            return;
        }
        if (loadingBotDetails.contains(botId)) {
            return;
        }
        YhBotDetail cached = botDetails.get(botId);
        if (!force && cached != null) {
            callback.onSuccess(cached);
            return;
        }
        loadingBotDetails.add(botId);
        repository.loadBotDetail(botId, new YhUserRepository.DetailCallback<YhBotDetail>() {
            @Override
            public void onSuccess(YhBotDetail value) {
                loadingBotDetails.remove(botId);
                botDetails.put(botId, value);
                callback.onSuccess(value);
            }

            @Override
            public void onError(String error) {
                loadingBotDetails.remove(botId);
                callback.onError(error);
            }
        });
    }
}
