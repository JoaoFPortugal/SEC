package hds_user;

public class Main {
	
	// Connection stuff
	private static String serverName = "localhost";
	private static int port = 6066;
	private static Connection conn;
	
	public static void main(String[] args) {		

		conn = new Connection(serverName, port);
		
		Main.println("Hello! Please enter your name");
		String name = Main.readString();

		User user = new User(name);

		while(true){
			int option=0;
			Main.println("What would you like to do?");
			Main.println("1- Do something");
			Main.println("2- Do something");
			Main.println("3- DO something");
			Main.println("4- Exit");
			String input = Main.readString();
			try{
				option = Integer.parseInt(input);
			}
			catch(Exception e){
				Main.println("Wrong input!");
			}

			switch(option){
				case 1:
					break;
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
	
	public static void println(String str){
		System.out.println(str);
	}

	public static String readString(){
		String input = System.console().readLine();
		return input;
	}
}
