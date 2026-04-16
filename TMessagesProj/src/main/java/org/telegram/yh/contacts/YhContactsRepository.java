package org.telegram.yh.contacts;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;
import org.telegram.yh.YhApiFacade;
import org.telegram.yh.model.YhAddressBookEntry;

import java.util.ArrayList;

public class YhContactsRepository {

    public interface Callback {
        void onSuccess(ArrayList<YhAddressBookEntry> entries);
        void onError(String error);
    }

    private final int account;

    public YhContactsRepository(int account) {
        this.account = account;
    }

    public void loadAddressBook(Callback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                ArrayList<YhAddressBookEntry> entries = YhApiFacade.getInstance(account)
                        .users()
                        .getAddressBookList();
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(entries));
            } catch (Exception e) {
                String error = e.getMessage() == null ? "加载通讯录失败" : e.getMessage();
                AndroidUtilities.runOnUIThread(() -> callback.onError(error));
            }
        });
    }
}
