package hds_user.exceptions;

public class InexistentGoodsException extends Exception {

	private static final long serialVersionUID = 1L;

	public String toString() {
		return "No goods in the notary!";
	}
}
