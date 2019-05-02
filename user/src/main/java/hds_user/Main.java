package hds_user;

import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import hds_security.Message;
import notary.exceptions.InvalidSignatureException;

import javax.swing.JOptionPane;

public class Main {

	// Connection stuff
	private String serverName = "localhost";
	private int notaryPort = 6066;
	private int userListenerPort;
	private NotaryConnection conn;
	private User user;
	Random rand = new Random();

	public static void main(String[] args) {

		Main main = new Main();

		println("Hello!");

		int uid = readInt("Please enter your ID: ");

		// Listen to user requests
		main.userListenerPort = readIntFromFile("./src/main/resources/" + uid + "_port.txt");

		println("User listening on port: " + main.userListenerPort);
		UserListener userListener = new UserListener(main.userListenerPort, "userListenerThread", main);
		userListener.start();

		String password = readPassword("Please enter your secret password: ");
		main.user = new User(uid, password);
		main.conn = new NotaryConnection(main.serverName, main.notaryPort, main.user);

		while (true) {

			String menuStr = "What would you like to do?\n" + "1. Get State of Good\n" + "2. Intention to sell\n"
					+ "3. Intention to buy\n" + "0. Exit\n";

			int option = readInt(menuStr);

			switch (option) {
			case 1:
				main.menuPrintGood(uid, main);
				break;
			case 2:
				main.intentionToSell(uid, main);
				break;
			case 3:
				main.buyGood(uid, main);
				break;
			case 0:
				System.exit(0);
			default:
				println(("Wrong number!"));
			}

		}

	}

	public void intentionToSell(int uid, Main main) {

		int gid = readInt("Good ID: ");

		int b = 0;

		try {
			b = conn.intentionToSell(gid, uid);
		} catch (IOException | NoSuchAlgorithmException | InvalidSignatureException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println(b);

	}

	public void menuPrintGood(int uid, Main main) {

		int gid = readInt("Good ID: ");

		Good g = null;

		try {
			g = conn.getStateOfGood(gid, uid);
		} catch (IOException | NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidSignatureException e) {
			e.printStackTrace();
			System.exit(0);
		}

		assert g != null;

		println("Good with ID=" + gid + " belongs to user with ID=" + g.getOwner() + " and is "
				+ (g.getForSale() ? "" : "not ") + "for sale.");
	}

	private void buyGood(int userID, Main main) {
		int gid = readInt("Good ID: ");
		int ownerID = readInt("Owner ID: ");

		Message replyMessage;

		int port = readIntFromFile("./src/main/resources/" + ownerID + "_port.txt");

		try (Socket owner = new Socket("localhost", port);
				DataOutputStream out = new DataOutputStream(owner.getOutputStream());
				DataInputStream in = new DataInputStream(owner.getInputStream());) {

			Date date = new Date();
			long now = date.getTime();
			long nonce = rand.nextLong();
			Message message = new Message(userID, ownerID, 'B', now, gid, nonce);

			byte[] finalmsg = conn.sign(message);
			out.writeInt(finalmsg.length);
			out.write(finalmsg, 0, finalmsg.length);

			int msgLen = in.readInt();
			byte[] msg = new byte[msgLen];
			in.readFully(msg);
			replyMessage = Message.fromBytes(msg);
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println(replyMessage.getContent());
	}

	static public void println(String str) {
		System.out.println(str);
	}

	/**
	 * This method contemplates if the program is being run in Eclipse or not, as
	 * Eclipse does not support System.console. Same for Gradle.
	 */
	static public String readString(String prompt) {

		String input = "";
		Console c = System.console();

		if (c == null) {
			input = JOptionPane.showInputDialog(prompt);
		} else {
			println(prompt);
			input = c.readLine();

		}

		return input;
	}

	/**
	 * This method contemplates if the program is being run in Eclipse or not, as
	 * Eclipse does not support System.console. Same for Gradle.
	 */
	static public String readPassword(String prompt) {

		String input = "";
		Console c = System.console();

		if (c == null) {
			input = JOptionPane.showInputDialog(prompt);
		} else {
			char[] in = System.console().readPassword(prompt);
			input = String.valueOf(in);
		}
		return input;
	}

	static public int readInt(String prompt) {
		int i;
		while (true) {
			try {
				i = Integer.parseInt(readString(prompt));
				break;
			} catch (NumberFormatException e) {
				println("Not a valid number.");
			}
		}
		return i;
	}

	static public int readIntFromFile(String filename) {
		int i = -1;
		try (FileInputStream fis = new FileInputStream(filename); Scanner scanner = new Scanner(fis);) {
			i = scanner.nextInt();
		} catch (IOException e) {
			e.printStackTrace();
			println("Could not read from file '" + filename + "'.");
			System.exit(0);
		}
		return i;
	}

	public NotaryConnection getNotaryConnection() {
		return conn;
	}
}
