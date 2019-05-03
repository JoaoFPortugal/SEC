package hds_security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashMessage {

	public byte[] hashMessage(String message) {
		MessageDigest digest = null;
		try {
			/**
			 * A family of two similar hash functions, with different block sizes, known as
			 * SHA-256 and SHA-512. They differ in the word size; SHA-256 uses 32-bit words
			 * where SHA-512 uses 64-bit words. There are also truncated versions of each
			 * standard, known as SHA-224, SHA-384, SHA-512/224 and SHA-512/256. These were
			 * also designed by the NSA.
			 */
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] hashedMessage = digest.digest(message.getBytes(StandardCharsets.UTF_8));
		return hashedMessage;
	}

	public byte[] hashBytes(byte[] bytes) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] hashedMessage = digest.digest(bytes);
		return hashedMessage;
	}
}
