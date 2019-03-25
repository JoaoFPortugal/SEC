package notary;

import java.io.IOException;

public class Main {
	
	public static int port = 6066;
	public static String db_name = "notary.db";
	public static Database db = null;
	
	public static void main(String [] args) {
		
		try {
			db = new Database(db_name);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db.selectAllUsers();
		
        try {
            Server server = new Server(port);
            Thread t1 = new Thread(server,"thread1");
            Thread t2 = new Thread(server,"thread2");
            t1.start();
            t2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
