package hds_security;

import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

/**
 * Messages sent by users
 */
public class SecureSession {

	// Delay allowed for messages, within this time we'll save nonces
	private final int replayDelayMs = 15*60*1000; // 15 mins
	
	// Timestamp, Nonce
	protected SortedMap<Long, Long> nonceMap = Collections.synchronizedSortedMap(new TreeMap<Long, Long>());
	

	public Message readFromUser(DataInputStream in) throws IOException, InvalidKeyException, InvalidKeySpecException,
			NoSuchAlgorithmException, SignatureException, InvalidSignatureException, IllegalAccessException,
			ReplayAttackException, NullPublicKeyException {

		int msgLen = in.readInt();
		byte[] mbytes = new byte[msgLen];
		in.readFully(mbytes);

		byte[] unwrapmsg = new byte[mbytes.length - Message.length];
		byte[] originalmessage = new byte[Message.length];

		// Get original message
		System.arraycopy(mbytes, 0, originalmessage, 0, Message.length);
		// Get signed hash
		System.arraycopy(mbytes, Message.length, unwrapmsg, 0, unwrapmsg.length);

		Message replyMessage = Message.fromBytes(originalmessage);
		String pubKeyPath = "./src/main/resources/" + replyMessage.origin + "_public_key.txt";
		PublicKey pubKey = LoadKeys.loadPublicKey(pubKeyPath, "RSA");

		// Create hash
		byte[] hashedcontent = HashMessage.hashBytes(originalmessage);
		
		// Verify if hashes are the same, using EC
		if (!SignMessage.verify(hashedcontent, unwrapmsg, pubKey)) {
			throw new InvalidSignatureException();
		}

		verifyFreshness(replyMessage);

		return replyMessage;
	}


	public Message readFromUser(DataInputStream in, String name) throws IOException, InvalidKeyException, InvalidKeySpecException,
			NoSuchAlgorithmException, SignatureException, InvalidSignatureException, IllegalAccessException,
			ReplayAttackException, NullPublicKeyException {

		int msgLen = in.readInt();
		byte[] mbytes = new byte[msgLen];
		in.readFully(mbytes);

		byte[] unwrapmsg = new byte[mbytes.length - Message.length];
		byte[] originalmessage = new byte[Message.length];

		// Get original message
		System.arraycopy(mbytes, 0, originalmessage, 0, Message.length);
		// Get signed hash
		System.arraycopy(mbytes, Message.length, unwrapmsg, 0, unwrapmsg.length);
		Message replyMessage = Message.fromBytes(originalmessage);
		String pubKeyPath = "./src/main/resources/" + name + "_public_key.txt";
		PublicKey pubKey = LoadKeys.loadPublicKey(pubKeyPath, "RSA");

		// Create hash
		byte[] hashedcontent = HashMessage.hashBytes(originalmessage);

		// Verify if hashes are the same, using EC
		if (!SignMessage.verify(hashedcontent, unwrapmsg, pubKey)) {
			throw new InvalidSignatureException();
		}

		verifyFreshness(replyMessage);

		return replyMessage;
	}
	
	public Message readFromCC(DataInputStream in, String serverPubKeyPath)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException,
			InvalidSignatureException, NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		PublicKey pubKey = LoadKeys.loadPublicKey(serverPubKeyPath, "RSA");
		
		int msgLen = in.readInt();
		byte[] mbytes = new byte[msgLen];
		in.readFully(mbytes);

		byte[] unwrapmsg = new byte[mbytes.length - Message.length];
		byte[] originalmessage = new byte[Message.length];

		// Get original message
		System.arraycopy(mbytes, 0, originalmessage, 0, Message.length);
		// Get signed hash
		System.arraycopy(mbytes, Message.length, unwrapmsg, 0, unwrapmsg.length);

		Message replyMessage = Message.fromBytes(originalmessage);

		// Create hash
		byte[] hashedcontent = HashMessage.hashBytes(originalmessage);
		
		// Verify if hashes are the same, using RSA
		if (!SignMessage.verifyServerMsg(hashedcontent, unwrapmsg, pubKey)) {
			throw new InvalidSignatureException();
		}

		verifyFreshness(replyMessage);

		return replyMessage;
	}

	protected void verifyFreshness(Message replyMessage) throws ReplayAttackException {
		long now = Utils.createTimeStamp();
		
		/**
		 * Remove entries with timestamp less than 'now' from the nonce collection.
		 * Note: The method returns the entries removed.
		 * 
		 * https://www.geeksforgeeks.org/treemap-headmap-method-in-java/ 
		 */
		nonceMap.headMap(now);
		
		long msgNow = replyMessage.getNow();
		long msgNonce = replyMessage.getNonce();
		Long nonce = nonceMap.get(msgNow);
		if ((now - replayDelayMs >= msgNow) || (nonce != null && nonce == msgNonce)) {
			throw new ReplayAttackException();
		} else {
			nonceMap.put(msgNow, msgNonce);
		}
	}
}