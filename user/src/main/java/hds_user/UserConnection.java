package hds_user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Random;

import notary.Request;
import hds_security.Message;
import notary.exceptions.InvalidSignatureException;

public class UserConnection implements Runnable {

	private Thread t;
	private String threadName;
	private Socket clientSocket;
	private UserListener userListener;
	private Request request;
	private Random rand = new Random();

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
		replyMessage = null;
		try {
			replyMessage = conn.transferGood(request.gid, request.destin, request.origin);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchAlgorithmException | SignatureException | InvalidSignatureException | InvalidKeyException e) {
			e.printStackTrace();
		}
		assert replyMessage != null;
		System.out.println(replyMessage.getContent());
		try {
			request.write(replyMessage);
		} catch(IOException e){
			e.printStackTrace();
		}


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
