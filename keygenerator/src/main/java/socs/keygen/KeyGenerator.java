package socs.keygen;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

/**
 * Class that provides functionality for generating pair of private and public keys, converting keys to HEX or Base64 format.
*/
public class KeyGenerator {
    /** Local instance of KeyPairGenerator */
    private KeyPairGenerator generator;

    /**
     * Constructor with dependency injection.
     * @param[in] generator Instance of KeyPairGenerator
     * @param[in] keySize Size of keys
     * @return Instance of KeyGenerator
     */
    public KeyGenerator(KeyPairGenerator generator, int keySize) {
        this.generator = generator;
        this.generator.initialize(keySize);
    }
    /**
     * Function that generates pair of private and public keys.
     * @return Pair of private and public keys
     */
    public KeyPair generateKeyPair() {
        return generator.generateKeyPair();
    }
    /**
     * Function that formats key to HEX.
     * @param[in] key Key to format in bytes
     * @return Key in HEX format
     */
    public String getKeyHEX(byte[] key) {
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b: key) stringBuilder.append(String.format("%02x", b));
        return stringBuilder.toString();
    }
    /**
     * Function that formats key to Base64.
     * @param[in] key Key to format in bytes
     * @return Key in Base64 format
     */
    public String getKeyBase64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }
}
