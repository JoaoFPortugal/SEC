package hds_user;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import hds_user.exceptions.ReplayAttackException;
import notary.Request;

public class UserListener implements Runnable {

	private int portNumber;
	private Thread t;
	private String threadName;
	protected Vector<UserConnection> clientConnections;
	private Random rand = new Random();
	private HashMap<Long,Long> noncemap = new HashMap<>();
	private Main main;

	public UserListener(int port, String name, Main main) {
		this.portNumber = port;
		this.threadName = name;
		this.clientConnections = new Vector<>();
		this.main = main;
	}

	@Override
	public void run() {

		try (
				ServerSocket serverSocket = new ServerSocket(portNumber);
		) {
			while (true) {
                Socket clientSocket = serverSocket.accept();
				Request request = new Request(clientSocket);
				verifyFreshness(request.now,request.nonce);
				System.out.println(request.gid);
				UserConnection cn = new UserConnection(this, clientSocket,
						"Client: " + clientSocket.getInetAddress(), request,main);
				cn.start();
				this.clientConnections.add(cn);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("Thread " +  threadName + " exiting.");
	}


	private void verifyFreshness(long mnow, long mnonce) throws ReplayAttackException {
		Date date = new Date();
		long now = date.getTime();

		System.out.println(mnow);
		System.out.println(now);
		if(mnow + 10000 <= now){
			throw new ReplayAttackException();
		}

		else{
			Long nonce = noncemap.get(mnow);
			if(!(nonce == null)){
				if(nonce == mnonce){
					throw new ReplayAttackException();
				}
			}

			else{
				noncemap.put(mnow,mnonce);
			}
		}
	}


	public void start() {
		System.out.println("Starting " + threadName);
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
