package hds_user;

import java.io.IOException;

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
			Main.println("4. Exit");
			String input = Main.readString();
			try{
				option = Integer.parseInt(input);
			}
			catch(Exception e){
				Main.println("Wrong input!");
			}

			switch(option) {
				case 1:
					Main.print("Good id: ");
					String good = Main.readString();
					int id;
					try {
						id = Integer.parseInt(good);
					} catch (NumberFormatException e) {
						Main.println("Not a valid id.");
						break;
					}
					try {
						conn.getStateOfGood(id);
					} catch (IOException e){
						e.printStackTrace();
					}
				case 2:
					break;
				case 3:
					break;
				case 4:
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

	public static String readString(){
		String input = System.console().readLine();
		return input;
	}
}
