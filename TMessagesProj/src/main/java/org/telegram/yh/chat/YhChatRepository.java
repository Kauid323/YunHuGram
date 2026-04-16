package org.telegram.yh.chat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.yh.YhApiFacade;
import org.telegram.yh.YhApiStatus;
import org.telegram.yh.model.YhForwardMessageSpec;
import org.telegram.yh.model.YhForwardReceiveTarget;
import org.telegram.yh.model.YhMessageListSpec;
import org.telegram.yh.model.YhTextMessageSpec;
import org.telegram.yh.proto.YhProtoIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class YhChatRepository {

    public static final int DEFAULT_MESSAGE_COUNT = 20;

    public interface Callback {
        void onSuccess(ArrayList<YhChatMessage> messages);
        void onError(String error);
    }

    public interface SendCallback {
        void onSuccess();
        void onError(String error);
    }

    private final int account;

    public YhChatRepository(int account) {
        this.account = account;
    }

    public void loadInitialMessages(String chatId, int chatType, Callback callback) {
        loadMessages(chatId, chatType, null, callback);
    }

    public void loadMoreMessages(String chatId, int chatType, String oldestMessageId, Callback callback) {
        loadMessages(chatId, chatType, oldestMessageId, callback);
    }

    public void sendTextMessage(String chatId, int chatType, String text, String messageId, String quoteMessageId, String quoteMessageText, SendCallback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                YhApiStatus status = YhApiFacade.getInstance(account)
                        .messages()
                        .sendTextMessage(new YhTextMessageSpec(chatId, chatType, text, messageId, quoteMessageId, quoteMessageText));
                if (status == null || !status.isSuccess()) {
                    String error = status == null || status.getMessage() == null || status.getMessage().length() == 0
                            ? "Message send failed"
                            : status.getMessage();
                    AndroidUtilities.runOnUIThread(() -> callback.onError(error));
                    return;
                }
                AndroidUtilities.runOnUIThread(callback::onSuccess);
            } catch (Exception e) {
                String error = e == null || e.getMessage() == null || e.getMessage().length() == 0 ? "Message send failed" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }

    public void forwardMessages(ArrayList<YhForwardMessageSpec> messageSpecs, ArrayList<YhForwardReceiveTarget> receiveTargets, SendCallback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                if (messageSpecs == null || messageSpecs.isEmpty()) {
                    AndroidUtilities.runOnUIThread(() -> callback.onError("Forward messages are empty"));
                    return;
                }
                if (receiveTargets == null || receiveTargets.isEmpty()) {
                    AndroidUtilities.runOnUIThread(() -> callback.onError("Forward targets are empty"));
                    return;
                }
                for (int i = 0; i < messageSpecs.size(); i++) {
                    YhForwardMessageSpec spec = messageSpecs.get(i);
                    if (spec == null) {
                        continue;
                    }
                    YhApiStatus status = YhApiFacade.getInstance(account)
                            .messages()
                            .forwardMessage(spec, receiveTargets);
                    if (status == null || !status.isSuccess()) {
                        String error = status == null || status.getMessage() == null || status.getMessage().length() == 0
                                ? "Message forward failed"
                                : status.getMessage();
                        AndroidUtilities.runOnUIThread(() -> callback.onError(error));
                        return;
                    }
                }
                AndroidUtilities.runOnUIThread(callback::onSuccess);
            } catch (Exception e) {
                String error = e == null || e.getMessage() == null || e.getMessage().length() == 0 ? "Message forward failed" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }

    private void loadMessages(String chatId, int chatType, String oldestMessageId, Callback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                byte[] body = YhApiFacade.getInstance(account)
                        .messages()
                        .listMessagesRaw(new YhMessageListSpec(chatId, chatType, DEFAULT_MESSAGE_COUNT, oldestMessageId));
                ArrayList<YhChatMessage> messages = YhProtoIO.parseMessageList(body);
                Collections.sort(messages, Comparator.comparingLong(YhChatRepository::sortKey).reversed());
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                String error = e == null || e.getMessage() == null || e.getMessage().length() == 0 ? "Message load failed" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }

    private static long sortKey(YhChatMessage message) {
        if (message == null) {
            return 0L;
        }
        return message.getSequence() > 0 ? message.getSequence() : message.getSendTimeMs();
    }
}
