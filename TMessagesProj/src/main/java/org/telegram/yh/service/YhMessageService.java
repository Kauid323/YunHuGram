package org.telegram.yh.service;

import com.google.gson.JsonObject;

import org.telegram.yh.YhApiException;
import org.telegram.yh.YhApiStatus;
import org.telegram.yh.YhSession;
import org.telegram.yh.YhSessionStore;
import org.telegram.yh.model.YhForwardMessageSpec;
import org.telegram.yh.model.YhForwardReceiveTarget;
import org.telegram.yh.model.YhMessageListSpec;
import org.telegram.yh.model.YhTextMessageSpec;
import org.telegram.yh.proto.YhProtoIO;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YhMessageService {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final YhApiClient apiClient;
    private final YhSessionStore sessionStore;

    public YhMessageService(YhApiClient apiClient, YhSessionStore sessionStore) {
        this.apiClient = apiClient;
        this.sessionStore = sessionStore;
    }

    public YhApiStatus sendTextMessage(YhTextMessageSpec spec) throws IOException {
        YhSession session = requireSession();
        YhTextMessageSpec resolvedSpec = ensureMessageId(spec);
        byte[] requestBody = YhProtoIO.buildSendTextMessage(resolvedSpec);
        byte[] response = apiClient.postProto("v1/msg/send-message", session.getToken(), requestBody);
        return YhProtoIO.parseStatusResponse(response);
    }

    public byte[] listMessagesRaw(YhMessageListSpec spec) throws IOException {
        YhSession session = requireSession();
        byte[] requestBody = YhProtoIO.buildListMessagesRequest(spec);
        return apiClient.postProto("v1/msg/list-message", session.getToken(), requestBody);
    }

    public byte[] listMessagesBySeqRaw(String chatId, int chatType, long messageStart) throws IOException {
        YhSession session = requireSession();
        byte[] requestBody = YhProtoIO.buildListMessagesBySeqRequest(chatId, chatType, messageStart);
        return apiClient.postProto("v1/msg/list-message-by-seq", session.getToken(), requestBody);
    }

    public YhApiStatus forwardMessage(YhForwardMessageSpec messageSpec, List<YhForwardReceiveTarget> receiveTargets) throws IOException {
        YhSession session = requireSession();
        if (messageSpec == null || messageSpec.getChatType() <= 0 || messageSpec.getMessageId() == null || messageSpec.getMessageId().length() == 0) {
            throw new YhApiException("Invalid forward message spec");
        }
        if (receiveTargets == null || receiveTargets.isEmpty()) {
            throw new YhApiException("Forward targets are empty");
        }

        ForwardMessageBody requestBody = new ForwardMessageBody(
                messageSpec.getMessageId(),
                messageSpec.getChatType(),
                receiveTargets
        );
        byte[] response = apiClient.postJson("v1/msg/msg-forward", session.getToken(), requestBody);
        JsonObject object = apiClient.getGson().fromJson(new String(response, UTF_8), JsonObject.class);
        int code = object != null && object.has("code") ? object.get("code").getAsInt() : -1;
        String message = object != null && object.has("msg") ? object.get("msg").getAsString() : "Unknown response";
        return new YhApiStatus(0L, code, message);
    }

    private YhTextMessageSpec ensureMessageId(YhTextMessageSpec spec) {
        if (spec.getMessageId() != null && spec.getMessageId().length() > 0) {
            return spec;
        }
        return new YhTextMessageSpec(
                spec.getChatId(),
                spec.getChatType(),
                spec.getText(),
                UUID.randomUUID().toString().replace("-", ""),
                spec.getQuoteMessageId(),
                spec.getQuoteMessageText()
        );
    }

    private YhSession requireSession() throws YhApiException {
        YhSession session = sessionStore.getSession();
        if (session == null || !session.isValid()) {
            throw new YhApiException("YH session is not set");
        }
        return session;
    }

    private static class ForwardMessageBody {
        private final String msgId;
        private final int chatType;
        private final List<ForwardTargetBody> receive;

        private ForwardMessageBody(String msgId, int chatType, List<YhForwardReceiveTarget> receiveTargets) {
            this.msgId = msgId;
            this.chatType = chatType;
            this.receive = new ArrayList<>();
            for (int i = 0; i < receiveTargets.size(); i++) {
                YhForwardReceiveTarget target = receiveTargets.get(i);
                if (target == null || target.getChatType() <= 0 || target.getChatId() == null || target.getChatId().length() == 0) {
                    continue;
                }
                this.receive.add(new ForwardTargetBody(target.getChatId(), target.getChatType()));
            }
        }
    }

    private static class ForwardTargetBody {
        private final String chatId;
        private final int chatType;

        private ForwardTargetBody(String chatId, int chatType) {
            this.chatId = chatId;
            this.chatType = chatType;
        }
    }
}
