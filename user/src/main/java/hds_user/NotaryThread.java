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
import java.util.List;

public class NotaryThread extends Thread {

    private final ReadWriteLock readWriteLock ;
    private final NotaryConnection notary;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final int writer;
    private final int port;
    private boolean quorumAchieved = false;
    private int cc;

    public NotaryThread(NotaryConnection notary, DataInputStream in, DataOutputStream out, ReadWriteLock readWriteLock, int writer, int port, int cc){
        this.notary = notary;
        this.in = in;
        this.out = out;
        this.readWriteLock  = readWriteLock;
        this.writer = writer;
        this.port = port;
        this.cc = cc;
    }

    @Override
    public void run(){
        SecureSession secureSession = new SecureSession();
        Message m = null;

        if(writer == 0){
            read(secureSession,m);

        }

        if(writer == 1){
            write(secureSession,m);
        }
        this.interrupt();
    }


    public void read(SecureSession secureSession, Message m) {
        try {
            if(cc==1){
                m= secureSession.readFromCC(in, "./src/main/resources/serverPublicKey.txt");
            }
            else {
                System.out.println("Connected and waiting for server from port " + port);
                m = secureSession.readFromUser(in, Integer.toString(port));
            }
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidSignatureException | IllegalAccessException | ReplayAttackException | NullPublicKeyException e) {
            e.printStackTrace();
        }
        assert m != null;

        try {
            readWriteLock.lockRead();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(m.getTag());
        quorumAchieved = notary.getQuorum();

        if (quorumAchieved) {
            try {
                readWriteLock.lockWrite();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int finalTag = notary.getFinalTag();
            System.out.println("finaltag: " + finalTag);
            System.out.println("msgtag: " + m.getTag());
            if(finalTag > m.getTag()) {
                List<Message> writesMessages = notary.getWritesMessages();
                User user = notary.getUser();
                for (Message msg : writesMessages){
                    try {
                        Utils.write(msg, out, user.getPrivateKey());
                    }catch( IOException | InvalidKeyException | NoSuchAlgorithmException | SignatureException e){
                        e.printStackTrace();
                    }
                }
                }
            try {
                readWriteLock.unlockWrite();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readWriteLock.unlockRead();
        }

        else {
            try {
                readWriteLock.lockWrite();
                notary.responseFromServer(m,port,out,in);
                readWriteLock.unlockRead();
                readWriteLock.unlockWrite();

            } catch (InterruptedException | IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(SecureSession secureSession, Message m){
        while(true) {
            try {
                if(cc==1){
                    m= secureSession.readFromCC(in, "./src/main/resources/serverPublicKey.txt");
                }
                else {
                    System.out.println("Connected and waiting for server from port " + port);
                    m = secureSession.readFromUser(in, Integer.toString(port));
                }
            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidSignatureException | IllegalAccessException | ReplayAttackException | NullPublicKeyException e) {
                e.printStackTrace();
            }

            if (m.getOperation() !='G'){
                break;
            }
        }
        notary.returnReply(m);

    }
}

