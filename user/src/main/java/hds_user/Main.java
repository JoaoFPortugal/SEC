package hds_user;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import hds_security.Message;
import hds_security.SecureSession;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullDestination;
import hds_security.exceptions.NullPrivateKeyException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

public class Main {

	// Connection stuff
	private final String serverName = "localhost";
	private final int notaryPort = 6066;
	private int userListenerPort;
	private NotaryConnection conn;
	private User user;
	private SecureSession userSS;

	public static void main(String[] args) {

		Main main = new Main();

		Utility.println("Hello!");

		int uid = Utility.readInt("Please enter your ID: ");

		// Listen to user requests
		main.userListenerPort = Utility.readIntFromFile("./src/main/resources/" + uid + "_port.txt");
		UserListener userListener = new UserListener(main.userListenerPort, "userListenerThread", main);
		userListener.start();
		Utility.println("User listening on port: " + main.userListenerPort);
		main.userSS = new SecureSession();

		String password = Utility.readPassword("Please enter your secret password: ");
		try {
			main.user = new User(uid, password);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException | NullPublicKeyException e) {
			e.printStackTrace();
			Utility.println("Failed to load user's keys.");
			System.exit(0);
		}
		main.conn = new NotaryConnection(main.serverName, main.notaryPort, main.user);

		while (true) {

			String menuStr = "What would you like to do?\n" + "1. Get State of Good\n" + "2. Intention to sell\n"
					+ "3. Intention to buy\n" + "0. Exit\n";

			int option = Utility.readInt(menuStr);

			switch (option) {
			case 1:
				main.getStateOfGood(uid);
				break;
			case 2:
				main.intentionToSell(uid);
				break;
			case 3:
				main.buyGood(uid);
				break;
			case 0:
				System.exit(0);
			default:
				Utility.println(("Wrong number!"));
			}

		}

	}

	public void getStateOfGood(int uid) {

		int gid = Utility.readInt("Good ID: ");

		try {
			Good g = conn.getStateOfGood(gid, uid);
			if (g == null) {
				Utility.println("Good with ID=" + gid + " does not exist.");
			} else {
				Utility.println("Good with ID=" + gid + " belongs to user with ID=" + g.getOwner() + " and is "
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

		int gid = Utility.readInt("Good ID: ");

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
	private void buyGood(int userID) {
		int gid = Utility.readInt("Good ID: ");
		int ownerID = Utility.readInt("Owner ID: ");

		int port = Utility.readIntFromFile("./src/main/resources/" + ownerID + "_port.txt");

		try (Socket owner = new Socket("localhost", port);
				DataOutputStream out = new DataOutputStream(owner.getOutputStream());
				DataInputStream in = new DataInputStream(owner.getInputStream());) {
			SecureSession.write(new Message(userID, ownerID, 'B', gid), out, user.getPrivateKey());

			Message replyMessage = userSS.readFromUser(in);
			
			if (replyMessage.getGoodID() < 0) {
				System.out.println("Failed to transfer good with ID=" + gid + ". Do you own it?");
				return;
			}
			
			System.out.println("Good with ID=" + gid + " successfully transfered to me.");
		} catch (IOException e) {
			e.printStackTrace();
			Utility.println("Failed to contact user with ID=" + userID + ".");
			return;
		} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
			Utility.println("Problem with user's key.");
			return;
		} catch (InvalidKeySpecException | IllegalAccessException | InvalidSignatureException | ReplayAttackException
				| NullPublicKeyException e) {
			e.printStackTrace();
			Utility.println("Problem with reply message.");
			return;
		}

	}

	public NotaryConnection getNotaryConnection() {
		return conn;
	}
	
	public SecureSession getUserSecureSession() {
		return userSS;
	}
	
	public User getUser() {
		return user;
	}
}
