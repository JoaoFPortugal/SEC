package hds_user;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import hds_security.Message;
import hds_security.SecureSession;
import hds_security.Utils;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullDestination;
import hds_security.exceptions.NullPrivateKeyException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;
import java.util.HashMap;

public class NotaryConnection {

	private String serverName;
	private int[] ports;
	private List<Socket> servers = new ArrayList<>();
	private List<DataOutputStream> outs = new ArrayList<>();
	private List<DataInputStream> ins = new ArrayList<>();
	private User user;
	private SecureSession notarySS;
	private String serverPubKeyPath;
	private AtomicInteger currentTag;
	private int[] portsBehindTag;
	private boolean quorumAchieved = false;
	private int finalTag=0;
	private Message finalValue;
	private ReadWriteLock readWriteLock = new ReadWriteLock();
	private int tag;
	private int responses=0;
	private HashMap<Integer,Message> responsesMessages = new HashMap<>();

	public NotaryConnection(String serverName, int[] ports, User user) {
		this.serverName = serverName;
		this.ports = ports;
		this.user = user;
		notarySS = new SecureSession();
		this.serverPubKeyPath = "./src/main/resources/serverPublicKey.txt";
		this.tag = 0;
	}

	private void connect() throws IOException {
	    for(int port : ports) {
            servers.add(new Socket(serverName, port));
        }

        for (Socket server : servers) {
            outs.add(new DataOutputStream(server.getOutputStream()));
            ins.add(new DataInputStream(server.getInputStream()));
        }

		for (int i = 0; i < ports.length; i++) {
			NotaryThread t = new NotaryThread(this, ins.get(i), outs.get(i), readWriteLock);
			t.run();
		}
	}

	private void disconnect() throws IOException {
        for (Socket server : servers) {
            server.close();
        }
    }

	public void sendRead(int gid, int uid)throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		for (DataOutputStream out : outs){
			Utils.write(new Message(uid, 'G', gid, -1), out, user.getPrivateKey());
		}
	}

	/**
	 * Sends a request to the notary to know if the good is for sale and who owns
	 * it. Returns a Good object on success.
	 */

	public Good getStateOfGood(int gid, int uid) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
        connect();
        for (DataOutputStream out : outs){
            Utils.write(new Message(uid, 'G', gid, -1), out, user.getPrivateKey());
        }


        //below is wrong
		Message replyMessage = notarySS.readFromCC(in, serverPubKeyPath);

		// Origin value of reply is actually the 'owner' value.
		// Good ID value of reply is actually the 'for_sale' value.
		if(replyMessage.getOrigin() < 0) {
			return null;
		}
		Good g = new Good(gid, replyMessage.getOrigin(), replyMessage.getGoodID() == 1);

		disconnect();
		return g;
	}

	/**
	 * Sends a request to the notary expressing that a good is for sale. Fails if
	 * user doesn't own it.
	 */
	public int intentionToSell(int gid, int uid) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect();

		sendRead(gid, uid);

		Utils.write(new Message(uid, 'S', gid), out, user.getPrivateKey());

		//below is wrong

		Message replyMessage = notarySS.readFromCC(in, serverPubKeyPath);

		disconnect();
		// Good id contains 'for_sale' value.
		return (replyMessage.getGoodID());
	}

	/**
	 * Sends a request to the server to change the owner of a good. Fails if user
	 * doesn't own it. Returns good ID on success.
	 */

	public synchronized int getFinalTag(){
		return finalTag;
	}

	public synchronized void setFinalTag(int finalTag){
		this.finalTag = finalTag;
	}

	public synchronized Message getFinalValue(){
		return finalValue;
	}

	public synchronized void setFinalValue(Message m){
		this.finalValue = m;
	}

	public synchronized boolean getQuorum(){
		return quorumAchieved;
	}

	public synchronized void setQuorum(boolean bool){
		quorumAchieved = bool;
	}
	public User getUser(){
		return user;
	}

	public synchronized void responseFromServer(Message m) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		responses +=1;

		if (responses<=3) {
			int tag = m.getTag();
			responsesMessages.put(tag, m);

			if (tag > getFinalTag()) {
				setFinalTag(tag);
				setFinalValue(m);
			}
		}

		if(responses==3){
			setQuorum(true);
		}

	}



	public Message transferGood(int good, int owner, int buyer) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect();

		Utils.write(new Message(owner, buyer, 'T', good), out, user.getPrivateKey());

		//below is wrong
		Message replyMessage = notarySS.readFromCC(in, serverPubKeyPath);

		disconnect();
		return replyMessage;
	}

}
