package hds_user;


import hds_security.Message;
import hds_security.SecureSession;
import hds_security.Utils;
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
    private final int writer;
    private boolean quorumAchieved = false;

    public NotaryThread(NotaryConnection notary, DataInputStream in, DataOutputStream out, ReadWriteLock readWriteLock, int writer){
        this.notary = notary;
        this.in = in;
        this.out = out;
        this.readWriteLock  = readWriteLock;
        this.writer = writer;
    }

    @Override
    public void run(){

        if(writer == 0){
            read();
        }

        if(writer == 1){
            read();
            write();
        }
        }


    public void read() {
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
        if (quorumAchieved) {
            int finalTag = notary.getFinalTag();
            Message finalValue = notary.getFinalValue();
            readWriteLock.unlockRead();
            if(finalTag > m.getTag()) {
                if (!finalValue.isEqual(m)) { //if notupdated
                    try {
                        Utils.write(new Message(finalValue.getOrigin(), 'W', finalValue.getGoodID(), finalValue.getFor_sale(), finalValue.getTag()), out, notary.getUser().getPrivateKey());
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        else {
            try {
                readWriteLock.lockWrite();
                notary.responseFromServer(m);
                readWriteLock.unlockRead();
                readWriteLock.unlockWrite();

            } catch (InterruptedException | IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(){

    }
}

