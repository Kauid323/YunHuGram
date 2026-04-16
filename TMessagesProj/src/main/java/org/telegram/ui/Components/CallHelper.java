package org.telegram.ui.Components;

import android.app.Activity;
import android.widget.Toast;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.call.CallConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;

import java.io.File;

public class CallHelper {

    public static long lastCallTime;

    private static void showUnavailable(Activity activity) {
        if (activity == null) {
            return;
        }
        Toast.makeText(activity, activity.getString(R.string.VoipOfflineAirplaneTitle), Toast.LENGTH_SHORT).show();
    }

    public static void startCall(TLRPC.User user, boolean videoCall, boolean canVideoCall, Activity activity, TLRPC.UserFull userFull, AccountInstance accountInstance) {
        showUnavailable(activity);
    }

    public static void startCall(TLRPC.Chat chat, TLRPC.InputPeer peer, String hash, boolean createGroupCall, Activity activity, BaseFragment fragment, AccountInstance accountInstance) {
        showUnavailable(activity);
    }

    public static void startCall(TLRPC.Chat chat, TLRPC.InputPeer peer, String hash, boolean createGroupCall, boolean canStream, Activity activity, BaseFragment fragment, AccountInstance accountInstance) {
        showUnavailable(activity);
    }

    public static void permissionDenied(Activity activity, Runnable onFinish, int requestCode) {
        if (onFinish != null) {
            onFinish.run();
        }
    }

    public static int getDataSavingDefault() {
        return CallConfig.DATA_SAVING_NEVER;
    }

    public static void showGroupCallAlert(BaseFragment fragment, TLRPC.Chat chat, TLRPC.InputPeer peer, boolean createGroupCall, AccountInstance accountInstance) {
        showUnavailable(fragment != null ? fragment.getParentActivity() : null);
    }

    public static boolean canRateCall(TLRPC.TL_messageActionPhoneCall call) {
        return false;
    }

    public static void showRateAlert(Activity activity, TLRPC.TL_messageActionPhoneCall call) {
    }

    public static void showRateAlert(Activity activity, Runnable onDismiss, boolean video, long callId, long callAccessHash, int account, boolean userInitiative) {
        if (onDismiss != null) {
            onDismiss.run();
        }
    }

    public static void showCallDebugSettings(Activity activity) {
    }

    public static File getLogsDir() {
        File dir = new File(FileLoader.checkDirectory(4), "call_logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static String getLogFilePath(long id, boolean stats) {
        return new File(getLogsDir(), (stats ? "stats_" : "call_") + id + ".log").getAbsolutePath();
    }

    public static String getLogFilePath(String name) {
        return new File(getLogsDir(), name + ".log").getAbsolutePath();
    }
}
