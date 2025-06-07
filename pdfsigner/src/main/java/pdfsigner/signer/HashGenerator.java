package pdfsigner.signer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class HashGenerator {
    private  MessageDigest digest;

    public HashGenerator(MessageDigest digest) {
        this.digest = digest;
    }
    public byte[] getHash(String message) {
        return digest.digest(message.getBytes(StandardCharsets.UTF_8));
    }
    public SecretKey getHashAsKey(String message) {
        byte[] hash = getHash(message);
        return new SecretKeySpec(hash, 0, hash.length, "AES");
    }
}