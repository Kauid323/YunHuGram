package org.telegram.yh.service;

import org.telegram.yh.YhApiException;
import org.telegram.yh.YhSession;
import org.telegram.yh.YhSessionStore;
import org.telegram.yh.model.YhBotDetail;
import org.telegram.yh.model.YhAddressBookEntry;
import org.telegram.yh.model.YhSelfUser;
import org.telegram.yh.model.YhUserDetail;
import org.telegram.yh.proto.YhProtoIO;

import java.io.IOException;
import java.util.ArrayList;

public class YhUserService {

    private final YhApiClient apiClient;
    private final YhSessionStore sessionStore;

    public YhUserService(YhApiClient apiClient, YhSessionStore sessionStore) {
        this.apiClient = apiClient;
        this.sessionStore = sessionStore;
    }

    public YhSelfUser getSelfUser() throws IOException {
        YhSession session = requireSession();
        byte[] response = apiClient.getProto("v1/user/info", session.getToken());
        return YhProtoIO.parseSelfUser(response);
    }

    public YhUserDetail getUserDetail(String userId) throws IOException {
        YhSession session = requireSession();
        byte[] request = YhProtoIO.buildUserDetailRequest(userId);
        byte[] response = apiClient.postProto("v1/user/get-user", session.getToken(), request);
        return YhProtoIO.parseUserDetail(response);
    }

    public YhBotDetail getBotDetail(String botId) throws IOException {
        YhSession session = requireSession();
        byte[] request = YhProtoIO.buildBotInfoRequest(botId);
        byte[] response = apiClient.postProto("v1/bot/bot-info", session.getToken(), request);
        return YhProtoIO.parseBotDetail(response);
    }

    public ArrayList<YhAddressBookEntry> getAddressBookList() throws IOException {
        YhSession session = requireSession();
        byte[] request = YhProtoIO.buildAddressBookListRequest(String.valueOf(System.currentTimeMillis()));
        byte[] response = apiClient.postProto("v1/friend/address-book-list", session.getToken(), request);
        return YhProtoIO.parseAddressBookList(response);
    }

    private YhSession requireSession() throws YhApiException {
        YhSession session = sessionStore.getSession();
        if (session == null || !session.isValid()) {
            throw new YhApiException("YH session is not set");
        }
        return session;
    }
}
