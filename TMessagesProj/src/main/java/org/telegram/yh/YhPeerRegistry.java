package org.telegram.yh;

import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;

public final class YhPeerRegistry {

    private static final ConcurrentHashMap<Long, String> PEER_IDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> PEER_TYPES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, String> AVATAR_URLS = new ConcurrentHashMap<>();

    private YhPeerRegistry() {
    }

    public static void registerPeer(long syntheticPeerId, String peerId, int chatType) {
        if (syntheticPeerId == 0 || TextUtils.isEmpty(peerId) || !YhChatType.isValid(chatType)) {
            return;
        }
        PEER_IDS.put(syntheticPeerId, peerId);
        PEER_TYPES.put(syntheticPeerId, chatType);
    }

    public static void registerGroup(long syntheticChatId, String groupId) {
        registerPeer(syntheticChatId, groupId, YhChatType.GROUP);
    }

    public static void registerAvatarUrl(long syntheticPeerId, String avatarUrl) {
        if (syntheticPeerId == 0) {
            return;
        }
        String normalizedAvatarUrl = YhImageUrlHelper.getAvatarDetailUrl(avatarUrl);
        if (TextUtils.isEmpty(normalizedAvatarUrl)) {
            AVATAR_URLS.remove(syntheticPeerId);
        } else {
            AVATAR_URLS.put(syntheticPeerId, normalizedAvatarUrl);
        }
    }

    public static String findGroupId(long syntheticChatId) {
        return findPeerType(syntheticChatId) == YhChatType.GROUP ? findPeerId(syntheticChatId) : null;
    }

    public static String findPeerId(long syntheticPeerId) {
        return PEER_IDS.get(syntheticPeerId);
    }

    public static int findPeerType(long syntheticPeerId) {
        Integer type = PEER_TYPES.get(syntheticPeerId);
        return type == null ? 0 : type;
    }

    public static String findAvatarUrl(long syntheticPeerId) {
        return AVATAR_URLS.get(syntheticPeerId);
    }
}
