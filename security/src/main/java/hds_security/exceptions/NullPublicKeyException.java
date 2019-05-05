package hds_security.exceptions;


public class NullPublicKeyException extends Exception {

    private static final long serialVersionUID = 1L;
    public String toString() {
        return "Message public key cannot be null.";
    }
}
