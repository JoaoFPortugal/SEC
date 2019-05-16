package hds_user;

import hds_security.LoadKeys;
import hds_security.Message;
import hds_security.SecureSession;
import hds_security.Utils;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullDestination;
import hds_security.exceptions.NullPrivateKeyException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class User {

	private final int uid;
	private final String password;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	String strongpassword;
	
	private NotaryConnection conn;
	private SecureSession userSS;
	private UserListener userListener;

	public User(int id, String password, String serverName, int[] notaryPorts, int userListenerPort, int cc) throws Exception {
		this.uid = id;
		this.password = password;
		loadPubKey();
		loadPrivKey();


		this.userSS = new SecureSession();
		this.conn = new NotaryConnection(serverName, notaryPorts, this, cc);

		this.userListener = new UserListener(userListenerPort, "userListenerThread", this);
		userListener.start();
		Utils.println("User listening on port: " + userListenerPort);
	}

	private void loadPubKey()
			throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, NullPublicKeyException {
		this.publicKey = LoadKeys.loadPublicKey("./src/main/resources/" + uid + "_public_key.txt", "EC");
	}

	private void loadPrivKey()
			throws Exception {
		this.privateKey = LoadKeys.loadPrivateKey("./src/main/resources/" + uid + "_private_key.txt",
				"./src/main/resources/" + uid + "_salt.txt", "./src/main/resources/" + uid + "_hash.txt",
				this.password);
	}
	
	public int getID() {
		return this.uid;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public PrivateKey getPrivateKey() {
		return this.privateKey;
	}
	
	public void getStateOfGood(int uid) {

		int gid = Utils.readInt("Good ID: ");

		try {
			Good g = conn.getStateOfGood(gid, uid);
			if (g == null) {
				Utils.println("Good with ID=" + gid + " does not exist.");
			} else {
				Utils.println("Good with ID=" + gid + " belongs to user with ID=" + g.getOwner() + " and is "
						+ (g.getForSale() ? "" : "not ") + "for sale.");
			}
		} catch (IOException | NoSuchAlgorithmException | SignatureException | InvalidKeyException
				| InvalidSignatureException | NullPrivateKeyException | NullDestination | NullPublicKeyException
				| InvalidKeySpecException | ReplayAttackException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void intentionToSell(int uid) {

		int gid = Utils.readInt("Good ID: ");

		try {
			int for_sale = conn.intentionToSell(gid, uid);
			System.out.println("Good with ID=" + gid + " is " + (for_sale == 1 ? "" : "not ") + "for sale.");
		} catch (IOException | NoSuchAlgorithmException | InvalidSignatureException | InvalidKeyException
				| SignatureException | NullPrivateKeyException | NullDestination | NullPublicKeyException
				| InvalidKeySpecException | ReplayAttackException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Asks another user to buy a good.
	 */
	public void buyGood(int userID) {
		int gid = Utils.readInt("Good ID: ");
		int ownerID = Utils.readInt("Owner ID: ");

		int port = Utils.readIntFromFile("./src/main/resources/" + ownerID + "_port.txt");

		try (Socket owner = new Socket("localhost", port);
				DataOutputStream out = new DataOutputStream(owner.getOutputStream());
				DataInputStream in = new DataInputStream(owner.getInputStream());) {
			Utils.write(new Message(userID, ownerID, 'B', gid, -1), out, this.getPrivateKey());

			Message replyMessage = userSS.readFromUser(in);
			
			if (replyMessage.getGoodID() < 0) {
				System.out.println("Failed to transfer good with ID=" + gid + ". Do you own it?");
				return;
			}
			
			System.out.println("Good with ID=" + gid + " successfully transfered to me.");
		} catch (IOException e) {
			e.printStackTrace();
			Utils.println("Failed to contact user with ID=" + userID + ".");
			return;
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
			Utils.println("Problem with user's key.");
			return;
		} catch (InvalidKeySpecException | IllegalAccessException | InvalidSignatureException | ReplayAttackException
				| NullPublicKeyException e) {
			e.printStackTrace();
			Utils.println("Problem with reply message.");
			return;
		}

	}
	
	public NotaryConnection getNotaryConnection() {
		return conn;
	}
	
	public SecureSession getUserSecureSession() {
		return userSS;
	}

}