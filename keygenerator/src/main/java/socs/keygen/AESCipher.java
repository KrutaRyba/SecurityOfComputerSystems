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

public class AESCipher {
    private Cipher cipher;

    public AESCipher(Cipher cipher) {
        this.cipher = cipher;
    }
    public byte[] encrypt(Key key, byte[] message, IvParameterSpec iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String mode = cipher.getAlgorithm().split("/")[1];
        if (mode.equals("ECB")) cipher.init(Cipher.ENCRYPT_MODE, key);
        else cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(message);
    }
    public byte[] decrypt(Key key, byte[] message, IvParameterSpec iv) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        String mode = cipher.getAlgorithm().split("/")[1];
        if (mode.equals("ECB")) cipher.init(Cipher.DECRYPT_MODE, key);
        else cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(message);
    }
    public IvParameterSpec generateIV() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[cipher.getBlockSize()];
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
