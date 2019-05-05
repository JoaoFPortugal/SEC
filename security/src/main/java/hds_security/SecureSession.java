package hds_security;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;

import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

/**
 * Messages sent by users
 */
public class SecureSession {

	// Delay allowed for messages, within this time we'll save nonces
	private final int replayDelayMs = 3600*24; // 24 hours
	
	// XXX - replace with TreeMap? must be thread safe
	// XXX - make method to periodically clean nonces older than `replayDelayMs`
	protected Hashtable<Long, Long> nonceTable = new Hashtable<>();
	

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
		PublicKey pubKey = loadPublicKey(pubKeyPath, "EC");

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
		PublicKey pubKey = loadPublicKey(serverPubKeyPath, "RSA");
		
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

	protected void verifyFreshness(Message replyMessage) throws ReplayAttackException {
		long now = Utility.createTimeStamp();
		long msgNow = replyMessage.getNow();
		long msgNonce = replyMessage.getNonce();
		Long nonce = nonceTable.get(msgNow);
		if ((now - replayDelayMs >= msgNow) || (nonce != null && nonce == msgNonce)) {
			throw new ReplayAttackException();
		} else {
			nonceTable.put(msgNow, msgNonce);
		}
	}

	/**
	 * Creates an hash of the message and signs the hash
	 */
	private static byte[] sign(Message msg, PrivateKey privKey)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		byte[] mbytes = msg.toBytes();
		byte[] signedmessage = SignMessage.sign(HashMessage.hashBytes(mbytes), privKey);
		// Final message = message + signed hash
		byte[] finalmsg = new byte[mbytes.length + signedmessage.length];
		System.arraycopy(mbytes, 0, finalmsg, 0, mbytes.length);
		System.arraycopy(signedmessage, 0, finalmsg, mbytes.length, signedmessage.length);
		return finalmsg;
	}

	public static void write(Message msg, DataOutputStream out, PrivateKey privKey)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		byte[] finalmsg = sign(msg, privKey);
		out.writeInt(finalmsg.length);
		out.write(finalmsg, 0, finalmsg.length);
	}
	
	private static byte[] signWithCC(Message msg) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, PteidException, PKCS11Exception {
		CitizenCard citizenCard = new CitizenCard();
		byte[] firstmsg = msg.toBytes();
		byte[] secondmsg = citizenCard.signMessage(HashMessage.hashBytes(firstmsg));
		// Final message = message + signed hash
		byte[] finalmsg = new byte[firstmsg.length + secondmsg.length];
		System.arraycopy(firstmsg, 0, finalmsg, 0, firstmsg.length);
		System.arraycopy(secondmsg, 0, finalmsg, firstmsg.length, secondmsg.length);
		return finalmsg;
	}

	public static void writeWithCC(Message message, DataOutputStream out) throws IOException, PteidException, InvocationTargetException,
			IllegalAccessException, PKCS11Exception, NoSuchMethodException, ClassNotFoundException {
		byte[] finalmsg = signWithCC(message);
		out.writeInt(finalmsg.length);
		out.write(finalmsg, 0, finalmsg.length);
	}
}