package notary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import hds_security.Message;
import hds_security.Request;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.ReplayAttackException;
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class Server extends Thread {

	private Database db;
	private ServerSocket serverSocket;

	private BlockingQueue<Request> requests;

	// Does not allow more than 'max_queue' requests on the queue (for resources
	// concern)
	private int max_queue = 1024;
	private HashMap<Long, Long> noncemap = new HashMap<>();
	Random rand = new Random();

	public Server(int port, Database db) throws IOException {
		this.db = db;
		serverSocket = new ServerSocket(port);

		/**
		 * 1st parameter: capacity - the capacity of this queue 2nd parameter: fair - if
		 * true then queue accesses for threads blocked on insertion or removal, are
		 * processed in FIFO order; if false the access order is unspecified.
		 */
		requests = new ArrayBlockingQueue<Request>(max_queue, true);
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
				}
			}
		}
	}

	public void runProducer() {
		try {
			System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");

			Request request = null;
			try {
				request = new Request(serverSocket.accept());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException
					| InvalidSignatureException e) {
				e.printStackTrace();
			}

			// 'put' blocks, 'add' throws exception
			assert request != null;
			requests.put(request);

			System.out.println("Request created from: " + request.getAddr());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void runConsumer() throws PteidException, IllegalAccessException, PKCS11Exception, NoSuchMethodException,
			InvocationTargetException, ClassNotFoundException {

		try {
			// 'take' blocks, 'remove' throws exception
			Request request = requests.take();

			Date date = new Date();
			long now = date.getTime();
			System.out.println(request.now);
			System.out.println(request.now + 10000);
			System.out.println(now);
			if (request.now + 10000 <= now) {
				System.out.println("Invalid packet, delay too long");
				throw new IllegalAccessException();
			} else {
				Long nonce = noncemap.get(request.now);
				if (!(nonce == null)) {
					if (nonce == request.nonce) {
						throw new ReplayAttackException();
					}
				} else {
					noncemap.put(request.now, request.nonce);
				}
			}

			if (request.operation == 'S') {
				int reply = db.checkIntentionToSell(request.origin, request.gid);
				Message message = new Message(-1, -1, 'R', reply);
				try {
					request.writeServer(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			else if (request.operation == 'G') {
				String query_result = db.getStateOfGood(request.gid);
				String[] splitted_query = query_result.split(" ");

				Message message = new Message(Integer.valueOf(splitted_query[0]), -1, 'R',
						Integer.valueOf(splitted_query[1]));
				try {
					request.writeServer(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			else if (request.operation == 'T') {
				int reply = db.transferGood(request.gid, request.origin, request.destin);

				Message message = new Message(-1, -1, 'R', reply);
				try {
					request.writeServer(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (InterruptedException | ReplayAttackException e) {
			e.printStackTrace();
		}
	}
}