package notary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import hds_security.LoadKeys;
import hds_security.Message;
import hds_security.SecureSession;
import hds_security.Utils;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;
import pteidlib.PteidException;
import sun.reflect.generics.tree.Tree;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class Server extends Thread {

	private Database db;
	private ServerSocket serverSocket;
	private int cc;
	private String port;
	private SecureSession secureSession;
	private BlockingQueue<Request> requests;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String password;

	// Does not allow more than 'max_queue' requests on the queue (for resources
	// concern)
	private final int max_queue = 1024;

	public Server(String port, Database db, int cc, String password) throws Exception {
		this.db = db;
		this.cc = cc;
		this.port = port;
		this.password = password;
		serverSocket = new ServerSocket(Integer.valueOf(port));
		loadPrivKey();
		loadPubKey();


		/**
		 * 1st parameter: capacity - the capacity of this queue 2nd parameter: fair - if
		 * true then queue accesses for threads blocked on insertion or removal, are
		 * processed in FIFO order; if false the access order is unspecified.
		 */
		requests = new ArrayBlockingQueue<Request>(max_queue, true);
		secureSession = new SecureSession();
	}

	private void loadPubKey()
			throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, NullPublicKeyException {
		this.publicKey = LoadKeys.loadPublicKey("./src/main/resources/" + port + "_public_key.txt", "EC");
	}

	private void loadPrivKey()
			throws Exception {
		this.privateKey = LoadKeys.loadPrivateKey("./src/main/resources/" + port + "_private_key.txt",
				"./src/main/resources/" + port + "_salt.txt", "./src/main/resources/" + port + "_hash.txt",
				this.password);
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}

	public void write(Message message, Request request) throws Exception{
		if(cc==1){Utils.writeWithCC(message, request.getDataOutputStream());}
		else{ Utils.write(message, request.getDataOutputStream(), this.getPrivateKey());}
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

			// XXX
			//MessageLogger.log(Server.class.getName(), Level.INFO,msg.toBytes());

			if (msg.getOperation() == 'S') {
				int reply;
				synchronized(db) {
					reply = db.intentionToSell(msg.getOrigin(), msg.getGoodID(), msg.getNow());
				}
				Message message = new Message('R', reply);
				try {
					write(message, request);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (msg.getOperation() == 'G') {
				int reply;
				synchronized(db) {
					reply = db.getStateOfGood(msg.getGoodID(), msg.getGoodID(), msg.getNow());
				}
				Message message = new Message( 'R', reply);
				try {
					write(message, request);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (msg.getOperation() == 'T') {
				boolean reply;
				synchronized(db) {
					reply = db.transferGood(msg.getGoodID(), msg.getOrigin(), msg.getDestination(), msg.getNow());
				}
				// returns good id on success
				Message message = new Message('R', (reply ? msg.getGoodID() : -1));
				try {
					write(message, request);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}