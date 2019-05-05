package hds_security;

import java.security.*;

public class SignMessage {

	public static byte[] sign(byte[] message, PrivateKey privateKey)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

		Signature ecdsa;
		ecdsa = Signature.getInstance("SHA256withECDSA");
		ecdsa.initSign(privateKey);
		ecdsa.update(message);
		byte[] signature = ecdsa.sign();
		return signature;
	}

	public static boolean verify(byte[] plaintextMessage, byte[] signedMessage, PublicKey publicKey)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature;
		signature = Signature.getInstance("SHA256withECDSA");
		signature.initVerify(publicKey);
		signature.update(plaintextMessage);
		return signature.verify(signedMessage);
	}

	public static boolean verifyServerMsg(byte[] plaintextMessage, byte[] signedMessage, PublicKey publicKey)
			throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature signature;
		signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(publicKey);
		signature.update(plaintextMessage);
		return signature.verify(signedMessage);
	}
}
