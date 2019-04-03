package hds_security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class SignMessage {

    public byte[] sign(byte[] message,PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature ecdsa;
        ecdsa = Signature.getInstance("SHA256withECDSA");
        ecdsa.initSign(privateKey);
        ecdsa.update(message);
        byte[] signature = ecdsa.sign();
        return signature;
    }

        public boolean verify(byte[] plaintextMessage, byte[] signedMessage, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
            Signature signature;
            signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(plaintextMessage);
        return signature.verify(signedMessage);
    }
}
