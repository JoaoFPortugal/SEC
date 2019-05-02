package hds_security.exceptions;


public class ReplayAttackException extends Exception {

    private static final long serialVersionUID = 1L;
    public String toString() {
        return "Nonce already in list, maybe replay attack?";
    }
}
