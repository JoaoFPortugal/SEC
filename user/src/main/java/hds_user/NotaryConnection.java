package hds_user;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm;
import hds_security.Message;
import hds_security.SecureSession;
import hds_security.Utils;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullDestination;
import hds_security.exceptions.NullPrivateKeyException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

import javax.xml.crypto.Data;

public class NotaryConnection {

	private String serverName;
	private int[] ports;
	private List<Socket> servers = new ArrayList<>();
	private List<DataOutputStream> outs = new ArrayList<>();
	private List<DataInputStream> ins = new ArrayList<>();
	private User user;
	private SecureSession notarySS;
	private String serverPubKeyPath;

	private boolean quorumAchieved = false;
	private int finalTag=-1;
	private Message finalValue;
	private ReadWriteLock readWriteLock = new ReadWriteLock();
	private int responses=0;
	private int writeResponses=0;
	private final Object lock = new Object();
	private final Object lock1 = new Object();
	private boolean flag;
	private boolean notReceived;
	private Message reply;
	private HashMap<Integer,Integer> responsesMap = new HashMap<>();
	private HashMap<Integer,Integer> writesMap = new HashMap<>();
	private List<Message> writesMessages = new ArrayList<>();
	private HashMap<Integer,List<DataOutputStream>> outsMap = new HashMap<>();

	public NotaryConnection(String serverName, int[] ports, User user) {
		this.serverName = serverName;
		this.ports = ports;
		this.user = user;
		notarySS = new SecureSession();
		this.serverPubKeyPath = "./src/main/resources/serverPublicKey.txt";
		this.flag = true;
		this.notReceived = true;
	}

	private void connect(int wr) throws IOException {
		setQuorum(false);

		if(!servers.isEmpty()){
			servers.clear();
		}
		if(!outs.isEmpty()) {
			outs.clear();
		}
		if(!ins.isEmpty()){
			ins.clear();
		}
		if(!responsesMap.isEmpty()){
			responsesMap.clear();
		}
		if(!writesMap.isEmpty()){
			writesMap.clear();
		}



	    for(int port : ports) {
            servers.add(new Socket(serverName, port));
        }

		for (Socket server : servers) {
            outs.add(new DataOutputStream(server.getOutputStream()));
            ins.add(new DataInputStream(server.getInputStream()));
        }


		for (int i = 0; i < ports.length; i++) {
			NotaryThread t = new NotaryThread(this, ins.get(i), outs.get(i), readWriteLock, wr,ports[i]);
			t.start();
		}
	}


	public void sendRead(int gid, int uid)throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		for (DataOutputStream out : outs){
			Utils.write(new Message(uid, 'G', gid, -1,-1), out, user.getPrivateKey());
		}
	}

	/**
	 * Sends a request to the notary to know if the good is for sale and who owns
	 * it. Returns a Good object on success.
	 */

	public Good getStateOfGood(int gid, int uid) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect(0);

		for (DataOutputStream out : outs){
            Utils.write(new Message(uid, 'G', gid, -1,-1), out, user.getPrivateKey());
        }

		while(flag){
			waitLock();
		}

		flag = true;

		Message replyMessage = getFinalValue();

		// Origin value of reply is actually the 'owner' value.
		// Good ID value of reply is actually the 'for_sale' value.
		if(replyMessage.getOrigin() < 0) {
			return null;
		}
		Good g = new Good(gid, replyMessage.getOrigin(), replyMessage.getFor_sale() == 1);
		return g;
	}

	/**
	 * Sends a request to the notary expressing that a good is for sale. Fails if
	 * user doesn't own it.
	 */
	public int intentionToSell(int gid, int uid) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect(0);

		sendRead(gid, uid);

		while(flag){
			waitLock();
		}
		flag = true;

		writeBack();

		int tag = getFinalTag();

		try {
			Thread.sleep(1000);
		}catch(Exception e){
			e.printStackTrace();
		}

		connect(1);

		for (DataOutputStream out : outs) {
            System.out.println(tag+1);
			Utils.write(new Message(uid, 'S', gid, -1, tag + 1), out, user.getPrivateKey());
		}


		while(notReceived){
			waitLock1();
		}

		notReceived = true;

		return (reply.getFor_sale());
	}

	/**
	 * Sends a request to the server to change the owner of a good. Fails if user
	 * doesn't own it. Returns good ID on success.
	 */

	public Message transferGood(int good, int owner, int buyer) throws IOException, InvalidSignatureException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException, NullPrivateKeyException, NullDestination,
			NullPublicKeyException, InvalidKeySpecException, ReplayAttackException {
		connect(0);

		sendRead(good, owner);

		while(flag){
			waitLock();
		}
		flag = true;

		writeBack();

		int tag = getFinalTag();

        try {
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }

        connect(1);


		for (DataOutputStream out : outs) {
		    System.out.println(tag+1);
			Utils.write(new Message(owner, buyer, 'T', good, tag+1), out, user.getPrivateKey());
		}

		while(notReceived){
			waitLock1();
		}

		notReceived = true;

		//below is wrong

		return reply;
	}


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
	public synchronized List<Message> getWritesMessages(){
		return writesMessages;
	}
	public User getUser(){
		return user;
	}


	private synchronized void writeBack() {
		List<DataOutputStream> outsForWB;

		for (Integer v : responsesMap.keySet()) {
			if (responsesMap.get(v) < 4) {
				outsForWB = outsMap.get(v);

				for (DataOutputStream out : outsForWB) {
					for (Message msg : writesMessages){
						if (msg.getTag()>v) {
							try {
								Utils.write(msg, out, user.getPrivateKey());
							} catch (IOException | InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public synchronized void responseFromServer(Message m,int port,DataOutputStream out, DataInputStream in) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException{
		responses +=1;
        int counter=0;

		if(responsesMap.get(m.getTag())==null){
		    responsesMap.put(m.getTag(),1);

			List<DataOutputStream> p = new ArrayList<>();
			p.add(out);
		    outsMap.put(m.getTag(),p);

		}

        else{

			List<DataOutputStream> p = outsMap.get(m.getTag());
			p.add(out);
			outsMap.put(m.getTag(),p);

		    counter = responsesMap.get(m.getTag());
		    counter++;
		    responsesMap.replace(m.getTag(),counter);
        }


        if (counter==4){
		    int tag = m.getTag();
		    setFinalTag(tag);
		    setFinalValue(m);
            synchronized (lock) {
                flag = false;
                lock.notifyAll();
            }
            setQuorum(true);
        }



		if(responses == 5){
			responses = 0;
			setQuorum(false);
		}


	}

	public synchronized  void returnReply(Message m){
		int counter=0;

		if(writesMap.get(m.getTag())==null){
			writesMap.put(m.getTag(),1);
		}

		else{
			counter = writesMap.get(m.getTag());
			counter++;
			writesMap.replace(m.getTag(),counter);
		}

		if (counter==4){
			reply = m;
			writesMessages.add(m);
			synchronized (lock1) {
				notReceived = false;
				lock1.notifyAll();
			}
		}
	}

	private void waitLock(){
		synchronized (lock){
			try{
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void waitLock1(){
		synchronized (lock1){
			try{
				lock1.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}



}
