package hds_user;

import java.util.List;
import hds_security.Utils;

public class Main {

	// Connection stuff
	private final String serverName = "localhost";
	private final int[] notaryPorts = {6066, 6067, 6068, 6069, 6070};
	private int userListenerPort;
	private User user;

	public static void main(String[] args) {

		Main main = new Main();

		int uid = Utils.readInt("Hello!\nPlease enter your ID: ");

		// For listening to user requests
		main.userListenerPort = Utils.readIntFromFile("./src/main/resources/" + uid + "_port.txt");

		String password = Utils.readPassword("Please enter your secret password: ");

		int cc = Utils.readInt("Hello!\n" + "Is the server using CC?\n" + "0. No\n" + "1. Yes\n");
		
		try {
			int[] ccports = {6066};
			if(cc==1){
				main.user = new User(uid, password, main.serverName,ccports , main.userListenerPort, cc);

			}
			else{
				main.user = new User(uid, password, main.serverName, main.notaryPorts, main.userListenerPort, cc);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.println(e.getMessage());
			System.exit(0);
		}

		while (true) {

			String menuStr = "What would you like to do?\n" + "1. Get State of Good\n" + "2. Intention to sell\n"
					+ "3. Intention to buy\n" + "0. Exit\n";

			int option = Utils.readInt(menuStr);

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
				Utils.println(("Wrong number!"));
			}

		}

	}
	
	public User getUser() {
		return user;
	}
}
