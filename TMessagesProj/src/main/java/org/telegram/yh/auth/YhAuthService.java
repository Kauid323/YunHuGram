package org.telegram.yh.auth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.telegram.yh.YhApiException;
import org.telegram.yh.service.YhApiClient;

import java.io.IOException;
import java.nio.charset.Charset;

public class YhAuthService {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String PLATFORM = "android";

    private final YhApiClient apiClient;
    private final Gson gson;

    public YhAuthService(YhApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = apiClient.getGson();
    }

    public YhCaptchaInfo fetchCaptcha() throws IOException {
        JsonObject response = parseJson(apiClient.postJson("v1/user/captcha", null, null));
        JsonObject data = requireSuccess(response);
        String base64 = stringValue(data, "b64s");
        String id = stringValue(data, "id");
        Bitmap bitmap = decodeBitmap(base64);
        if (TextUtils.isEmpty(id) || bitmap == null) {
            throw new YhApiException("图片验证码解析失败");
        }
        return new YhCaptchaInfo(id, base64, bitmap);
    }

    public void requestSmsCode(String mobile, String captchaCode, String captchaId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("mobile", normalizeMobile(mobile));
        body.addProperty("code", captchaCode != null ? captchaCode.trim() : null);
        body.addProperty("id", captchaId);
        JsonObject response = parseJson(apiClient.postJson("v1/verification/get-verification-code", null, body));
        requireSuccess(response);
    }

    public YhLoginSession verificationLogin(String mobile, String smsCode, String deviceId) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("mobile", normalizeMobile(mobile));
        body.addProperty("captcha", smsCode != null ? smsCode.trim() : null);
        body.addProperty("deviceId", deviceId);
        body.addProperty("platform", PLATFORM);
        JsonObject response = parseJson(apiClient.postJson("v1/user/verification-login", null, body));
        JsonObject data = requireSuccess(response);
        String token = stringValue(data, "token");
        if (TextUtils.isEmpty(token)) {
            throw new YhApiException("登录成功但未返回 token");
        }
        return new YhLoginSession(token, mobile, deviceId);
    }

    private String normalizeMobile(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return mobile;
        }
        String normalized = mobile.replaceAll("[^0-9]", "");
        if (normalized.startsWith("86") && normalized.length() > 11) {
            normalized = normalized.substring(2);
        }
        return normalized;
    }

    private JsonObject parseJson(byte[] bytes) throws IOException {
        JsonObject object = gson.fromJson(new String(bytes, UTF_8), JsonObject.class);
        if (object == null) {
            throw new YhApiException("服务端返回为空");
        }
        return object;
    }

    private JsonObject requireSuccess(JsonObject object) throws YhApiException {
        int code = object.has("code") ? object.get("code").getAsInt() : -1;
        String message = object.has("msg") ? object.get("msg").getAsString() : "请求失败";
        if (code != 1) {
            throw new YhApiException(code, message);
        }
        return object.has("data") && object.get("data").isJsonObject() ? object.getAsJsonObject("data") : new JsonObject();
    }

    private String stringValue(JsonObject object, String key) {
        return object != null && object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
    }

    private Bitmap decodeBitmap(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String content = raw;
        int commaIndex = raw.indexOf(',');
        if (commaIndex >= 0 && commaIndex < raw.length() - 1) {
            content = raw.substring(commaIndex + 1);
        } else {
            int base64Index = raw.indexOf(";base64,");
            if (base64Index >= 0 && base64Index + 8 < raw.length()) {
                content = raw.substring(base64Index + 8);
            }
        }
        byte[] bytes = Base64.decode(content, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
