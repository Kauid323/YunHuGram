package org.telegram.yh.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;
import org.telegram.yh.YhApiFacade;
import org.telegram.yh.service.YhApiClient;

import java.util.UUID;

public class YhAuthRepository {

    private static final String PREFS_PREFIX = "yh_auth_login_";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_MOBILE = "mobile";

    private final int account;
    private final SharedPreferences preferences;
    private final YhAuthService authService;

    public YhAuthRepository(int account) {
        this.account = account;
        this.preferences = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_PREFIX + account, Context.MODE_PRIVATE);
        this.authService = new YhAuthService(new YhApiClient());
    }

    public String getSavedMobile() {
        return preferences.getString(KEY_MOBILE, "");
    }

    public void requestCaptcha(YhAuthCallback<YhCaptchaInfo> callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                YhCaptchaInfo result = authService.fetchCaptcha();
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(result));
            } catch (Exception e) {
                AndroidUtilities.runOnUIThread(() -> callback.onError(resolveMessage(e)));
            }
        });
    }

    public void requestSmsCode(String mobile, String captchaCode, String captchaId, YhAuthCallback<Void> callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                authService.requestSmsCode(mobile, captchaCode, captchaId);
                preferences.edit().putString(KEY_MOBILE, mobile).apply();
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(null));
            } catch (Exception e) {
                AndroidUtilities.runOnUIThread(() -> callback.onError(resolveMessage(e)));
            }
        });
    }

    public void verificationLogin(String mobile, String smsCode, YhAuthCallback<YhLoginSession> callback) {
        Utilities.globalQueue.postRunnable(() -> {
            try {
                String deviceId = getOrCreateDeviceId();
                YhLoginSession session = authService.verificationLogin(mobile, smsCode, deviceId);
                preferences.edit().putString(KEY_MOBILE, mobile).apply();
                YhApiFacade apiFacade = YhApiFacade.getInstance(account);
                apiFacade.setSession(session.getToken(), null);
                try {
                    apiFacade.webSocket().connect();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                AndroidUtilities.runOnUIThread(() -> callback.onSuccess(session));
            } catch (Exception e) {
                AndroidUtilities.runOnUIThread(() -> callback.onError(resolveMessage(e)));
            }
        });
    }

    private String getOrCreateDeviceId() {
        String deviceId = preferences.getString(KEY_DEVICE_ID, null);
        if (!TextUtils.isEmpty(deviceId)) {
            return deviceId;
        }
        deviceId = Settings.Secure.getString(ApplicationLoader.applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString().replace("-", "");
        }
        preferences.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        return deviceId;
    }

    private String resolveMessage(Exception e) {
        return e == null || TextUtils.isEmpty(e.getMessage()) ? "请求失败" : e.getMessage();
    }
}
