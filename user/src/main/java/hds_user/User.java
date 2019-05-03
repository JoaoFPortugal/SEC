package hds_user;

import hds_security.HashMessage;
import hds_security.StrongPasswordGenerator;
import hds_security.SymmetricKeyEncryption;
import hds_security.Utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User {

	private String password;
	private List<Good> setOfGoods;
	private List<UserInfo> setOfUsers;
	private final int uid;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	String strongpassword;

	public User(int id, String password) {
		this.uid = id;
		loadPubKey();
		this.password = password;
		loadPrivKey();
		setOfGoods = new ArrayList<>();
		setOfUsers = new ArrayList<>();
	}

	private void loadPubKey() {
		try {
			byte[] pub = Files.readAllBytes(Paths.get("./src/main/resources/" + uid + "_public_key.txt"));
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pub);
			KeyFactory kf = KeyFactory.getInstance("EC");
			this.publicKey = kf.generatePublic(keySpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void loadPrivKey() {
		boolean left = invertPBKDF2(password, uid);
		if (left) {
			byte[] privateKey = loadKey(uid);
			try {
				PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKey);
				KeyFactory kf = KeyFactory.getInstance("EC");
				this.privateKey = kf.generatePrivate(ks);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private byte[] loadKey(int uid) {
		SymmetricKeyEncryption symmetricKeyEncryption = new SymmetricKeyEncryption(Utility.hexToBytes(strongpassword));
		byte[] decryptedPrivateKey = new byte[0];
		try {
			byte[] privateKeyEncoded = Files
					.readAllBytes(Paths.get("./src/main/resources/" + uid + "_private_key.txt"));
			decryptedPrivateKey = symmetricKeyEncryption.decrypt(privateKeyEncoded);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPrivateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	private boolean invertPBKDF2(String password, int uid) {
		// load salt
		byte[] salt = loadSalt(uid);

		String key = generatePassword(password, salt);

		String parts[] = key.split(":");

		// generate hash of key

		byte[] finalKey = Utility.hexToBytes(parts[2]);

		HashMessage hashMessage = new HashMessage();
		byte[] hashedMessage = hashMessage.hashBytes(finalKey);

		// load hash from file

		byte[] hashedPassword = loadHash(uid);

		if (Arrays.equals(hashedMessage, hashedPassword)) {
			strongpassword = parts[2];
			return true;
		}
		return false;
	}

	private String generatePassword(String password, byte[] salt) {
		StrongPasswordGenerator pass = new StrongPasswordGenerator(password);
		try {
			return pass.generateStrongPasswordHash(salt);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	private byte[] loadHash(int uid) {
		try {
			byte[] pub = Files.readAllBytes(Paths.get("./src/main/resources/" + uid + "_hash.txt"));
			return pub;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	private byte[] loadSalt(int uid) {
		try {
			byte[] pub = Files.readAllBytes(Paths.get("./src/main/resources/" + uid + "_salt.txt"));
			return pub;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

}