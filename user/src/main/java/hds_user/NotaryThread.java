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
    private final int port;
    private boolean quorumAchieved = false;

    public NotaryThread(NotaryConnection notary, DataInputStream in, DataOutputStream out, ReadWriteLock readWriteLock, int writer, int port){
        this.notary = notary;
        this.in = in;
        this.out = out;
        this.readWriteLock  = readWriteLock;
        this.writer = writer;
        this.port = port;
    }

    @Override
    public void run(){
        SecureSession secureSession = new SecureSession();
        Message m = null;

        if(writer == 0){
            read(secureSession,m);
        }

        if(writer == 1){
            read(secureSession,m);
            write(secureSession,m);
        }
        }


    public void read(SecureSession secureSession, Message m) {

        try {
            m = secureSession.readFromUser(in,Integer.toString(port));
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidSignatureException | IllegalAccessException | ReplayAttackException | NullPublicKeyException e) {
            e.printStackTrace();
        }
        assert m != null;

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
                    try {
                        Utils.write(new Message(finalValue.getOrigin(), 'W', finalValue.getGoodID(), finalValue.getFor_sale(), finalValue.getTag()), out, notary.getUser().getPrivateKey());
                    }catch(Exception e){
                        e.printStackTrace();
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

    public void write(SecureSession secureSession, Message m){
        try {
            m = secureSession.readFromUser(in,Integer.toString(port));
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidSignatureException | IllegalAccessException | ReplayAttackException | NullPublicKeyException e) {
            e.printStackTrace();
        }
        notary.returnReply(m);

    }
}

