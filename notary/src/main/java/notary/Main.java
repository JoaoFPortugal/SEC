package notary;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

	// Connection stuff
	public static int port = 6066;
	private static Server server = null;

	// Thread stuff
	private static Thread producer; // One producer is enough and FIFO.
	private static Thread consumer;

	// Database stuff
	public static String db_name = "notary.db";
	public static Database db = null;

	public static void main(String[] args) {


		try {
			db = new Database(db_name);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		try {
			server = new Server(port, db);
			producer = new Thread(server, "producer");
			producer.start();
			consumer = new Thread(server, "consumer");
			consumer.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
