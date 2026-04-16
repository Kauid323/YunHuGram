package org.telegram.yh.auth;

public interface YhAuthCallback<T> {
    void onSuccess(T result);

    void onError(String error);
}
