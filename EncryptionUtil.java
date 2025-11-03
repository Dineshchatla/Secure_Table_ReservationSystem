import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

// Simple AES-GCM utility for demonstration. Key is derived from a fixed passphrase for this basic project.
public class EncryptionUtil {
    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    // NOTE: For real systems, use a securely stored key, not a hard-coded passphrase.
    private static final byte[] KEY = "VerySecretKey123VerySecretKey".getBytes(); // 24+ bytes -> will be trimmed/padded

    public static String encrypt(String plain) throws Exception {
        byte[] keyBytes = new byte[16];
        System.arraycopy(KEY, 0, keyBytes, 0, Math.min(KEY.length, 16));
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[12];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
        byte[] encrypted = cipher.doFinal(plain.getBytes());
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String cipherText) throws Exception {
        byte[] combined = Base64.getDecoder().decode(cipherText);
        byte[] iv = new byte[12];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        byte[] enc = new byte[combined.length - iv.length];
        System.arraycopy(combined, iv.length, enc, 0, enc.length);

        byte[] keyBytes = new byte[16];
        System.arraycopy(KEY, 0, keyBytes, 0, Math.min(KEY.length, 16));
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, AES);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
        byte[] decrypted = cipher.doFinal(enc);
        return new String(decrypted);
    }
}
