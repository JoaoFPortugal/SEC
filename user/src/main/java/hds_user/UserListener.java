package hds_user;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class UserListener implements Runnable {

	private int portNumber;
	private Thread t;
	private String threadName;
	protected Vector<UserConnection> clientConnections;

	UserListener(int port, String name) {
		this.portNumber = port;
		this.threadName = name;
		this.clientConnections = new Vector<UserConnection>();
	}

	@Override
	public void run() {

		try (
				ServerSocket serverSocket = new ServerSocket(portNumber);
		) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				UserConnection cn = new UserConnection(this, clientSocket,
						"Client: " + clientSocket.getInetAddress());
				cn.start();
				this.clientConnections.add(cn);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("Thread " +  threadName + " exiting.");
	}

	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
