package org.telegram.messenger.call;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.telegram.messenger.ChatObject;
import org.telegram.tgnet.TLRPC;

public class CallService extends Service {

    public interface StateListener {
        default void onAudioSettingsChanged() {
        }

        default void onStateChanged(int state) {
        }
    }

    public static final int STATE_WAIT_INIT = 1;
    public static final int STATE_WAIT_INIT_ACK = 2;
    public static final int STATE_EXCHANGING_KEYS = 3;
    public static final int STATE_WAITING = 4;
    public static final int STATE_REQUESTING = 5;
    public static final int STATE_RINGING = 6;
    public static final int STATE_WAITING_INCOMING = 7;
    public static final int STATE_BUSY = 8;
    public static final int STATE_ESTABLISHED = 9;
    public static final int STATE_RECONNECTING = 10;
    public static final int STATE_FAILED = 11;
    public static final int STATE_HANGING_UP = 12;
    public static final int STATE_ENDED = 13;
    public static final int STATE_CREATING = 14;

    public static TLRPC.PhoneCall callIShouldHavePutIntoIntent;

    private static CallService sharedInstance;

    public ChatObject.Call groupCall;
    public boolean hasFewPeers;
    public boolean mutedByAdmin;
    public String currentBluetoothDeviceName = "";

    private final IBinder binder = new Binder();
    private int currentAccount;
    private int callState = STATE_ENDED;
    private boolean micMute;
    private TLRPC.Chat chat;
    private TLRPC.User user;
    private TLRPC.InputPeer groupCallPeer;
    private String lastError = CallConfig.ERROR_UNKNOWN;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedInstance = this;
    }

    @Override
    public void onDestroy() {
        if (sharedInstance == this) {
            sharedInstance = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public static CallService getSharedInstance() {
        return sharedInstance;
    }

    public static boolean isAnyKindOfCallActive() {
        return false;
    }

    public static boolean hasRtmpStream() {
        return false;
    }

    public int getAccount() {
        return currentAccount;
    }

    public TLRPC.Chat getChat() {
        return chat;
    }

    public TLRPC.User getUser() {
        return user;
    }

    public long getCallerId() {
        return 0L;
    }

    public long getSelfId() {
        return 0L;
    }

    public boolean isHangingUp() {
        return false;
    }

    public boolean isMicMute() {
        return micMute;
    }

    public boolean isOutgoing() {
        return false;
    }

    public boolean isJoined() {
        return false;
    }

    public boolean hasEarpiece() {
        return false;
    }

    public boolean isBluetoothHeadsetConnected() {
        return false;
    }

    public boolean isHeadsetPlugged() {
        return false;
    }

    public boolean isFrontFaceCamera() {
        return false;
    }

    public boolean isFullscreen(Object... args) {
        return false;
    }

    public boolean isSwitchingStream() {
        return false;
    }

    public int getCallState() {
        return callState;
    }

    public int getCurrentAudioRoute() {
        return 0;
    }

    public int getVideoState(boolean presentation) {
        return CallConfig.VIDEO_STATE_INACTIVE;
    }

    public int getRemoteVideoState() {
        return CallConfig.VIDEO_STATE_INACTIVE;
    }

    public int getRemoteAudioState() {
        return CallConfig.AUDIO_STATE_ACTIVE;
    }

    public long getCallDuration() {
        return 0L;
    }

    public String getLastError() {
        return lastError;
    }

    public TLRPC.InputPeer getGroupCallPeer() {
        return groupCallPeer;
    }

    public void setMicMute(boolean mute, boolean notify, boolean save) {
        micMute = mute;
    }

    public void registerStateListener(StateListener listener) {
    }

    public void unregisterStateListener(StateListener listener) {
    }

    public void onGroupCallUpdated(TLRPC.GroupCall call) {
    }

    public void onGroupCallParticipantsUpdate(TLRPC.TL_updateGroupCallParticipants update) {
    }

    public void onSignalingData(TLRPC.TL_updatePhoneCallSignalingData data) {
    }

    public void onCallUpdated(TLRPC.PhoneCall call) {
    }

    public void onCameraFirstFrameAvailable() {
    }

    public void playAllowTalkSound() {
    }

    public void playConnectedSound() {
    }

    public void playStartRecordSound() {
    }

    public void createCaptureDevice(Object... args) {
    }

    public void acceptIncomingCall() {
    }

    public void declineIncomingCall() {
    }

    public void requestVideoCall(Object... args) {
    }

    public void setVideoState(Object... args) {
    }

    public void toggleSpeakerphoneOrShowRouteSheet(Object... args) {
    }

    public void hangUp() {
    }

    public void stopScreenCapture() {
    }

    public void setSwitchingCamera(Object... args) {
    }

    public void setLocalSink(Object... args) {
    }

    public void addRemoteSink(Object... args) {
    }

    public void removeRemoteSink(Object... args) {
    }

    public void clearRemoteSinks() {
    }

    public void setSinks(Object... args) {
    }

    public void setBackgroundSinks(Object... args) {
    }

    public void swapSinks(Object... args) {
    }

    public void switchCamera() {
    }

    public void setGroupCallPeer(Object... args) {
        if (args.length > 0 && args[0] instanceof TLRPC.InputPeer) {
            groupCallPeer = (TLRPC.InputPeer) args[0];
        }
    }

    public void migrateToChat(TLRPC.Chat value) {
        chat = value;
    }

    public void setParticipantsVolume() {
    }

    public void editCallMember(Object... args) {
    }

    public void setParticipantVolume(Object... args) {
    }

    public void handleNotificationAction(Object... args) {
    }

    public void onMediaButtonEvent(Object... args) {
    }

    public void callFailedFromConnectionService() {
    }
}
