package notary;

import java.io.IOException;
import java.util.ArrayList;
import hds_security.Utils;

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
		String port = Utils.readString(
				"Port:\n");
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
			for (int i = 0; i < cores; i++) {
				Thread c = new Thread(main.server, "consumer_" + i);
				main.consumers.add(c);
				c.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
