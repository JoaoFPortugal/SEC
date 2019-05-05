package hds_security;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class GenerateKeys {

	private final String password;
	private String filename;

	public GenerateKeys(String filename, String password) throws NoSuchAlgorithmException {
		this.filename = filename;
		this.password = password;
	}

	public void createFiles() throws NoSuchAlgorithmException {
		KeyPair keys = generateKeys();
		createPublicKeyFiles(filename, keys.getPublic());
		createEncryptedPrivateKeyFiles(keys.getPrivate());
	}
	
	private KeyPair generateKeys() throws NoSuchAlgorithmException {
		/**
		 * Gets the type of algorithm to use in the key-pair generation. In this case it
		 * is Elliptical Curves (EC), but could be RSA, ElGammal... EC is the key with
		 * smaller size, more efficient with equivalent security.
		 */
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
		// SHA1PRNG is the standard to generate secure randoms
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		// Use 224 key size
		keyGen.initialize(224, random);
		KeyPair keys = keyGen.generateKeyPair();
		return keys;
	}

	private void createPublicKeyFiles(String filename, PublicKey public_key) {
		try (FileOutputStream fos = new FileOutputStream(filename + "_public_key.txt");) {
			fos.write(public_key.getEncoded());
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void createEncryptedPrivateKeyFiles(PrivateKey privateKey) {
		StrongPasswordGenerator strongPasswordGenerator = new StrongPasswordGenerator(password);
		String strongPassword = null;
		try {
			strongPassword = strongPasswordGenerator.generateStrongPasswordHash();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		String[] parts = strongPassword.split(":");
		storeEncryptedPrivateKey(Utility.hexToBytes(parts[2]), filename, privateKey);
		storeSalt(Utility.hexToBytes(parts[1]), filename);
		storePasswordHash(Utility.hexToBytes(parts[2]), filename);
	}

	private void storeEncryptedPrivateKey(byte[] key, String filename, PrivateKey privateKey) {
		SymmetricKeyEncryption symmetricKeyEncryption = new SymmetricKeyEncryption(key);
		try (FileOutputStream fos = new FileOutputStream(filename + "_private_key.txt");) {
			byte[] encryptedMessage = symmetricKeyEncryption.encrypt(privateKey.getEncoded());
			fos.write(encryptedMessage);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void storeSalt(byte[] salt, String filename) {
		try (FileOutputStream fos = new FileOutputStream(filename + "_salt.txt");) {
			fos.write(salt);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void storePasswordHash(byte[] password, String filename) {
		byte[] hashedMessage = HashMessage.hashBytes(password);

		try (FileOutputStream fos = new FileOutputStream(filename + "_hash.txt");) {
			fos.write(hashedMessage);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void main(String[] args) throws NoSuchAlgorithmException {
		new GenerateKeys("1", "11").createFiles();
		new GenerateKeys("2", "22").createFiles();
		new GenerateKeys("3", "33").createFiles();
		new GenerateKeys("4", "44").createFiles();
		new GenerateKeys("5", "55").createFiles();
	}
}
