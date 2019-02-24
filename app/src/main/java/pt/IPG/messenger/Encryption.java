package pt.IPG.messenger;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author JDinis
 */
public class Encryption {

    public static final String ENCRYPTION_SEPARATOR = ":";

    public enum MessageType {
        Decrypted,
        DecryptedBytes,
        Encrypted,
        EncryptedBytes
    }

    static final String TAG = "SymmetricAlgorithmAES";

    // Set up secret key spec for 128-bit AES encryption and decryption
    private static SecretKeySpec sks = null;

    /**
     * Converts byte array to hex string
     * @param data byte array
     * @return hex string
     */
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Creates a Sha1 hash from a given string
     * @param text string to hex
     * @return Sha1 Hash
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("iso-8859-1");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    /**
     * Instantiates the class Encryption by generating a secret key
     */
    public Encryption() {
        try {

            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed("Chat-RSCM".getBytes());
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(128, sr);
            sks = new SecretKeySpec(SHA1("Chat-RSCM").getBytes(), "AES");
        } catch (Exception e) {
            Log.e(TAG, "AES secret key spec error");
        }
    }

    /**
     * Encrypts a message string
     * @param message to be encrypted
     * @param type describes whether or not a message is to be encrypted
     * @throws Exception MessageType must be either Decrypted or Encrypted
     * @return Encrypted message string encoded with base64 or Plain-Text message depending on MessageType specified
     */
    public String Encrypt(String message, MessageType type) throws Exception {
        if(type==MessageType.Decrypted){
            return type.ordinal()+ENCRYPTION_SEPARATOR+ message;
        }

        if(type != MessageType.Encrypted){
            throw new Exception("Wrong encryption method or message type used!");
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(message.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
        return MessageType.Encrypted.ordinal() +ENCRYPTION_SEPARATOR+ Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    /**
     * Encrypts a byte array message
     * @param messageBytes can be a byte array that holds a string value, image bytes, voice byte data, etc...
     * @param type describes whether or not a message is to be encrypted
     * @throws Exception MessageType must be either DecryptedBytes or EncryptedBytes
     * @return Encrypted or Decrypted byte array encoded with base64 depending on MessageType specified
     */
    public String Encrypt(byte[] messageBytes, MessageType type) throws Exception {
        if(type==MessageType.DecryptedBytes){
            return type.ordinal()+ENCRYPTION_SEPARATOR+ Base64.encodeToString(messageBytes,Base64.DEFAULT);
        }

        if(type != MessageType.EncryptedBytes){
            throw new Exception("Wrong encryption method or message type used!");
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(messageBytes);
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
        return MessageType.EncryptedBytes.ordinal() +ENCRYPTION_SEPARATOR+ Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    /**
     * Decrypts a base64 string message
     * @param encodedMessage is data that has been encoded with base64 to be decrypted
     * @throws Exception MessageType must be Encrypted
     * @return Decrypted data as String
     */
    public String Decrypt(String encodedMessage) throws Exception {
        int type = Integer.parseInt(encodedMessage.split(ENCRYPTION_SEPARATOR)[0]);
        String message = encodedMessage.split(ENCRYPTION_SEPARATOR)[1];
        byte[] decodedBytes = null;
        String result = null;

        switch (MessageType.values()[type]){
            case Decrypted:
            case DecryptedBytes:
                return message;
            case Encrypted:
            case EncryptedBytes:
                // Decode the encoded data with AES
                try {
                    Cipher c = Cipher.getInstance("AES");
                    c.init(Cipher.DECRYPT_MODE, sks);
                    decodedBytes = c.doFinal(Base64.decode(message, Base64.DEFAULT));
                } catch (Exception e) {
                    Log.e(TAG, "AES decryption error");
                }

                result = (type==MessageType.EncryptedBytes.ordinal()) ? Base64.encodeToString(decodedBytes,Base64.DEFAULT) : new String(decodedBytes);
                break;
            default:
                throw new Exception("Wrong decryption method or encryption message type used!");
        }
        return result;
    }
}
