package hds_user;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class UserListener implements Runnable {

	private int portNumber;
	private Thread t;
	private String threadName;
	protected Vector<UserConnection> clientConnections;
	private User user;

	public UserListener(int port, String name, User user) {
		this.portNumber = port;
		this.threadName = name;
		this.clientConnections = new Vector<>();
		this.user = user;
	}

	@Override
	public void run() {

		try (ServerSocket serverSocket = new ServerSocket(portNumber);) {
			while (true) {
				Socket clientSocket = serverSocket.accept();
				UserConnection cn = new UserConnection(this, clientSocket, "Client: " + clientSocket.getInetAddress(),
						user);
				cn.start();
				this.clientConnections.add(cn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println("Thread " + threadName + " exiting.");
	}

	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
