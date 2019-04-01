package hds_user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hds_user.exceptions.InexistentGoodException;

public class Main {
	
	// Connection stuff
	private static String serverName = "localhost";
	private static int port = 6066;
	private static Connection conn;

	public static void main(String[] args) {

		conn = new Connection(serverName, port);
		
		Main.print("Hello! Please enter your ID: ");
		String sid = Main.readString();
		int uid = 0;
		try{
			uid = Integer.parseInt(sid);
		}
		catch(Exception e){
			Main.println("ID must be a number.");
			System.exit(0);
		}

		User user = new User(uid);

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
			}

			switch(option) {
				case 1:
					Main.print("Good ID: ");
					String good = Main.readString();
					int gid;
					try {
						gid = Integer.parseInt(good);
					} catch (NumberFormatException e) {
						Main.println("Not a valid ID.");
						break;
					}
					Good g = null;
					try {
						g = conn.getStateOfGood(gid);
					} catch (IOException e){
						e.printStackTrace();
						break;
					} catch (InexistentGoodException e) {
						println(e.toString());
						break;
					}
					println("Good with ID=" + gid + " belongs to user with ID=" + g.getOwner() +
							" and is " + (g.getForSale() ? "" : "not ") + "for sale.");
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
	
	public static void print(String str){
		System.out.print(str);
	}
	
	public static void println(String str){
		System.out.println(str);
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
