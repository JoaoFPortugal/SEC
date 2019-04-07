package hds_security;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class GenPubAndPrivKeys {

	private final String password;
	private String filename;

	public GenPubAndPrivKeys(String filename,String password) throws NoSuchAlgorithmException {
		this.filename = filename;
		this.password = password;
	}

	public void genKeyFiles() throws NoSuchAlgorithmException {
		KeyPair gen_keys = gen_keys();
		store_gen_keys(filename, get_public_key(gen_keys), get_private_key(gen_keys));
	}

	private void store_gen_keys(String filename, PublicKey public_key, PrivateKey private_key) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream( filename + "_public_key.txt");
			fos.write(public_key.getEncoded());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		encryptFiles(private_key);
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


	private void encryptFiles(PrivateKey privateKey) {

		StrongPasswordGenerator strongPasswordGenerator = new StrongPasswordGenerator(password);
		String strongPassword = null;
		try {
			strongPassword = strongPasswordGenerator.generateStrongPasswordHash();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		String[] parts = strongPassword.split(":");
		storeSalt(fromHex(parts[1]),filename);
		encryptPrivateKey(fromHex(parts[2]),filename,privateKey);
		hashPassword(filename,fromHex(parts[2]));

	}

	private void storeSalt(byte[] salt,String filename) {
		try {
			FileOutputStream fos = new FileOutputStream("saltfile" + filename + ".txt");
			fos.write(salt);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void encryptPrivateKey(byte[] key, String filename, PrivateKey privateKey ) {

		SymmetricKeyEncryption symmetricKeyEncryption = new SymmetricKeyEncryption(key);
		try {

			File privateKeyFile = new File( filename + "_private_key.txt");
			privateKeyFile.createNewFile();
			//String encryptingThis = "Ola";
			//byte[] encryptedMessage = symmetricKeyEncryption.encrypt(encryptingThis.getBytes(Charset.defaultCharset()));
			byte[] encryptedMessage = symmetricKeyEncryption.encrypt(privateKey.getEncoded());
			FileOutputStream fos = new FileOutputStream(filename + "_private_key.txt");
			fos.write(encryptedMessage);
			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void hashPassword(String filename, byte[] password) {
		HashMessage hashMessage = new HashMessage();

		byte[] hashedMessage = hashMessage.hashBytes(password);

		try {
			FileOutputStream fos = new FileOutputStream("hashedPasswordfile" + filename + ".txt");
			fos.write(hashedMessage);
			fos.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] fromHex(String hex)
	{
		byte[] bytes = new byte[hex.length() / 2];
		for(int i = 0; i<bytes.length ;i++)
		{
			bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}


	public static void main(String[] args) throws NoSuchAlgorithmException {
		new GenPubAndPrivKeys("1","11").genKeyFiles();
		new GenPubAndPrivKeys("2","22").genKeyFiles();
		new GenPubAndPrivKeys("3","33").genKeyFiles();
		new GenPubAndPrivKeys("4","44").genKeyFiles();
		new GenPubAndPrivKeys("5","55").genKeyFiles();
	}
}
