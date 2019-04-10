package hds_user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import notary.Request;
import hds_security.Message;

public class UserConnection implements Runnable {

	private Thread t;
	private String threadName;
	private Socket clientSocket;
	private UserListener userListener;
	private Request request;

	UserConnection(UserListener ul, Socket s, String name, Request req) {
		userListener = ul;
		threadName = name;
		clientSocket = s;
		request=req;
	}

	@Override
	public void run() {
		NotaryConnection conn = Main.getNotaryConnection();

		Message replyMessage;

		try {
			replyMessage = conn.transferGood(request.gid, request.destin, request.origin);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println(replyMessage.getContent());
		try {
			request.write(replyMessage);
		} catch(IOException e){
			e.printStackTrace();
		}


/*		try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(
					 new InputStreamReader(clientSocket.getInputStream()));
		) {

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				if (inputLine.startsWith("buyGood")) {

					String[] tokens = inputLine.split(" ");

					if (tokens.length != 3) throw new IOException();

					int gid, owner;
					try {
						// test if good id is an integer
						gid = Integer.parseInt(tokens[1]);
						// test if owner is an integer
						owner = Integer.parseInt(tokens[2]);
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
						throw new IOException();
					}

					//boolean success = Main.callTransfer(gid, owner);
					boolean success= true;

					out.println(success ? "true" : "false");

					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
*/
		System.out.println("Thread " + threadName + " exiting.");
		userListener.clientConnections.remove(this);
	}

	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
