package org.telegram.yh;

import org.telegram.messenger.UserConfig;
import org.telegram.yh.service.YhApiClient;
import org.telegram.yh.service.YhConversationService;
import org.telegram.yh.service.YhGroupService;
import org.telegram.yh.service.YhMessageService;
import org.telegram.yh.service.YhUserService;
import org.telegram.yh.service.YhWebSocketManager;

public class YhApiFacade {

    private static final YhApiFacade[] INSTANCES = new YhApiFacade[UserConfig.MAX_ACCOUNT_COUNT];

    public static YhApiFacade getInstance(int account) {
        YhApiFacade instance = INSTANCES[account];
        if (instance == null) {
            synchronized (YhApiFacade.class) {
                instance = INSTANCES[account];
                if (instance == null) {
                    instance = new YhApiFacade(account);
                    INSTANCES[account] = instance;
                }
            }
        }
        return instance;
    }

    private final int account;
    private final YhSessionStore sessionStore;
    private final YhApiClient apiClient;
    private final YhConversationService conversationService;
    private final YhMessageService messageService;
    private final YhGroupService groupService;
    private final YhUserService userService;
    private final YhWebSocketManager webSocketManager;

    private YhApiFacade(int account) {
        this.account = account;
        sessionStore = new YhSessionStore(account);
        apiClient = new YhApiClient();
        conversationService = new YhConversationService(apiClient, sessionStore);
        messageService = new YhMessageService(apiClient, sessionStore);
        groupService = new YhGroupService(apiClient, sessionStore);
        userService = new YhUserService(apiClient, sessionStore);
        webSocketManager = new YhWebSocketManager(apiClient, sessionStore);
    }

    public int getAccount() {
        return account;
    }

    public void setSession(String token, String userId) {
        sessionStore.saveSession(token, userId);
    }

    public YhSession getSession() {
        return sessionStore.getSession();
    }

    public void clearSession() {
        webSocketManager.disconnect();
        sessionStore.clear();
    }

    public YhConversationService conversations() {
        return conversationService;
    }

    public YhMessageService messages() {
        return messageService;
    }

    public YhGroupService groups() {
        return groupService;
    }

    public YhUserService users() {
        return userService;
    }

    public YhWebSocketManager webSocket() {
        return webSocketManager;
    }
}
