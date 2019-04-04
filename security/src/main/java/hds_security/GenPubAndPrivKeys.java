package hds_security;

import java.io.*;
import java.security.*;

public class GenPubAndPrivKeys {

	private String filename;

	public GenPubAndPrivKeys(String filename) throws NoSuchAlgorithmException {
		this.filename = filename;
	}

	public void genKeyFiles() throws NoSuchAlgorithmException {
		KeyPair gen_keys = gen_keys();
		store_gen_keys(filename, get_public_key(gen_keys), get_private_key(gen_keys));
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
		/**
		 * Gets the type of algorithm to use in the key-pair generation. In this case it
		 * is Elliptical Curves (EC), but could be RSA, ElGammal... EC is the key with
		 * smaller size, more efficient with equivalent security.
		 */
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
		// SHA1PRNG is the standard to generate secure randoms
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		// EC uses 224 key size
		keyGen.initialize(224, random);
		KeyPair keys = keyGen.generateKeyPair();
		return keys;
	}

	private PrivateKey get_private_key(KeyPair keys) {
		PrivateKey privKey = keys.getPrivate();
		return privKey;
	}

	private PublicKey get_public_key(KeyPair keys) {
		PublicKey pubKey = keys.getPublic();
		return pubKey;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException {
		new GenPubAndPrivKeys("1").genKeyFiles();
		new GenPubAndPrivKeys("2").genKeyFiles();
		new GenPubAndPrivKeys("3").genKeyFiles();
		new GenPubAndPrivKeys("4").genKeyFiles();
		new GenPubAndPrivKeys("5").genKeyFiles();
	}
}
