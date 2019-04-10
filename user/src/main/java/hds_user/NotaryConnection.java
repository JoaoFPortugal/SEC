package hds_user;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import hds_security.HashMessage;
import hds_security.Message;
import hds_security.SignMessage;
import hds_user.exceptions.*;
import notary.exceptions.InvalidSignatureException;

import java.util.Date;

public class NotaryConnection {

	private String serverName;
	private int port;
	private Socket client;
	private DataOutputStream out;
	private DataInputStream in;
	private User user;

	public NotaryConnection(String serverName, int port, User user) {
		this.serverName = serverName;
		this.port = port;
		this.user = user;
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
	 *
	 * @throws InexistentGoodException
	 */

	public Good getStateOfGood(int gid, int uid) throws IOException, InvalidSignatureException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		connect();
		Date date = new Date();
		long now = date.getTime();
		Message message = new Message(uid, -1, 'G', now, gid);

		write(message);

		byte[] reply = readFromServer();

		Message replyMessage = Message.fromBytes(reply);

		Good g = new Good(gid, replyMessage.getOrigin(), replyMessage.getContent() == 1);

		disconnect();
		return g;

	}

	public byte[] readFromServer() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidSignatureException {
		PublicKey pubKey = loadServerPublicKey();
		assert pubKey != null;
		int msgLen = in.readInt();
		byte[] mbytes = new byte[msgLen];
		in.readFully(mbytes);
		byte[] unwrapmsg = new byte[mbytes.length-22];
		byte[] originalmessage = new byte[22];

		System.arraycopy(mbytes,0,originalmessage,0,22);
		System.arraycopy(mbytes,22,unwrapmsg,0,unwrapmsg.length);

		HashMessage hashedoriginal = new HashMessage();
		byte[] hashedcontent = hashedoriginal.hashBytes(originalmessage);
		SignMessage sign = new SignMessage();

		if(!sign.verify(hashedcontent,unwrapmsg,pubKey)){
			throw new InvalidSignatureException();
		}
		return originalmessage;
	}

	private PublicKey loadServerPublicKey() {
		PublicKey pub;
		try {
			FileInputStream fis = new FileInputStream("./src/main/resources/serverPublicKey.txt");
			byte[] pubKey = fis.readAllBytes();
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			pub = kf.generatePublic(keySpec);
			return pub;
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Sends a request to the notary expressing that a good is for sale. Fails if
	 * user doesn't own it.
	 */
	public int intentionToSell(int gid, int uid) throws IOException, InvalidSignatureException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		connect();
		Date date = new Date();
		long now = date.getTime();
		Message message = new Message(uid, -1, 'S', now, gid);

		write(message);

		byte[] reply = readFromServer();
		Message replyMessage = Message.fromBytes(reply);

		disconnect();
		return(replyMessage.getContent());
	}

	/**
	 * Sends a request to the server to change the owner of a good. Fails if user
	 * doesn't own it.
	 */

	public Message transferGood(int good, int owner, int buyer) throws IOException, InvalidSignatureException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		connect();
		Date date = new Date();
		long now = date.getTime();
		Message message = new Message(owner, buyer, 'T', now, good);

		write(message);

		byte[] reply = readFromServer();
		Message replyMessage = Message.fromBytes(reply);

		disconnect();
		return replyMessage;
	}


	private byte[] read() throws IOException {
		int msgLen = in.readInt();
		byte[] msg = new byte[msgLen];
		in.readFully(msg);
		return msg;
	}

	public byte[] cypher(Message message){

		byte[] msg = message.toBytes();
		HashMessage hashMessage = new HashMessage();
		SignMessage signMessage = new SignMessage();
		byte[] finalmsg = new byte[0];
		try {
			byte[] signedmessage = signMessage.sign(hashMessage.hashBytes(msg), user.getPrivateKey());
			finalmsg = new byte[msg.length + signedmessage.length];
			System.arraycopy(msg, 0, finalmsg, 0, msg.length);
			System.arraycopy(signedmessage, 0, finalmsg, msg.length, signedmessage.length);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
		return finalmsg;
	}

	private void write(Message message) throws IOException {
		byte[] finalmsg = cypher(message);
		out.writeInt(finalmsg.length);
		out.write(finalmsg, 0, finalmsg.length);
	}
}
