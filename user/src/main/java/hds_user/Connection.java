package hds_user;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;

public class Connection {
	
	private String serverName;
	private int port;
	private Socket client;
	private DataOutputStream out;
	private DataInputStream in;
	
	public Connection(String serverName, int port) {
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
	 * Sends a request to the notary to know if the good is for sale and who owns it.
	 */
	public String getStateOfGood(int good) throws IOException {
		connect();
		write("getStateOfGood " + Integer.toString(good));
		String reply = read();
		disconnect();
		return reply;
	}

	/**
	 * Sends a request to the notary expressing that a good is for sale.
	 * Fails if user doesn't own it.
	 */
	public boolean intentionToSell(int good) throws IOException {
		connect();
		write("intentionToSell " + Integer.toString(good));
		boolean success = Boolean.valueOf(read());
		disconnect();
		return success;
	}

	/**
	 * Sends a request to the server to change the owner of a good.
	 * Fails if user doesn't own it.
	 */
	public boolean transferGood(int good, int owner) throws IOException {
		connect();
		write("transferGood " + Integer.toString(good) + " " + Integer.toString(owner));
		boolean success = Boolean.valueOf(read());
		disconnect();
		return success;
	}
	
	private String read() throws IOException {
		return in.readUTF();
	}
	
	private void write(String msg) throws IOException {
		out.writeUTF(msg);
	}
	
	public String getAddr() {
		return client.getRemoteSocketAddress().toString();
	}
}
