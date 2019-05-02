package hds_user;

import java.io.IOException;
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
	private Main main;

	public UserConnection(UserListener ul, Socket s, String name, Request req, Main main) {
		userListener = ul;
		threadName = name;
		clientSocket = s;
		request=req;
		this.main = main;
	}

	@Override
	public void run() {
		NotaryConnection conn = main.getNotaryConnection();

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
