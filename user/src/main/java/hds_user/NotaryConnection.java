package hds_user;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;

import hds_security.HashMessage;
import hds_security.Message;
import hds_security.SignMessage;
import hds_user.exceptions.*;

import java.io.IOException;
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

	public Good getStateOfGood(int gid, int uid) throws IOException {
		connect();
		Date date = new Date();
		long now = date.getTime();
		Message message = new Message(uid, -1, 'G', now, gid);

		write(message);

		byte[] reply = read();

		Message replyMessage = Message.fromBytes(reply);

		Good g = new Good(gid, replyMessage.getOrigin(), replyMessage.getContent() == 1 ? true : false);

		disconnect();
		return g;

	}



	public ArrayList<Good> getListOfGoods() throws IOException, InexistentGoodsException {
		connect();

		// TODO
		//write("getListOfGoods");

		String reply = "bla";//read();

		if (reply.isEmpty()) {
			disconnect();
			throw new IOException();
		}

		if (reply.startsWith("null")) {
			disconnect();
			throw new InexistentGoodsException();
		}

		String[] tokens = reply.split(" ");

		ArrayList<Good> list = new ArrayList<Good>();

		for (int i = 0; i < tokens.length; i += 3) {
			int gid, owner;
			try {
				// test if good id is an integer
				gid = Integer.parseInt(tokens[i]);
				// test if owner is an integer
				owner = Integer.parseInt(tokens[i + 1]);
				Good g = new Good(gid, owner, Boolean.valueOf(tokens[i + 2]));
				list.add(g);
			} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
				disconnect();
				throw new IOException();
			}
		}

		disconnect();
		return list;
	}

	public ArrayList<UserInfo> getListOfUsers() throws IOException, InexistentGoodsException {
		connect();
		// TODO
		//write("getListOfUsers");


		ArrayList<UserInfo> list = new ArrayList<UserInfo>();

		disconnect();
		return list;
	}

	/**
	 * Sends a request to the notary expressing that a good is for sale. Fails if
	 * user doesn't own it.
	 */
	public int intentionToSell(int gid, int uid) throws IOException {
		connect();
		Date date = new Date();
		long now = date.getTime();
		Message message = new Message(uid, -1, 'S', now, gid);

		write(message);

		byte[] reply = read();
		Message replyMessage = Message.fromBytes(reply);

		disconnect();
		return(replyMessage.getContent());
	}

	/**
	 * Sends a request to the server to change the owner of a good. Fails if user
	 * doesn't own it.
	 */

	public int transferGood(int good, int owner, int buyer) throws IOException {
		connect();
		Date date = new Date();
		long now = date.getTime();
		Message message = new Message(owner, buyer, 'T', now, good);

		write(message);

		byte[] reply = read();
		Message replyMessage = Message.fromBytes(reply);

		disconnect();
		return replyMessage.getContent();
	}

	//public int buyGood(int good, int owner, int buyer) throws IOException {



	//}

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
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return finalmsg;
	}

	private void write(Message message) throws IOException {
		byte[] finalmsg = cypher(message);
		out.writeInt(finalmsg.length);
		out.write(finalmsg, 0, finalmsg.length);
	}

	public String getAddr() {
		return client.getRemoteSocketAddress().toString();
	}
}
