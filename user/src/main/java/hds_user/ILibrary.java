package hds_user;

import java.io.IOException;

public interface ILibrary {
	/**
	 * Sends a request to the notary to know if the good is for sale and who owns it.
	 */
	String getStateOfGood(String good) throws IOException;

	/**
	 * Sends a request to the notary expressing that a good is for sale.
	 * Fails if user doesn't own it.
	 */
	boolean intentionToSell();

	/**
	 * buyGood is sent to another user through peer-to-peer, it will be moved out of
	 * here.
	 
	boolean buyGood();
	*/
	/**
	 * Sends a request to the server to change the owner of a good.
	 * Fails if user doesn't own it.
	 */
	boolean transferGood();
}
