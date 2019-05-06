package hds_security;

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
import java.util.Arrays;

import hds_security.exceptions.NullPrivateKeyException;
import hds_security.exceptions.NullPublicKeyException;

public class LoadKeys {
	public static PublicKey loadPublicKey(String pubKeyPath, String algorithm)
			throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, NullPublicKeyException {
		byte[] pub = Files.readAllBytes(Paths.get(pubKeyPath));
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pub);
		KeyFactory kf = KeyFactory.getInstance(algorithm);
		PublicKey pk = kf.generatePublic(keySpec);
		if (pk == null) {
			throw new NullPublicKeyException();
		}
		return pk;
	}

	/* PRIVATE KEY LOAD - START */
	public static PrivateKey loadPrivateKey(String privKeyPath, String saltPath, String hashPath, String password)
			throws Exception {
		String strongPassword = invertPBKDF2(privKeyPath, saltPath, hashPath, password);
		if (strongPassword != null) {
			byte[] privateKey = loadAndDecryptKey(privKeyPath, strongPassword);
			PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKey);
			KeyFactory kf = KeyFactory.getInstance("EC");
			return kf.generatePrivate(ks);
		} else {
			throw new NullPrivateKeyException();
		}
	}

	private static String invertPBKDF2(String privKeyPath, String saltPath, String hashPath, String password)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		// load salt
		byte[] salt = loadSalt(saltPath);

		String key = generatePassword(password, salt);

		String parts[] = key.split(":");

		// generate hash of key
		byte[] finalKey = Utils.hexToBytes(parts[2]);

		byte[] hashedMessage = HashMessage.hashBytes(finalKey);

		// load hash from file
		byte[] hashedPassword = loadHash(hashPath);

		// verify if hashes match
		if (Arrays.equals(hashedMessage, hashedPassword)) {
			return parts[2];
		}
		return null;
	}

	private static byte[] loadAndDecryptKey(String privKeyPath, String strongPassword) throws Exception {
		byte[] privateKeyEncoded = Files.readAllBytes(Paths.get(privKeyPath));
		SymmetricKeyEncryption symmetricKeyEncryption = new SymmetricKeyEncryption(Utils.hexToBytes(strongPassword));
		return symmetricKeyEncryption.decrypt(privateKeyEncoded);
	}

	private static String generatePassword(String password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		StrongPasswordGenerator pass = new StrongPasswordGenerator(password);
		return pass.generateStrongPasswordHash(salt);
	}

	private static byte[] loadSalt(String saltPath) throws IOException {
		return Files.readAllBytes(Paths.get(saltPath));
	}

	private static byte[] loadHash(String hashPath) throws IOException {
		return Files.readAllBytes(Paths.get(hashPath));
	}
	/* PRIVATE KEY LOAD - END */
}
