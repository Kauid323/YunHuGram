package org.telegram.ui;

import android.app.Activity;

public class CallOverlayFragment {

    private static CallOverlayFragment instance;

    public static void show(Activity activity, int account) {
    }

    public static CallOverlayFragment getInstance() {
        return instance;
    }

    public static void clearInstance() {
        instance = null;
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    }

    public static void onPause() {
    }

    public static void onResume() {
    }

    public void onScreenCastStart() {
    }
}
