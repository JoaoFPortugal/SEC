package hds_security.exceptions;

public class InvalidSignatureException extends Exception {

    private static final long serialVersionUID = 1L;
    public String toString() {
        return "Message signed differs from what is expected";
    }
}
