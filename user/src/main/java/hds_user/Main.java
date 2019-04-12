package hds_user;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import hds_security.HashMessage;
import hds_security.Message;
import hds_security.SignMessage;
import hds_user.exceptions.*;
import notary.exceptions.InvalidSignatureException;

import javax.xml.crypto.Data;

public class Main {

	// Connection stuff
	private String serverName = "localhost";
	private int notaryPort = 6066;
	private int userListenerPort;
	private NotaryConnection conn;
	private  User user;
	Random rand = new Random();

	public static void main(String[] args) {

		Main main = new Main();
		main.println("Hello!");
		int uid = 0;
		while (true) {
			try {
				uid = Integer.parseInt(main.readString("Please enter your ID: ", main));
				break;
			} catch (Exception e) {
				main.println("ID must be a number.");
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
			main.println("port file not found");
		}

		//ByteBuffer bb = ByteBuffer.wrap(port_bytes);
		//int port = bb.getInt();
		main.userListenerPort= port;
		System.out.println(main.userListenerPort);
		UserListener userListener = new UserListener(main.userListenerPort, "userListenerThread",main);
		userListener.start();

		String password = main.readPassword();
		main.user = new User(uid,password);
		main.conn = new NotaryConnection(main.serverName, main.notaryPort, main.user);


		while (true) {

			int option = 0;

			main.println("What would you like to do?");
			main.println("1. Get State of Good");
			main.println("2. Intention to sell");
			main.println("3. Intention to buy");
			main.println("0. Exit");

			String input = main.readString("",main);

			try {
				option = Integer.parseInt(input);
			} catch (Exception e) {
				main.println("Wrong input!");
				continue;
			}

			switch (option) {
				case 1:
					main.menuPrintGood(uid,main);
					break;
				case 2:
					main.intentionToSell(uid,main);
					break;
				case 3:
				    main.buyGood(uid,main);
					break;
				case 0:
					System.exit(0);
				default:
					main.println(("Wrong number!"));
			}

		}

	}

	public void intentionToSell(int uid,Main main) {

		int gid;

		while (true) {
			try {
				gid = Integer.parseInt(main.readString("Good ID: ", main));
				break;
			} catch (NumberFormatException e) {
				main.println("Not a valid ID.");
			}
		}

		int b = 0;

		try {
			b = conn.intentionToSell(gid, uid);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchAlgorithmException | InvalidSignatureException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
		}
		System.out.println(b);

	}

	public void menuPrintGood(int uid,Main main) {

		int gid;

		while (true) {
			try {
				gid = Integer.parseInt(main.readString("Good ID: ", main));
				break;
			} catch (NumberFormatException e) {
				main.println("Not a valid ID.");
			}
		}

		Good g = null;

		try {
			g = conn.getStateOfGood(gid, uid);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidSignatureException e) {
			e.printStackTrace();
		}

		assert g!=null;

		println("Good with ID=" + gid + " belongs to user with ID=" + g.getOwner() + " and is "
				+ (g.getForSale() ? "" : "not ") + "for sale.");
	}


	private void buyGood(int userID,Main main) {
		int gid, ownerID;
		while (true) {
			try {
				gid = Integer.parseInt(main.readString("Good ID: ",main));
				ownerID = Integer.parseInt(main.readString("Owner ID: ",main));
				break;
			} catch (NumberFormatException e) {
				main.println("Not a valid ID.");
			}
		}
		Message replyMessage;

		try {

			FileInputStream fis = new FileInputStream("./src/main/resources/" + ownerID + "_port.txt");
			Scanner scanner = new Scanner(fis);
			int port =  scanner.nextInt();
			fis.close();

			Socket owner = new Socket("localhost", port);
			DataOutputStream out = new DataOutputStream(owner.getOutputStream());
			DataInputStream in = new DataInputStream(owner.getInputStream());

			Date date = new Date();
			long now = date.getTime();
			long nonce = rand.nextLong();
			Message message = new Message(userID,ownerID,'B', now, gid,nonce);

			byte[] finalmsg = conn.sign(message);
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

	private void println(String str) {
		System.out.println(str);
	}

	private String readString(String prompt, Main main) {
		// Used BufferedReader instead of System console because the later doesn't work
		// with
		// Eclipse IDE.
		main.println(prompt);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		try {
			input = bufferedReader.readLine();
		} catch (IOException ioe) {
			println("Problems with reading user input.");
		}
		return input;
	}
	private String readPassword(){
		Console console =  System.console();
		char [] input = console.readPassword("Please enter your secret password:  ");
		return String.valueOf(input);
	}


	public NotaryConnection getNotaryConnection(){
	    return conn;
    }
}
