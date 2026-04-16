package org.telegram.yh.service;

import com.google.gson.JsonObject;

import org.telegram.yh.YhApiException;
import org.telegram.yh.YhApiStatus;
import org.telegram.yh.YhSession;
import org.telegram.yh.YhSessionStore;
import org.telegram.yh.model.YhConversationList;
import org.telegram.yh.proto.YhProtoIO;

import java.io.IOException;
import java.nio.charset.Charset;

public class YhConversationService {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final YhApiClient apiClient;
    private final YhSessionStore sessionStore;

    public YhConversationService(YhApiClient apiClient, YhSessionStore sessionStore) {
        this.apiClient = apiClient;
        this.sessionStore = sessionStore;
    }

    public YhConversationList listConversations() throws IOException {
        return YhProtoIO.parseConversationList(listConversationsRaw());
    }

    public byte[] listConversationsRaw() throws IOException {
        YhSession session = requireSession();
        return apiClient.postProto("v1/conversation/list", session.getToken(), new byte[0]);
    }

    public void dismissNotification(String chatId) throws IOException {
        YhSession session = requireSession();
        byte[] response = apiClient.postJson("v1/conversation/dismiss-notification", session.getToken(), new DismissNotificationBody(chatId));
        JsonObject object = apiClient.getGson().fromJson(new String(response, UTF_8), JsonObject.class);
        int code = object != null && object.has("code") ? object.get("code").getAsInt() : -1;
        String message = object != null && object.has("msg") ? object.get("msg").getAsString() : "Unknown response";
        if (code != 1) {
            throw new YhApiException(code, message);
        }
    }

    private YhSession requireSession() throws YhApiException {
        YhSession session = sessionStore.getSession();
        if (session == null || !session.isValid()) {
            throw new YhApiException("YH session is not set");
        }
        return session;
    }

    private static class DismissNotificationBody {
        private final String chatId;

        private DismissNotificationBody(String chatId) {
            this.chatId = chatId;
        }
    }
}
