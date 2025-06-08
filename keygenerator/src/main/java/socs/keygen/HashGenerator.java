package socs.keygen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class that provides functionality for hashing messages and constructing keys from them.
*/
public class HashGenerator {
    /** Local MessageDigest variable */
    private  MessageDigest digest;

    /**
     * Constructor with dependency injection.
     * @param digest Instance of MessageDigest
     * @return Instance of HashGenerator
     */
    public HashGenerator(MessageDigest digest) {
        this.digest = digest;
    }
    /**
     * Function that creates hash from given message.
     * @param message Message to hash
     * @return Hashed message in bytes
     */
    public byte[] getHash(String message) {
        return digest.digest(message.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * Function that constructs key from message specific for provided algorithm.
     * @param message Message to hash
     * @param algorithm Algorithm for constructing key from hash
     * @return Key constructed from hashed message
     */
    public SecretKey getHashAsKey(String message, String algorithm) {
        byte[] hash = getHash(message);
        return new SecretKeySpec(hash, 0, hash.length, algorithm);
    }
}
