package org.telegram.yh;

import android.text.TextUtils;

import java.net.URL;
import java.util.Locale;

public final class YhImageUrlHelper {

    private static final String JWZNB_HOST_SUFFIX = "jwznb.com";
    private static final String YH_REFERER = "https://myapp.jwznb.com";
    private static final String AVATAR_THUMB_QUERY = "imageView2%2F2%2Fw%2F72%2Fh%2F72";

    private YhImageUrlHelper() {
    }

    public static String getReferer() {
        return YH_REFERER;
    }

    public static boolean needsReferer(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        try {
            URL parsed = new URL(url);
            String protocol = parsed.getProtocol();
            String host = parsed.getHost();
            return "https".equalsIgnoreCase(protocol)
                    && !TextUtils.isEmpty(host)
                    && host.toLowerCase(Locale.US).endsWith(JWZNB_HOST_SUFFIX);
        } catch (Exception ignore) {
            return false;
        }
    }

    public static String getAvatarThumbUrl(String url) {
        String normalized = getAvatarDetailUrl(url);
        if (TextUtils.isEmpty(normalized)) {
            return normalized;
        }
        if (normalized.contains(AVATAR_THUMB_QUERY)) {
            return normalized;
        }
        return normalized + (normalized.contains("?") ? "&" : "?") + AVATAR_THUMB_QUERY;
    }

    public static String getAvatarDetailUrl(String url) {
        String normalized = normalize(url);
        if (TextUtils.isEmpty(normalized)) {
            return normalized;
        }
        if (!needsReferer(normalized)) {
            return normalized;
        }
        try {
            URL parsed = new URL(normalized);
            String protocol = parsed.getProtocol();
            String host = parsed.getHost();
            int port = parsed.getPort();
            String path = parsed.getPath();
            if (TextUtils.isEmpty(protocol) || TextUtils.isEmpty(host) || TextUtils.isEmpty(path)) {
                return normalized;
            }
            StringBuilder builder = new StringBuilder();
            builder.append(protocol).append("://").append(host);
            if (port != -1) {
                builder.append(":").append(port);
            }
            builder.append(path);
            return builder.toString();
        } catch (Exception ignore) {
            return normalized;
        }
    }

    public static String getDetailImageUrl(String url) {
        return normalize(url);
    }

    private static String normalize(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String normalized = url.trim();
        return normalized.length() == 0 ? null : normalized;
    }
}
