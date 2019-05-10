package hds_user;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import hds_security.Message;
import hds_security.SecureSession;
import hds_security.Utils;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullDestination;
import hds_security.exceptions.NullPrivateKeyException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

public class UserConnection implements Runnable {

	private Thread t;
	private String threadName;
	private DataOutputStream out;
	private DataInputStream in;
	private UserListener userListener;
	private NotaryConnection conn;
	private SecureSession userSS;
	private User user;

	public UserConnection(UserListener ul, Socket s, String name, User u) throws IOException {
		this.userListener = ul;
		this.threadName = name;
		this.out = new DataOutputStream(s.getOutputStream());
		this.in = new DataInputStream(s.getInputStream());
		this.conn = u.getNotaryConnection();
		this.userSS = u.getUserSecureSession();
		this.user = u;
	}

	@Override
	public void run() {
		try {
			Message request;
			request = userSS.readFromUser(in);
			
			if(request.getOperation() != 'B') {
				System.out.println("Invalid request: " + request.getOperation());
			}
			
			Message replyMessage = conn.transferGood(request.getGoodID(), request.getDestination(), request.getOrigin());
			
			Message toUser = new Message(this.user.getID(), request.getOrigin(), replyMessage.getOperation(), replyMessage.getGoodID());
			
			Utils.write(toUser, out, user.getPrivateKey());
		} catch (IOException | IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException | SignatureException | InvalidSignatureException | InvalidKeyException e) {
			e.printStackTrace();
		} catch (NullPrivateKeyException | NullPublicKeyException | NullDestination | InvalidKeySpecException | ReplayAttackException e) {
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
