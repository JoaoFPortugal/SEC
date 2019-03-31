package hds_user;

public class Main {
	
	private static String serverName = "localhost";
	private static int port = 6066;
	private static Connection conn;
	
	public static void main(String[] args) {
		
		conn = new Connection(serverName, port);
			
	}
}
