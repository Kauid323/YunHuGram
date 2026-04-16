package org.telegram.yh.dialogs;

import java.util.ArrayList;

public class YhDialogsViewModel {

    private final YhDialogsRepository repository;
    private final ArrayList<YhDialogItem> dialogs = new ArrayList<>();
    private boolean loading;

    public YhDialogsViewModel(int account) {
        repository = new YhDialogsRepository(account);
    }

    public ArrayList<YhDialogItem> getDialogs() {
        return new ArrayList<>(dialogs);
    }

    public boolean isLoading() {
        return loading;
    }

    public void loadDialogs(boolean force, YhDialogsRepository.Callback callback) {
        if (loading) {
            return;
        }
        if (!force && !dialogs.isEmpty()) {
            callback.onSuccess(getDialogs());
            return;
        }
        loading = true;
        repository.loadDialogs(new YhDialogsRepository.Callback() {
            @Override
            public void onSuccess(ArrayList<YhDialogItem> result) {
                loading = false;
                dialogs.clear();
                dialogs.addAll(result);
                callback.onSuccess(getDialogs());
            }

            @Override
            public void onError(String error) {
                loading = false;
                callback.onError(error);
            }
        });
    }
}
