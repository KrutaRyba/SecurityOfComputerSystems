package socs.keygen;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeyGenerator {
    private KeyPairGenerator generator;
    private KeyPair keys;

    public KeyGenerator(KeyPairGenerator gen, int keySize) {
        generator = gen;
        generator.initialize(keySize);
    }
    public void generateKeyPair() {
        keys = generator.generateKeyPair();
    }
    public byte[] getPrivateKey() {
        return keys.getPrivate().getEncoded();
    }
    public byte[] getPublicKey() {
        return keys.getPublic().getEncoded();
    }
    public String getKeyHEX(byte[] key) {
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b: key) stringBuilder.append(String.format("%02x", b));
        return stringBuilder.toString();
    }
}
