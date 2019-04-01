package hds_user;

import java.util.ArrayList;
import java.util.List;

public class Main {
	
	// Connection stuff
	private static String serverName = "localhost";
	private static int port = 6066;
	private static Connection conn;
	
	// User stuff
	private static List<Good> setOfGoods; // name and good
	
	public static void main(String[] args) {
		
		conn = new Connection(serverName, port);
		
		setOfGoods = new ArrayList<Good>();
		
	}
}
