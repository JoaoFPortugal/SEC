package hds_user.exceptions;

public class InexistentGoodException extends Exception {

	private static final long serialVersionUID = 1L;

	int id;

	public InexistentGoodException(int id) {
		this.id = id;
	}

	public String toString() {
		return "No good with ID=" + id + ".";
	}
}
