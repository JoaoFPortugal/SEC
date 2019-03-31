package hds_user;

import java.io.IOException;

public class Main {
	
	private static String serverName = "localhost";
	private static int port = 6066;
	private static Connection conn;
	
	public static void main(String[] args) {
		
		try {
			System.out.println("Connecting to server on port " + port);
			conn = new Connection(serverName, port);
			System.out.println("Just connected to " + conn.getAddr());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
