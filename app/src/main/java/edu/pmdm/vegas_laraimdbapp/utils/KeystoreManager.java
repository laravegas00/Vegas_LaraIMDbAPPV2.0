package edu.pmdm.vegas_laraimdbapp.utils;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import android.util.Base64;

/**
 * Clase para cifrar y descifrar datos utilizando el Keystore de Android.
 */
public class KeystoreManager {
    // Constantes para el Keystore
    private static final String KEY_ALIAS = "MyAppKeyAlias";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private Context context;

    /**
     * Constructor
     * @param context Contexto de la aplicación
     */
    public KeystoreManager(Context context) {
        this.context = context;

        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
                keyGenerator.init(new KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
                keyGenerator.generateKey();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cifrar un texto plano
     * @param plainText Texto plano a cifrar
     * @return Texto cifrado
     */
    public String encrypt(String plainText) {
        try {
            // Obtiene la clave almacenada en el Keystore
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            Key key = keyStore.getKey(KEY_ALIAS, null);

            // Configura el cifrador con AES/GCM/NoPadding y la clave
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, (SecretKey) key);

            // Cifra el texto plano
            byte[] iv = cipher.getIV();
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            // Combina el IV con el texto cifrado
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // Retorna el texto cifrado en Base64
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Descifrar un texto cifrado
     * @param encryptedText Texto cifrado a descifrar
     * @return Texto descifrado
     */
    public String decrypt(String encryptedText) {
        try {
            // Obtiene la clave almacenada en el Keystore
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            Key key = keyStore.getKey(KEY_ALIAS, null);

            // Decodifica el texto cifrado
            byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);
            byte[] iv = new byte[12];
            byte[] encrypted = new byte[combined.length - 12];

            // Copia el IV y el texto cifrado
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            // Configura el cifrador con AES/GCM/NoPadding y la clave
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, (SecretKey) key, new GCMParameterSpec(128, iv));

            return new String(cipher.doFinal(encrypted)); // Retorna el texto descifrado
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}