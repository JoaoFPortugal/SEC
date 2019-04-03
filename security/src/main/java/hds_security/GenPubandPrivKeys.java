package hds_security;

import java.io.*;
import java.security.*;

public class GenPubandPrivKeys {

    public GenPubandPrivKeys(String filename) throws NoSuchAlgorithmException {
        KeyPair gen_keys = gen_keys();
        store_gen_keys(filename,get_public_key(gen_keys),get_private_key(gen_keys));
    }

    private void store_gen_keys(String filename, PublicKey public_key, PrivateKey private_key) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filename + "_public_key.txt");
            fos.write(public_key.getEncoded());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos = new FileOutputStream(filename + "_private_key.txt");
            fos.write(private_key.getEncoded());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private KeyPair gen_keys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        keyGen.initialize(224,random);
        KeyPair keys = keyGen.generateKeyPair();
        return keys;
    }

    private PrivateKey get_private_key(KeyPair keys) {
        PrivateKey privKey = keys.getPrivate();
        return privKey;
    }

    private PublicKey get_public_key(KeyPair keys){
        PublicKey pubKey = keys.getPublic();
        return pubKey;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        GenPubandPrivKeys gen = new GenPubandPrivKeys("1");
        gen = new GenPubandPrivKeys("2");
        gen = new GenPubandPrivKeys("3");
        gen = new GenPubandPrivKeys("4");
        gen = new GenPubandPrivKeys("5");
    }
}
