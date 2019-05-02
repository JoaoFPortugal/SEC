package notary;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

	// Connection stuff
	private int port = 6066;
	private Server server;

	// Thread stuff
	private Thread producer; // One producer is enough and FIFO.
	private ArrayList<Thread> consumers;

	// Database stuff
	private String db_name = "notary.db";
	private Database db;

	// Singleton
	private Main() {
	}

	public static void main(String[] args) {
		Main main = new Main();

		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("CPU cores: " + cores);

		main.db = new Database(main.db_name);

		try {
			main.server = new Server(main.port, main.db);
			main.producer = new Thread(main.server, "producer");
			main.producer.start();

			/**
			 * Have number of consumers equal to number of CPU cores so that we can process
			 * more number of requests while maintaining a "first come first served" model
			 * and without delaying the answer to each client.
			 */
			main.consumers = new ArrayList<>(cores);
			for (int i = 0; i < cores; i++) {
				Thread c = new Thread(main.server, "consumer_" + i);
				main.consumers.add(c);
				c.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
