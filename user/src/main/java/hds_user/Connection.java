package hds_user;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.io.IOException;

public class Connection implements ILibrary {
	
	private Socket client;
	private DataOutputStream out;
	private DataInputStream in;
	
	public Connection(String serverName, int port) throws IOException {
		client = new Socket(serverName, port);
		out = new DataOutputStream(client.getOutputStream());
		in = new DataInputStream(client.getInputStream());
	}

	/**
	 * Sends a request to the notary to know if the good is for sale and who owns it.
	 */
	public String getStateofGood(Object o) throws IOException {
		send();
		return read();
	}

	/**
	 * Sends a request to the notary expressing that a good is for sale.
	 * Fails if user doesn't own it.
	 */
	public boolean intentionToSell() {

	}

	/**
	 * Sends a request to the server to change the owner of a good.
	 * Fails if user doesn't own it.
	 */
	public boolean transferGood() {

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
