package hds_security.exceptions;


public class NullDestination extends Exception {

    private static final long serialVersionUID = 1L;
    public String toString() {
        return "Message destination cannot be null.";
    }
}
