package org.telegram.yh.contacts;

import org.telegram.yh.model.YhAddressBookEntry;

import java.util.ArrayList;

public class YhContactsViewModel {

    private final YhContactsRepository repository;
    private boolean loading;
    private final ArrayList<YhAddressBookEntry> cachedEntries = new ArrayList<>();

    public YhContactsViewModel(int account) {
        repository = new YhContactsRepository(account);
    }

    public boolean isLoading() {
        return loading;
    }

    public ArrayList<YhAddressBookEntry> getCachedEntries() {
        return new ArrayList<>(cachedEntries);
    }

    public void loadAddressBook(boolean force, YhContactsRepository.Callback callback) {
        if (loading) {
            return;
        }
        if (!force && !cachedEntries.isEmpty()) {
            callback.onSuccess(getCachedEntries());
            return;
        }
        loading = true;
        repository.loadAddressBook(new YhContactsRepository.Callback() {
            @Override
            public void onSuccess(ArrayList<YhAddressBookEntry> entries) {
                loading = false;
                cachedEntries.clear();
                if (entries != null && !entries.isEmpty()) {
                    cachedEntries.addAll(entries);
                }
                callback.onSuccess(getCachedEntries());
            }

            @Override
            public void onError(String error) {
                loading = false;
                if (!cachedEntries.isEmpty()) {
                    callback.onSuccess(getCachedEntries());
                } else {
                    callback.onError(error);
                }
            }
        });
    }
}
