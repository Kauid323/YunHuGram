package org.telegram.yh.chat;

import org.telegram.yh.model.YhForwardMessageSpec;
import org.telegram.yh.model.YhForwardReceiveTarget;

import java.util.ArrayList;

public class YhChatViewModel {

    private final YhChatRepository repository;
    private final ArrayList<YhChatMessage> messages = new ArrayList<>();
    private boolean loading;
    private boolean sending;
    private String oldestMessageId;
    private boolean hasMoreMessages = true;

    public YhChatViewModel(int account) {
        repository = new YhChatRepository(account);
    }

    public ArrayList<YhChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean hasMoreMessages() {
        return hasMoreMessages;
    }

    public boolean isSending() {
        return sending;
    }

    public boolean canLoadMoreMessages() {
        return !loading && hasMoreMessages && oldestMessageId != null && oldestMessageId.length() > 0;
    }

    public void loadInitialMessages(String chatId, int chatType, boolean force, YhChatRepository.Callback callback) {
        if (loading) {
            return;
        }
        if (!force && !messages.isEmpty()) {
            callback.onSuccess(getMessages());
            return;
        }
        loading = true;
        repository.loadInitialMessages(chatId, chatType, new YhChatRepository.Callback() {
            @Override
            public void onSuccess(ArrayList<YhChatMessage> result) {
                loading = false;
                messages.clear();
                ArrayList<YhChatMessage> normalized = normalize(result);
                messages.addAll(normalized);
                updatePagination(normalized, true);
                callback.onSuccess(getMessages());
            }

            @Override
            public void onError(String error) {
                loading = false;
                callback.onError(error);
            }
        });
    }

    public void loadMoreMessages(String chatId, int chatType, YhChatRepository.Callback callback) {
        if (loading || !hasMoreMessages || oldestMessageId == null || oldestMessageId.length() == 0) {
            return;
        }
        loading = true;
        repository.loadMoreMessages(chatId, chatType, oldestMessageId, new YhChatRepository.Callback() {
            @Override
            public void onSuccess(ArrayList<YhChatMessage> result) {
                loading = false;
                ArrayList<YhChatMessage> normalized = normalize(result);
                messages.addAll(normalized);
                updatePagination(normalized, false);
                callback.onSuccess(normalized);
            }

            @Override
            public void onError(String error) {
                loading = false;
                callback.onError(error);
            }
        });
    }

    public void sendTextMessage(String chatId, int chatType, String text, String messageId, String quoteMessageId, String quoteMessageText, YhChatRepository.SendCallback callback) {
        if (sending) {
            if (callback != null) {
                callback.onError("Sending in progress");
            }
            return;
        }
        sending = true;
        repository.sendTextMessage(chatId, chatType, text, messageId, quoteMessageId, quoteMessageText, new YhChatRepository.SendCallback() {
            @Override
            public void onSuccess() {
                sending = false;
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                sending = false;
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    public void forwardMessages(ArrayList<YhForwardMessageSpec> messageSpecs, ArrayList<YhForwardReceiveTarget> receiveTargets, YhChatRepository.SendCallback callback) {
        if (sending) {
            if (callback != null) {
                callback.onError("Sending in progress");
            }
            return;
        }
        sending = true;
        repository.forwardMessages(messageSpecs, receiveTargets, new YhChatRepository.SendCallback() {
            @Override
            public void onSuccess() {
                sending = false;
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onError(String error) {
                sending = false;
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    private void updatePagination(ArrayList<YhChatMessage> result, boolean resetOldest) {
        if (resetOldest) {
            oldestMessageId = null;
        }
        hasMoreMessages = result != null && result.size() >= YhChatRepository.DEFAULT_MESSAGE_COUNT;
        if (result == null || result.isEmpty()) {
            if (resetOldest) {
                oldestMessageId = null;
            }
            return;
        }
        for (int i = result.size() - 1; i >= 0; i--) {
            YhChatMessage oldest = result.get(i);
            if (oldest == null) {
                continue;
            }
            if (oldest.getMessageId() != null && oldest.getMessageId().length() > 0) {
                oldestMessageId = oldest.getMessageId();
                return;
            }
        }
        hasMoreMessages = false;
    }

    private ArrayList<YhChatMessage> normalize(ArrayList<YhChatMessage> result) {
        ArrayList<YhChatMessage> normalized = new ArrayList<>();
        if (result == null || result.isEmpty()) {
            return normalized;
        }
        normalized.addAll(result);
        return normalized;
    }
}
