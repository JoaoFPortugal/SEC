package hds_user;

import java.io.Console;
import java.io.File;
import java.io.IOException;

import static java.lang.System.exit;

public class Main {

	public void println(String str){
		System.out.println(str);
	}

	public String readString(){
		Console console = System.console();
		String input = console.readLine();
		return input;
	}
	
	private static String serverName = "localhost";
	private static int port = 6066;

	public static void main(String[] args) throws IOException {
		Main main = new Main();

		Connection conn = new Connection(serverName, port);
		main.println("Hello! Please enter your name");

		String name = main.readString();
		User user = new User(name);

		while(true){
			int option=0;
			main.println("What would you like to do?");
			main.println("1- Do something");
			main.println("2- Do something");
			main.println("3- DO something");
			main.println("4- Exit");
			String input = main.readString();
			try{
				option = Integer.parseInt(input);
			}
			catch(Exception e){
				main.println("Wrong input!");
			}

			switch(option){
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					conn.disconnect();
					exit(0);
				default:
					main.println(("Wrong number!"));
			}

		}

	}
}
