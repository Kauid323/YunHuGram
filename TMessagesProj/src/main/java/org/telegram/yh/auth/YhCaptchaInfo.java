package org.telegram.yh.auth;

import android.graphics.Bitmap;

public class YhCaptchaInfo {

    private final String id;
    private final String base64;
    private final Bitmap bitmap;

    public YhCaptchaInfo(String id, String base64, Bitmap bitmap) {
        this.id = id;
        this.base64 = base64;
        this.bitmap = bitmap;
    }

    public String getId() {
        return id;
    }

    public String getBase64() {
        return base64;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
