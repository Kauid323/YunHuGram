package org.telegram.yh.service;

import org.telegram.yh.YhApiException;
import org.telegram.yh.YhSession;
import org.telegram.yh.YhSessionStore;
import org.telegram.yh.model.YhGroupInfo;
import org.telegram.yh.model.YhGroupMember;
import org.telegram.yh.proto.YhProtoIO;

import java.io.IOException;
import java.util.ArrayList;

public class YhGroupService {

    private final YhApiClient apiClient;
    private final YhSessionStore sessionStore;

    public YhGroupService(YhApiClient apiClient, YhSessionStore sessionStore) {
        this.apiClient = apiClient;
        this.sessionStore = sessionStore;
    }

    public YhGroupInfo getGroupInfo(String groupId) throws IOException {
        YhSession session = requireSession();
        byte[] requestBody = YhProtoIO.buildGroupInfoRequest(groupId);
        byte[] response = apiClient.postProto("v1/group/info", session.getToken(), requestBody);
        return YhProtoIO.parseGroupInfo(response);
    }

    public ArrayList<YhGroupMember> getGroupMembers(String groupId, int size, int page, String keywords) throws IOException {
        YhSession session = requireSession();
        byte[] requestBody = YhProtoIO.buildGroupMembersRequest(groupId, size, page, keywords);
        byte[] response = apiClient.postProto("v1/group/list-member", session.getToken(), requestBody);
        return YhProtoIO.parseGroupMembers(response);
    }

    private YhSession requireSession() throws YhApiException {
        YhSession session = sessionStore.getSession();
        if (session == null || !session.isValid()) {
            throw new YhApiException("YH session is not set");
        }
        return session;
    }
}
