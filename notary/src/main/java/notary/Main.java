package notary;

import java.io.IOException;

public class Main {

	// Connection stuff
	private int port = 6066;
	private Server server;

	// Thread stuff
	private Thread producer; // One producer is enough and FIFO.
	private Thread consumer;

	// Database stuff
	private String db_name = "notary.db";
	private Database db;

	public static void main(String[] args) {
		Main main = new Main();

		main.db = new Database(main.db_name);

		try {
			main.server = new Server(main.port, main.db);
			main.producer = new Thread(main.server, "producer");
			main.producer.start();
			main.consumer = new Thread(main.server, "consumer");
			main.consumer.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
