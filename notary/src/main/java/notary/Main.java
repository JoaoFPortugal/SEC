package notary;

import java.util.ArrayList;
import hds_security.Utils;

public class Main {

	// Connection stuff
	private Server server;

	// Thread stuff
	private Thread producer; // One producer is enough and FIFO.
	private ArrayList<Thread> consumers;

	// Database stuff
	private String db_name = "notary";
	private Database db;

	// Singleton
	private Main() {
	}

	public static void main(String[] args) {
		Main main = new Main();

		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("CPU cores: " + cores);

		int port = Utils.readInt("Port:\n");
		while (port <= 1023 || port > 65535) {
			// Ports 0-1023 are reserved in Linux for special services.
			port = Utils.readInt("Port number should be between 1024 and 65535.\nPort:\n");
		}

		// Will fail if .db file does not exist.
		main.db = new Database(main.db_name + String.format("%04d", port) + ".db");
		
		String pass = Utils.readString("Password:\n");
		int cc = Utils.readInt("Hello!\n" + "Would you like to use your CC?\n" + "0. No\n" + "1. Yes\n");

		try {
			main.server = new Server(port, main.db, cc, pass);
			main.producer = new Thread(main.server, "producer");
			main.producer.start();

			/**
			 * Have number of consumers equal to number of CPU cores so that we can process
			 * more number of requests while maintaining a "first come first served" model
			 * and without delaying the answer to each client.
			 */
			main.consumers = new ArrayList<>(cores);
			for (int i = 0; i < 1; i++) {
				Thread c = new Thread(main.server, "consumer_" + i);
				main.consumers.add(c);
				c.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
