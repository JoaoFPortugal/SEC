package notary;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

	// Connection stuff
	public static int port = 6066;
	private static Server server = null;

	// Thread stuff
	private static Thread producer; // One producer is enough and FIFO.
	private static ArrayList<Thread> consumers;

	// Database stuff
	public static String db_name = "notary.db";
	public static Database db = null;

	public static void main(String[] args) {

		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("CPU cores: " + cores);

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
			/**
			 * Have number of consumers equal to number of CPU cores so that we can process
			 * more number of requests while maintaining a "first come first served" model
			 * and without delaying the answer to each client.
			 */
			consumers = new ArrayList<>(cores);
			for (int i = 0; i < cores; i++) {
				Thread c = new Thread(server, "consumer_" + i);
				consumers.add(c);
				c.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
