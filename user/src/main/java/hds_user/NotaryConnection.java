package hds_user;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import hds_security.Message;
import hds_user.exceptions.*;

import java.io.IOException;
import java.util.Date;

public class NotaryConnection {

	private String serverName;
	private int port;
	private Socket client;
	private DataOutputStream out;
	private DataInputStream in;

	public NotaryConnection(String serverName, int port) {
		this.serverName = serverName;
		this.port = port;
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

	/*
	 * public Good getStateOfGood(int gid) throws IOException,
	 * InexistentGoodException { connect(); write("getStateOfGood " +
	 * Integer.toString(gid));
	 * 
	 * String reply = read();
	 * 
	 * if (reply.isEmpty()) { disconnect(); throw new IOException(); }
	 * 
	 * if (reply.startsWith("null")) { disconnect(); throw new
	 * InexistentGoodException(gid); }
	 * 
	 * String[] tokens = reply.split(" "); if (tokens.length != 2) { disconnect();
	 * throw new IOException(); }
	 * 
	 * int owner; // test if owner is an integer try { owner =
	 * Integer.parseInt(tokens[0]); } catch (NumberFormatException e) {
	 * disconnect(); throw new IOException(); } Good g = new Good(gid, owner,
	 * Boolean.valueOf(tokens[1]));
	 * 
	 * disconnect(); return g; }
	 */

	/*
	 * public ArrayList<Good> getListOfGoods() throws IOException,
	 * InexistentGoodsException { connect();
	 * 
	 * write("getListOfGoods");
	 * 
	 * String reply = read();
	 * 
	 * if (reply.isEmpty()) { disconnect(); throw new IOException(); }
	 * 
	 * if (reply.startsWith("null")) { disconnect(); throw new
	 * InexistentGoodsException(); }
	 * 
	 * String[] tokens = reply.split(" ");
	 * 
	 * ArrayList<Good> list = new ArrayList<Good>();
	 * 
	 * for (int i = 0; i < tokens.length; i+=3) { int gid, owner; try { // test if
	 * id is an integer gid = Integer.parseInt(tokens[i]); // test if owner is an
	 * integer owner = Integer.parseInt(tokens[i+1]); Good g = new Good(gid, owner,
	 * Boolean.valueOf(tokens[i+2])); list.add(g); } catch (NumberFormatException |
	 * ArrayIndexOutOfBoundsException e) { disconnect(); throw new IOException(); }
	 * }
	 * 
	 * disconnect(); return list; }
	 */

	/**
	 * Sends a request to the notary expressing that a good is for sale. Fails if
	 * user doesn't own it.
	 */
	public boolean intentionToSell(int gid, int uid) throws IOException {
		connect();
		Date date = new Date();
		long now = date.getTime();
		Message message = new Message(uid, -1, 'S', now, gid);

		write(message);

		byte[] reply = read();
		Message replyMessage = message.fromBytes(reply);

		System.out.println(replyMessage.getContent());

		disconnect();
		return true;
	}

	/**
	 * Sends a request to the server to change the owner of a good. Fails if user
	 * doesn't own it.
	 */
	/*
	 * public boolean transferGood(int good, int owner) throws IOException {
	 * connect(); write("transferGood " + Integer.toString(good) + " " +
	 * Integer.toString(owner)); boolean success = Boolean.valueOf(read());
	 * disconnect(); return success; }
	 */

	private byte[] read() throws IOException {
		int msgLen = in.readInt();
		byte[] msg = new byte[msgLen];
		in.readFully(msg);
		return msg;
	}

	private void write(Message message) throws IOException {
		// message.print();
		byte[] msg = message.toBytes();
		out.writeInt(msg.length);
		out.write(msg, 0, msg.length);
	}

	public String getAddr() {
		return client.getRemoteSocketAddress().toString();
	}
}
