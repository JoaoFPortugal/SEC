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
		db.selectAllGoods();

		try {
			Server server = new Server(port);
			Thread producer = new Thread(server,"producer");
			Thread consumer = new Thread(server,"consumer");
			producer.start();
			consumer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
