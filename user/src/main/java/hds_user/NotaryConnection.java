package hds_user;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import hds_security.Message;
import hds_security.SecureSession;
import hds_security.Utils;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullDestination;
import hds_security.exceptions.NullPrivateKeyException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

public class NotaryConnection {

	private String serverName;
	private int port;
	private Socket client;
	private DataOutputStream out;
	private DataInputStream in;
	private User user;
	private SecureSession notarySS;
	private String serverPubKeyPath;

	public NotaryConnection(String serverName, int port, User user) {
		this.serverName = serverName;
		this.port = port;
		this.user = user;
		notarySS = new SecureSession();
		this.serverPubKeyPath = "./src/main/resources/serverPublicKey.txt";
	}

	private void connect() throws IOException {
		client = new Socket(serverName, port);
		out = new DataOutputStream(client.getOutputStream());
		in = new DataInputStream(client.getInputStream());
	}

	private void disconnect() throws IOException {
		client.close();
	}

	/**
	 * Sends a request to the notary to know if the good is for sale and who owns
	 * it. Returns a Good object on success.
	 */

	public Good getStateOfGood(int gid, int uid) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect();

		Utils.write(new Message(uid, 'G', gid), out, user.getPrivateKey());

		Message replyMessage = notarySS.readFromCC(in, serverPubKeyPath);

		// Origin value of reply is actually the 'owner' value.
		// Good ID value of reply is actually the 'for_sale' value.
		if(replyMessage.getOrigin() < 0) {
			return null;
		}
		Good g = new Good(gid, replyMessage.getOrigin(), replyMessage.getGoodID() == 1);

		disconnect();
		return g;
	}

	/**
	 * Sends a request to the notary expressing that a good is for sale. Fails if
	 * user doesn't own it.
	 */
	public int intentionToSell(int gid, int uid) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect();

		Utils.write(new Message(uid, 'S', gid), out, user.getPrivateKey());

		Message replyMessage = notarySS.readFromCC(in, serverPubKeyPath);

		disconnect();
		// Good id contains 'for_sale' value.
		return (replyMessage.getGoodID());
	}

	/**
	 * Sends a request to the server to change the owner of a good. Fails if user
	 * doesn't own it. Returns good ID on success.
	 */

	public Message transferGood(int good, int owner, int buyer) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect();

		Utils.write(new Message(owner, buyer, 'T', good), out, user.getPrivateKey());

		Message replyMessage = notarySS.readFromCC(in, serverPubKeyPath);

		disconnect();
		return replyMessage;
	}

}
