package notary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import hds_security.Message;
import hds_security.SecureSession;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class Server extends Thread {

	private Database db;
	private ServerSocket serverSocket;

	private SecureSession secureSession;
	private BlockingQueue<Request> requests;

	// Does not allow more than 'max_queue' requests on the queue (for resources
	// concern)
	private final int max_queue = 1024;

	public Server(int port, Database db) throws IOException {
		this.db = db;
		serverSocket = new ServerSocket(port);

		/**
		 * 1st parameter: capacity - the capacity of this queue 2nd parameter: fair - if
		 * true then queue accesses for threads blocked on insertion or removal, are
		 * processed in FIFO order; if false the access order is unspecified.
		 */
		requests = new ArrayBlockingQueue<Request>(max_queue, true);
		secureSession = new SecureSession();
	}

	@Override
	public void run() {
		while (true) {
			if (Thread.currentThread().getName().equals("producer")) {
				runProducer();
			} else {
				try {
					runConsumer();
				} catch (PteidException | IllegalAccessException | PKCS11Exception | NoSuchMethodException
						| InvocationTargetException | ClassNotFoundException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
	}

	public void runProducer() {
		try {
			System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");

			// Read is done in Request constructor
			Request request = new Request(serverSocket.accept(), secureSession);

			// 'put' blocks, 'add' throws exception
			requests.put(request);

			System.out.println("Request created from: " + request.getAddr());

		} catch (IOException | InterruptedException | NoSuchAlgorithmException | InvalidKeySpecException
				| SignatureException | InvalidKeyException | InvalidSignatureException | IllegalAccessException
				| ReplayAttackException | NullPublicKeyException e) {
			e.printStackTrace();
		}
	}

	public void runConsumer() throws PteidException, IllegalAccessException, PKCS11Exception, NoSuchMethodException,
			InvocationTargetException, ClassNotFoundException {

		try {
			// 'take' blocks, 'remove' throws exception
			Request request = requests.take();
			Message msg = request.getMessage();

			if (msg.getOperation() == 'S') {
				int reply;
				synchronized(db) {
					reply = db.intentionToSell(msg.getOrigin(), msg.getGoodID());
				}
				Message message = new Message('R', reply);
				try {
					SecureSession.writeWithCC(message, request.getDataOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (msg.getOperation() == 'G') {
				HashMap<String, Integer> res;
				synchronized(db) {
					res = db.getStateOfGood(msg.getGoodID());
				}
				Message message;
				if (res == null) {
					message = new Message(-1, 'R', -1);
				} else {
					message = new Message(res.get("owner_id"), 'R', res.get("for_sale"));
				}
				try {
					SecureSession.writeWithCC(message, request.getDataOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (msg.getOperation() == 'T') {
				boolean reply;
				synchronized(db) {
					reply = db.transferGood(msg.getGoodID(), msg.getOrigin(), msg.getDestination());
				}
				// returns good id on success
				Message message = new Message('R', (reply ? msg.getGoodID() : -1));
				try {
					SecureSession.writeWithCC(message, request.getDataOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}