package org.telegram.yh.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.yh.YhApiConfig;
import org.telegram.yh.YhApiException;
import org.telegram.yh.YhSession;
import org.telegram.yh.YhSessionStore;

import java.util.UUID;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class YhWebSocketManager {

    private static final String PREFS_NAME = "yh_api_device";
    private static final String KEY_DEVICE_ID = "device_id";

    private final Object sync = new Object();
    private final Gson gson = new Gson();
    private final YhApiClient apiClient;
    private final YhSessionStore sessionStore;

    private volatile WebSocket webSocket;
    private volatile boolean connected;
    private volatile Callback callback = new EmptyCallback();

    public YhWebSocketManager(YhApiClient apiClient, YhSessionStore sessionStore) {
        this.apiClient = apiClient;
        this.sessionStore = sessionStore;
    }

    public void setCallback(Callback callback) {
        this.callback = callback == null ? new EmptyCallback() : callback;
    }

    public void connect() throws YhApiException {
        final YhSession session = requireSession();
        synchronized (sync) {
            if (webSocket != null) {
                return;
            }
            Request request = new Request.Builder().url(YhApiConfig.WS_URL).build();
            webSocket = apiClient.getHttpClient().newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    connected = true;
                    callback.onOpen();
                    sendLoginInternal(session);
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    callback.onTextMessage(text);
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    callback.onBinaryMessage(bytes);
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    connected = false;
                    callback.onClosing(code, reason);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    connected = false;
                    clearSocket(webSocket);
                    callback.onClosed(code, reason);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    connected = false;
                    clearSocket(webSocket);
                    callback.onFailure(t, response);
                    FileLog.e(t);
                }
            });
        }
    }

    public void disconnect() {
        WebSocket socket;
        synchronized (sync) {
            socket = webSocket;
            webSocket = null;
            connected = false;
        }
        if (socket != null) {
            socket.close(1000, "manual_close");
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean sendLogin() throws YhApiException {
        return sendLoginInternal(requireSession());
    }

    public boolean sendHeartbeat() {
        JsonObject data = new JsonObject();
        return sendCommand(nextSeq(), "heartbeat", data);
    }

    private boolean sendLoginInternal(YhSession session) {
        JsonObject data = new JsonObject();
        data.addProperty("userId", session.getUserId());
        data.addProperty("token", session.getToken());
        data.addProperty("platform", "android");
        data.addProperty("deviceId", getOrCreateDeviceId());
        return sendCommand(nextSeq(), "login", data);
    }

    private boolean sendCommand(String seq, String cmd, JsonObject data) {
        WebSocket socket = webSocket;
        if (socket == null) {
            return false;
        }

        JsonObject root = new JsonObject();
        root.addProperty("seq", seq);
        root.addProperty("cmd", cmd);
        root.add("data", data == null ? new JsonObject() : data);
        return socket.send(gson.toJson(root));
    }

    private String getOrCreateDeviceId() {
        Context context = ApplicationLoader.applicationContext;
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String deviceId = preferences.getString(KEY_DEVICE_ID, null);
        if (deviceId != null && deviceId.length() > 0) {
            return deviceId;
        }

        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.length() == 0) {
            deviceId = UUID.randomUUID().toString().replace("-", "");
        }

        preferences.edit().putString(KEY_DEVICE_ID, deviceId).apply();
        return deviceId;
    }

    private YhSession requireSession() throws YhApiException {
        YhSession session = sessionStore.getSession();
        if (session == null || !session.isValid() || session.getUserId() == null || session.getUserId().length() == 0) {
            throw new YhApiException("YH websocket session is not set");
        }
        return session;
    }

    private void clearSocket(WebSocket currentSocket) {
        synchronized (sync) {
            if (webSocket == currentSocket) {
                webSocket = null;
            }
        }
    }

    private String nextSeq() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public interface Callback {
        void onOpen();

        void onTextMessage(String text);

        void onBinaryMessage(ByteString bytes);

        void onClosing(int code, String reason);

        void onClosed(int code, String reason);

        void onFailure(Throwable throwable, Response response);
    }

    private static class EmptyCallback implements Callback {
        @Override
        public void onOpen() {
        }

        @Override
        public void onTextMessage(String text) {
        }

        @Override
        public void onBinaryMessage(ByteString bytes) {
        }

        @Override
        public void onClosing(int code, String reason) {
        }

        @Override
        public void onClosed(int code, String reason) {
        }

        @Override
        public void onFailure(Throwable throwable, Response response) {
        }
    }
}
