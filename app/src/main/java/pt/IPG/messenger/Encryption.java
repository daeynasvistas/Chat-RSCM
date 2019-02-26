package pt.IPG.messenger;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author JDinis
 */
public class Encryption {

    private static final String ENCRYPTION_SEPARATOR = ":";

    public enum MessageType {
        Decrypted,
        DecryptedBytes,
        Encrypted,
        EncryptedBytes
    }

    private static final String TAG = "SymmetricAlgorithmAES";

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
            int half_byte = (b >>> 4) & 0x0F;
            int two_halves = 0;
            do {
                buf.append((0 <= half_byte) && (half_byte <= 9) ? (char) ('0' + half_byte) : (char) ('a' + (half_byte - 10)));
                half_byte = b & 0x0F;
            } while (two_halves++ < 1);
        }
        return buf.toString();
    }

    /**
     * Creates a Sha1 hash from a given string
     * @param text string to hex
     * @return Sha1 Hash
     * @throws NoSuchAlgorithmException Hashing Algorithm not found!
     * @throws UnsupportedEncodingException Encoding unsupported!
     */
    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] textBytes = text.getBytes("UTF-8");
        md.update(textBytes, 0, textBytes.length);
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    /**
     * Instantiates the class Encryption by generating a secret key
     */
    public Encryption() {
        try {
            sks = new SecretKeySpec(SHA1("Chat-RSCM").substring(0,16).getBytes(), "AES");
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
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(message.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
        return MessageType.Encrypted.ordinal() +ENCRYPTION_SEPARATOR+ Base64.encodeToString(encodedBytes, Base64.URL_SAFE);
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
            return type.ordinal()+ENCRYPTION_SEPARATOR+ Base64.encodeToString(messageBytes,Base64.URL_SAFE);
        }

        if(type != MessageType.EncryptedBytes){
            throw new Exception("Wrong encryption method or message type used!");
        }

        // Encode the original data with AES
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, sks);
            encodedBytes = c.doFinal(messageBytes);
        } catch (Exception e) {
            Log.e(TAG, "AES encryption error");
        }
        return MessageType.EncryptedBytes.ordinal() +ENCRYPTION_SEPARATOR+ Base64.encodeToString(encodedBytes, Base64.URL_SAFE);
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
        String result ="";

        switch (MessageType.values()[type]){
            case Decrypted:
            case DecryptedBytes:
                return message;
            case Encrypted:
            case EncryptedBytes:
                // Decode the encoded data with AES
                try {
                    Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
                    c.init(Cipher.DECRYPT_MODE, sks);
                    decodedBytes = c.doFinal(Base64.decode(message, Base64.URL_SAFE));
                    result = (type == MessageType.EncryptedBytes.ordinal()) ? Base64.encodeToString(decodedBytes, Base64.URL_SAFE) : new String(decodedBytes)+ " ";
                } catch (Exception e) {
                    Log.e(TAG, "AES decryption error");
                }
                break;
            default:
                throw new Exception("Wrong decryption method or encryption message type used!");
        }
        return result;
    }
}
