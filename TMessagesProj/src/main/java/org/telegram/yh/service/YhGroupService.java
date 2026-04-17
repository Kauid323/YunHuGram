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
        android.util.Log.d("YHGroupService", "getGroupMembers: groupId=" + groupId + " size=" + size + " page=" + page + " keywords=" + keywords);
        byte[] requestBody = YhProtoIO.buildGroupMembersRequest(groupId, size, page, keywords);
        android.util.Log.d("YHGroupService", "Request body length: " + requestBody.length);
        // Log first few bytes as hex to verify protobuf encoding
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < Math.min(requestBody.length, 50); i++) {
            hex.append(String.format("%02X ", requestBody[i]));
        }
        android.util.Log.d("YHGroupService", "Request hex (first 50 bytes): " + hex.toString());
        byte[] response = apiClient.postProto("v1/group/list-member", session.getToken(), requestBody);
        android.util.Log.d("YHGroupService", "Response body length: " + response.length);
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
