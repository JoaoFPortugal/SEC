package notary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
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
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class Server extends Thread {

	private Database db;
	private ServerSocket serverSocket;
	private int cc;
	private int port;
	private SecureSession secureSession;
	private BlockingQueue<Request> requests;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String password;
	private HashMap<Integer, Integer> tags;
	//private HashMap<Integer, NoobChain> userChains;

	// Does not allow more than 'max_queue' requests on the queue (for resources
	// concern)
	private final int max_queue = 1024;

	public Server(int port, Database db, int cc, String password) throws Exception {
		this.db = db;
		this.cc = cc;
		this.port = port;
		this.password = password;
		serverSocket = new ServerSocket(port);
		loadPrivKey();
		loadPubKey();
		this.tags = new HashMap<>();
		getTagsFromBD();

		/**
		 * 1st parameter: capacity - the capacity of this queue 2nd parameter: fair - if
		 * true then queue accesses for threads blocked on insertion or removal, are
		 * processed in FIFO order; if false the access order is unspecified.
		 */
		requests = new ArrayBlockingQueue<Request>(max_queue, true);
		secureSession = new SecureSession();
		
		/**
		 * With each user a blockchain will be associated, to which a new block will be added
		 * after each request 
		 * Unfinished proof of work
		userChains = new HashMap<Integer, NoobChain>();
		*/
	}

	private void getTagsFromBD(){
		List<Integer> tagsFromDB = db.getTags();
		System.out.println(tagsFromDB);
		tags.put(1, tagsFromDB.get(0));
		tags.put(2, tagsFromDB.get(1));
		tags.put(3, tagsFromDB.get(2));
		tags.put(4, tagsFromDB.get(3));
		tags.put(5, tagsFromDB.get(4));
	}

	private void loadPubKey()
			throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, NullPublicKeyException {
		this.publicKey = LoadKeys.loadPublicKey("./src/main/resources/" + port + "_public_key.txt", "RSA");
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
		else{
			System.out.println("writing to server (origin, forsale, tag, operation):");
			System.out.println(message.getOrigin());
			System.out.println(message.getFor_sale());
			System.out.println(message.getTag());
			System.out.println(message.getOperation());
			Utils.write(message, request.getDataOutputStream(), this.getPrivateKey());
		}
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
			
			/* Unfinished proof of work
			int userID = request.getMessage().getOrigin();
			if (!userChains.containsKey(userID)) {
			    userChains.put(userID, new NoobChain());
			}
			*/
			
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

			int tag = tags.get(msg.getOrigin());
			System.out.println(tag);


			if (msg.getOperation() == 'G') {
				HashMap<String, Integer> res;
				synchronized(db) {
					res = db.getStateOfGood(msg.getOrigin(), msg.getGoodID(), msg.getNow());
				}
				Message message;
				if (res == null) {
					message = new Message(-1, 'G', -1 ,-1, tag);
				} else {
					message = new Message(res.get("owner_id"), 'G', msg.getGoodID(), res.get("for_sale"), tag);
				}
				try {
					write(message, request);
				} catch (Exception e) {
					e.printStackTrace();
				}


			} else if (msg.getOperation() == 'S') {
				if (tag < msg.getTag()) {
					tags.replace(msg.getOrigin(), msg.getTag());
					tag = tags.get(msg.getOrigin());
				}
				int reply;
				synchronized (db) {
					reply = db.intentionToSell(msg.getOrigin(), msg.getGoodID(), msg.getNow());
				}
				Message message = new Message('S', reply, tag);
				try {
					write(message, request);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}


			else if (msg.getOperation() == 'T') {
				if (tag < msg.getTag()) {
					tags.replace(msg.getOrigin(), msg.getTag());
					tag = tags.get(msg.getOrigin());
				}
				boolean reply;
				synchronized(db) {
					reply = db.transferGood(msg.getGoodID(), msg.getOrigin(), msg.getDestination(), msg.getNow());
				}
				// returns good id on success
				Message message = new Message('T', (reply ? msg.getGoodID() : -1), tag);
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