package org.telegram.yh;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class YhSessionStore {

    private static final String PREF_PREFIX = "yh_api_session_";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN_DATA = "token_data";
    private static final String KEY_TOKEN_IV = "token_iv";
    private static final String KEY_USER_ID_DATA = "user_id_data";
    private static final String KEY_USER_ID_IV = "user_id_iv";
    private static final String KEY_FALLBACK_SALT = "fallback_salt";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private final int account;
    private final SharedPreferences preferences;
    private final SecureRandom secureRandom = new SecureRandom();

    public YhSessionStore(int account) {
        this.account = account;
        Context context = ApplicationLoader.applicationContext;
        preferences = context.getSharedPreferences(PREF_PREFIX + account, Context.MODE_PRIVATE);
    }

    public synchronized void saveSession(String token, String userId) {
        try {
            EncryptedValue encryptedToken = encrypt(token);
            EncryptedValue encryptedUserId = encrypt(userId);
            SharedPreferences.Editor editor = preferences.edit()
                    .remove(KEY_TOKEN)
                    .remove(KEY_USER_ID);

            putEncrypted(editor, KEY_TOKEN_DATA, KEY_TOKEN_IV, encryptedToken);
            putEncrypted(editor, KEY_USER_ID_DATA, KEY_USER_ID_IV, encryptedUserId);
            editor.apply();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public synchronized YhSession getSession() {
        String token = decrypt(KEY_TOKEN_DATA, KEY_TOKEN_IV);
        String userId = decrypt(KEY_USER_ID_DATA, KEY_USER_ID_IV);
        if (!TextUtils.isEmpty(token)) {
            return new YhSession(token, userId);
        }

        String legacyToken = preferences.getString(KEY_TOKEN, null);
        String legacyUserId = preferences.getString(KEY_USER_ID, null);
        if (!TextUtils.isEmpty(legacyToken)) {
            saveSession(legacyToken, legacyUserId);
            preferences.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply();
            return new YhSession(legacyToken, legacyUserId);
        }
        return new YhSession(null, null);
    }

    public synchronized void clear() {
        preferences.edit().clear().apply();
    }

    private void putEncrypted(SharedPreferences.Editor editor, String dataKey, String ivKey, EncryptedValue value) {
        if (value == null) {
            editor.remove(dataKey).remove(ivKey);
            return;
        }
        editor.putString(dataKey, value.data);
        editor.putString(ivKey, value.iv);
    }

    private EncryptedValue encrypt(String value) throws GeneralSecurityException {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        return new EncryptedValue(
                Base64.encodeToString(encrypted, Base64.NO_WRAP),
                Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
        );
    }

    private String decrypt(String dataKey, String ivKey) {
        String encrypted = preferences.getString(dataKey, null);
        String iv = preferences.getString(ivKey, null);
        if (TextUtils.isEmpty(encrypted) || TextUtils.isEmpty(iv)) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    getSecretKey(),
                    new GCMParameterSpec(GCM_TAG_LENGTH, Base64.decode(iv, Base64.NO_WRAP))
            );
            byte[] decrypted = cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    private SecretKey getSecretKey() throws GeneralSecurityException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getOrCreateKeyStoreKey();
        }
        return getFallbackKey();
    }

    private SecretKey getOrCreateKeyStoreKey() throws GeneralSecurityException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
        try {
            keyStore.load(null);
        } catch (Exception e) {
            throw new GeneralSecurityException(e);
        }

        String alias = "yh_session_key_" + account;
        SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
        if (secretKey != null) {
            return secretKey;
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);
        keyGenerator.init(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return keyGenerator.generateKey();
    }

    private SecretKey getFallbackKey() throws GeneralSecurityException {
        String saltBase64 = preferences.getString(KEY_FALLBACK_SALT, null);
        if (TextUtils.isEmpty(saltBase64)) {
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP);
            preferences.edit().putString(KEY_FALLBACK_SALT, saltBase64).apply();
        }

        String androidId = Settings.Secure.getString(ApplicationLoader.applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(androidId)) {
            androidId = ApplicationLoader.applicationContext.getPackageName();
        }
        String secret = androidId + ":" + ApplicationLoader.applicationContext.getPackageName() + ":" + account;
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), Base64.decode(saltBase64, Base64.NO_WRAP), 12000, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, KeyProperties.KEY_ALGORITHM_AES);
    }

    private static class EncryptedValue {
        private final String data;
        private final String iv;

        private EncryptedValue(String data, String iv) {
            this.data = data;
            this.iv = iv;
        }
    }
}
