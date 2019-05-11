package hds_user;

import java.util.List;

public class Main {

	// Connection stuff
	private final String serverName = "localhost";
	private final int[] notaryPorts = {6066, 6067, 6068, 6069, 6070};
	private int userListenerPort;
	private User user;

	public static void main(String[] args) {

		Main main = new Main();

		int uid = Utility.readInt("Hello!\nPlease enter your ID: ");

		// For listening to user requests
		main.userListenerPort = Utility.readIntFromFile("./src/main/resources/" + uid + "_port.txt");

		String password = Utility.readPassword("Please enter your secret password: ");
		
		try {
			main.user = new User(uid, password, main.serverName, main.notaryPorts, main.userListenerPort);
		} catch (Exception e) {
			e.printStackTrace();
			Utility.println(e.getMessage());
			System.exit(0);
		}

		while (true) {

			String menuStr = "What would you like to do?\n" + "1. Get State of Good\n" + "2. Intention to sell\n"
					+ "3. Intention to buy\n" + "0. Exit\n";

			int option = Utility.readInt(menuStr);

			switch (option) {
			case 1:
				main.user.getStateOfGood(uid);
				break;
			case 2:
				main.user.intentionToSell(uid);
				break;
			case 3:
				main.user.buyGood(uid);
				break;
			case 0:
				System.exit(0);
			default:
				Utility.println(("Wrong number!"));
			}

		}

	}
	
	public User getUser() {
		return user;
	}
}
