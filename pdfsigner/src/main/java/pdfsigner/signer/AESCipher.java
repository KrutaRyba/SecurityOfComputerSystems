
package pdfsigner.signer;

import java.security.InvalidKeyException;
import java.security.Key;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class AESCipher {
    private Cipher cipher;

    public AESCipher(Cipher cipher) {
        this.cipher = cipher;
    }
    public byte[] encrypt(Key key, byte[] message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(message);
    }
    public byte[] decrypt(Key key, byte[] message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(message);
    }
}
