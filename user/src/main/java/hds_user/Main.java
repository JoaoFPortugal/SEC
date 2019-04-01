package hds_user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hds_user.exceptions.*;

public class Main {
	
	// Connection stuff
	private static String serverName = "localhost";
	private static int port = 6066;
	private static Connection conn;

	public static void main(String[] args) {

		conn = new Connection(serverName, port);
		
		println("Hello!");
		
		int uid = 0;
		while (true) {
			try{
				uid = Integer.parseInt(Main.readString("Please enter your ID: ", false));
				break;
			}
			catch(Exception e){
				Main.println("ID must be a number.");
			}
		}

		User user = null;
		try {
			user = new User(uid, conn.getListOfGoods());
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		} catch (InexistentGoodsException e) {
			e.toString();
			System.exit(0);
		}
		
		//user.printAllGoods();

		while(true){
			
			int option=0;
			
			Main.println("What would you like to do?");
			Main.println("1. Get State of Good");
			Main.println("2. Do something");
			Main.println("3. DO something");
			Main.println("0. Exit");
			
			String input = Main.readString();
			
			try{
				option = Integer.parseInt(input);
			}
			catch(Exception e){
				Main.println("Wrong input!");
				continue;
			}

			switch(option) {
				case 1:
					printStateOfGood();
				case 2:
					break;
				case 3:
					break;
				case 0:
					System.exit(0);
				default:
					Main.println(("Wrong number!"));
			}

		}

	}
	
	public static void printStateOfGood() {
		
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
			g = conn.getStateOfGood(gid);
		} catch (IOException e){
			e.printStackTrace();
			return;
		} catch (InexistentGoodException e) {
			println(e.toString());
			return;
		}
		
		println("Good with ID=" + gid + " belongs to user with ID=" + g.getOwner() +
				" and is " + (g.getForSale() ? "" : "not ") + "for sale.");
	}
	
	public static void print(String str){
		System.out.print(str);
	}
	
	public static void println(String str){
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
		// Used BufferedReader instead of System console because the later doesn't work with
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
}
