package org.telegram.yh.dialogs;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.yh.YhApiFacade;
import org.telegram.yh.model.YhConversation;
import org.telegram.yh.model.YhConversationList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class YhDialogsRepository {

    public interface Callback {
        void onSuccess(ArrayList<YhDialogItem> dialogs);
        void onError(String error);
    }

    private final int account;

    public YhDialogsRepository(int account) {
        this.account = account;
    }

    public void loadDialogs(Callback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                YhConversationList conversationList = YhApiFacade.getInstance(account).conversations().listConversations();
                ArrayList<YhDialogItem> dialogs = new ArrayList<>();
                for (YhConversation conversation : conversationList.getConversations()) {
                    dialogs.add(YhDialogItem.fromConversation(conversation));
                }
                Collections.sort(dialogs, Comparator.comparingLong(YhDialogItem::getTimestampSeconds).reversed());
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(dialogs));
            } catch (Exception e) {
                String error = e == null || e.getMessage() == null || e.getMessage().length() == 0 ? "会话加载失败" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }
}
