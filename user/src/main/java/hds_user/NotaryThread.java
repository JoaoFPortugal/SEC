package hds_user;


import hds_security.Message;
import hds_security.SecureSession;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class NotaryThread implements Runnable {

    private final ReadWriteLock readWriteLock ;
    private final NotaryConnection notary;
    private final DataInputStream in;
    private final DataOutputStream out;
    private boolean quorumAchieved = false;

    public NotaryThread(NotaryConnection notary, DataInputStream in, DataOutputStream out){
        this.notary = notary;
        this.in = in;
        this.out = out;
        this.readWriteLock = new ReadWriteLock();
    }

    @Override
    public void run(){
        SecureSession secureSession = new SecureSession();
        Message m = null;
        try {
            m = secureSession.readFromUser(in);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidSignatureException | IllegalAccessException | ReplayAttackException | NullPublicKeyException e) {
            e.printStackTrace();
        }
        assert m != null;
        int receivedTag = m.getTag();
        try {
            readWriteLock.lockRead();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        quorumAchieved = notary.getQuorum();
        if(quorumAchieved){
            int finalTag = notary.getFinalTag();
            Message finalValue = notary.getFinalValue();
            if(!finalValue.isEqual(m)){
                //writeback
            }
        }
        else{
            try {
                readWriteLock.lockWrite();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
