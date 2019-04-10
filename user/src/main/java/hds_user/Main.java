package hds_user;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.Scanner;

import hds_security.HashMessage;
import hds_security.Message;
import hds_security.SignMessage;
import hds_user.exceptions.*;

import javax.xml.crypto.Data;

public class Main {

	// Connection stuff
	private static String serverName = "localhost";
	private static int notaryPort = 6066;
	private static int userListenerPort;
	private static NotaryConnection conn;
	private static User user;

	public static void main(String[] args) {

		println("Hello!");

		int uid = 0;
		while (true) {
			try {
				uid = Integer.parseInt(Main.readString("Please enter your ID: ", false));
				break;
			} catch (Exception e) {
				Main.println("ID must be a number.");
			}
		}

		// Listen to user requests
		byte[] port_bytes = null;
		int port=0;

		try {
			FileInputStream fis = new FileInputStream("./src/main/resources/" + uid + "_port.txt");
			Scanner scanner = new Scanner(fis);
			port =  scanner.nextInt();
			fis.close();
		}catch( IOException e){
			Main.println("port file not found");
		}

		//ByteBuffer bb = ByteBuffer.wrap(port_bytes);
		//int port = bb.getInt();
		userListenerPort= port;
		System.out.println(userListenerPort);
		UserListener userListener = new UserListener(userListenerPort, "userListenerThread");
		userListener.start();

		String password = readPassword();
		user = new User(uid,password);
		conn = new NotaryConnection(serverName, notaryPort, user);

		/*try {
			//user.setUserList(conn.getListOfUsers());
			//user.setGoodList(conn.getListOfGoods());
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		} catch (InexistentGoodsException e) {
			e.toString();
			System.exit(0);
		}

		user.printAllUsers();
		user.printAllGoods();
*/
		while (true) {

			int option = 0;

			Main.println("What would you like to do?");
			Main.println("1. Get State of Good");
			Main.println("2. Intention to sell");
			Main.println("3. Intention to buy");
			Main.println("4. Transfer Good");
			Main.println("0. Exit");

			String input = Main.readString();

			try {
				option = Integer.parseInt(input);
			} catch (Exception e) {
				Main.println("Wrong input!");
				continue;
			}

			switch (option) {
				case 1:
					menuPrintGood(uid);
					break;
				case 2:
					intentionToSell(uid);
					break;
				case 3:
					break;
				case 4:
					transferGood(uid);
					break;
				case 0:
					System.exit(0);
				default:
					Main.println(("Wrong number!"));
			}

		}

	}

	public static void intentionToSell(int uid) {

		int gid;

		while (true) {
			try {
				gid = Integer.parseInt(Main.readString("Good ID: ", false));
				break;
			} catch (NumberFormatException e) {
				Main.println("Not a valid ID.");
			}
		}

		int b;

		try {
			b = conn.intentionToSell(gid, uid);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println(b);

	}

	public static void menuPrintGood(int uid) {

		int gid;

		while (true) {
			try {
				gid = Integer.parseInt(Main.readString("Good ID: ", false));
				break;
			} catch (NumberFormatException e) {
				Main.println("Not a valid ID.");
			}
		}

		Good g;

		try {
			g = conn.getStateOfGood(gid, uid);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		println("Good with ID=" + gid + " belongs to user with ID=" + g.getOwner() + " and is "
				+ (g.getForSale() ? "" : "not ") + "for sale.");
	}

	public static void transferGood(int ownerID) {
		int gid, buyerID;
		while (true) {
			try {
				gid = Integer.parseInt(Main.readString("Good ID: ", false));
				buyerID = Integer.parseInt(Main.readString("Buyer ID: ", false));
				break;
			} catch (NumberFormatException e) {
				Main.println("Not a valid ID.");
			}
		}
		int reply;

		try {
			reply = conn.transferGood(gid, ownerID, buyerID);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println(reply);

	}


	public static void buyGood(int userID) {
		int gid, ownerID;
		while (true) {
			try {
				gid = Integer.parseInt(Main.readString("Good ID: ", false));
				ownerID = Integer.parseInt(Main.readString("Owner ID: ", false));
				break;
			} catch (NumberFormatException e) {
				Main.println("Not a valid ID.");
			}
		}
		Message replyMessage;

		try {

			FileInputStream fis = new FileInputStream("./src/main/resources/" + ownerID + "_port.txt");
			byte[] port_bytes= fis.readAllBytes();
			fis.close();
			ByteBuffer bb = ByteBuffer.wrap(port_bytes);
			int port = bb.getInt();


			Socket owner = new Socket("localhost", port);
			DataOutputStream out = new DataOutputStream(owner.getOutputStream());
			DataInputStream in = new DataInputStream(owner.getInputStream());

			Date date = new Date();
			long now = date.getTime();
			Message message = new Message(ownerID,userID,'B', now, gid);

			byte[] finalmsg = conn.cypher(message);
			out.writeInt(finalmsg.length);
			out.write(finalmsg, 0, finalmsg.length);


			int msgLen = in.readInt();
			byte[] msg = new byte[msgLen];
			in.readFully(msg);

			replyMessage = Message.fromBytes(msg);

			owner.close();


		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println(replyMessage.getContent());

	}


	public static void print(String str) {
		System.out.print(str);
	}

	public static void println(String str) {
		System.out.println(str);
	}

	public static String readString(String prompt, boolean newline) {
		if (newline) {
			Main.println(prompt);
		} else {
			Main.print(prompt);
		}
		return readString();
	}

	public static String readString() {
		// Used BufferedReader instead of System console because the later doesn't work
		// with
		// Eclipse IDE.
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		try {
			input = bufferedReader.readLine();
		} catch (IOException ioe) {
			println("Problems with reading user input.");
		}
		return input;
	}
	public static String readPassword(){
		Console console =  System.console();
		char [] input = console.readPassword("Please enter your secret password:  ");
		return String.valueOf(input);
	}


	public static int convertByteToInt(byte[] b)
	{
		int value= 0;
		for(int i=0; i<b.length; i++)
			value = (value << 8) | b[i];
		return value;
	}
}
