package org.telegram.yh.service;

import com.google.gson.Gson;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.yh.YhApiConfig;
import org.telegram.yh.YhApiException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class YhApiClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType PROTO_MEDIA_TYPE = MediaType.parse("application/x-protobuf");

    private final OkHttpClient httpClient;
    private final Gson gson = new Gson();

    public YhApiClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        if (BuildVars.LOGS_ENABLED) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(interceptor);
        }

        httpClient = builder.build();
    }

    public byte[] postJson(String relativePath, String token, Object body) throws IOException {
        String json = body == null ? "{}" : gson.toJson(body);
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, json);
        return execute(buildPostRequest(relativePath, token, requestBody));
    }

    public byte[] postProto(String relativePath, String token, byte[] body) throws IOException {
        RequestBody requestBody = RequestBody.create(PROTO_MEDIA_TYPE, body == null ? new byte[0] : body);
        return execute(buildPostRequest(relativePath, token, requestBody));
    }

    public byte[] getProto(String relativePath, String token) throws IOException {
        return execute(buildGetRequest(relativePath, token));
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public Gson getGson() {
        return gson;
    }

    private Request buildPostRequest(String relativePath, String token, RequestBody requestBody) throws IOException {
        HttpUrl baseUrl = HttpUrl.parse(YhApiConfig.BASE_URL);
        if (baseUrl == null) {
            throw new IOException("Invalid YH API base url");
        }

        HttpUrl url = baseUrl.resolve(relativePath);
        if (url == null) {
            throw new IOException("Invalid YH API path: " + relativePath);
        }

        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("YH API POST: " + url + " token=" + (token != null && token.length() > 0 ? "present" : "null"));
        }

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Accept", "application/json, application/x-protobuf");

        if (token != null && token.length() > 0) {
            builder.header("token", token);
        }

        return builder.build();
    }

    private Request buildGetRequest(String relativePath, String token) throws IOException {
        HttpUrl baseUrl = HttpUrl.parse(YhApiConfig.BASE_URL);
        if (baseUrl == null) {
            throw new IOException("Invalid YH API base url");
        }

        HttpUrl url = baseUrl.resolve(relativePath);
        if (url == null) {
            throw new IOException("Invalid YH API path: " + relativePath);
        }

        Request.Builder builder = new Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json, application/x-protobuf");

        if (token != null && token.length() > 0) {
            builder.header("token", token);
        }

        return builder.build();
    }

    private byte[] execute(Request request) throws IOException {
        Response response = httpClient.newCall(request).execute();
        try {
            if (!response.isSuccessful()) {
                throw new YhApiException(response.code(), response.message());
            }

            ResponseBody body = response.body();
            return body == null ? new byte[0] : body.bytes();
        } finally {
            response.close();
        }
    }
}
