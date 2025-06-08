package socs.keygen;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;

/**
 * Class that provides functionality for encrypting and decrypting messages using AES algorithm, generating IV.
*/
public class AESCipher {
    /** Local Cipher variable */
    private Cipher cipher;

    /**
     * Constructor with dependency injection.
     * @param cipher Instance of Cipher
     * @return Instance of AESCipher
    */
    public AESCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    /**
     * Function that encrypts given message using provided key and IV (in case of mode other than ECB).
     * @param key Key for used for encryption
     * @param message Message to encrypt
     * @param iv Initialization vector (can be null if encryption mode is ECB)
     * @return Encrypted message in bytes
     * @throws InvalidKeyException Key is ivalid
     * @throws IllegalBlockSizeException Length of data does not match the block size of the cipher
     * @throws BadPaddingException Data is not padded properly
     * @throws InvalidAlgorithmParameterException Invalid or inappropriate IV
    */
    public byte[] encrypt(Key key, byte[] message, IvParameterSpec iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String mode = cipher.getAlgorithm().split("/")[1];
        if (mode.equals("ECB")) cipher.init(Cipher.ENCRYPT_MODE, key);
        else cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(message);
    }
    /**
     * Function that decrypts given message using provided key and IV (in case of mode other than ECB).
     * @param key Key for used for decryption
     * @param message Message to decrypt
     * @param iv Initialization vector (can be null if encryption mode is ECB)
     * @return Decrypted message in bytes
     * @throws InvalidKeyException Key is ivalid
     * @throws IllegalBlockSizeException Length of data does not match the block size of the cipher
     * @throws BadPaddingException Data is not padded properly
     * @throws InvalidAlgorithmParameterException Invalid or inappropriate IV
     */
    public byte[] decrypt(Key key, byte[] message, IvParameterSpec iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String mode = cipher.getAlgorithm().split("/")[1];
        if (mode.equals("ECB")) cipher.init(Cipher.DECRYPT_MODE, key);
        else cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(message);
    }
    /**
     * Function that generates initialization vector using block size from local Cipher instance.
     * @return Initialization vector in bytes
     * @throws NoSuchAlgorithmException Particular cryptographic algorithm is requested but is not available
     */
    public IvParameterSpec generateIV() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[cipher.getBlockSize()];
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
