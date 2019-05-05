package hds_security.exceptions;


public class NullPrivateKeyException extends Exception {

    private static final long serialVersionUID = 1L;
    public String toString() {
        return "Message private key cannot be null.";
    }
}
