package notary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.lang.Character;
import hds_security.Message;
import notary.exceptions.InvalidSignatureException;

public class Server extends Thread {
    private Database db;
    private ServerSocket serverSocket;
    private Queue<Request> requests;
    // Only consumes when there is something in the queue, starts with permit = 0
    private Semaphore sem_producer;
    // Does not allow more than 'max_queue' requests on the queue (for resources concern)
    private int max_queue = 100;
    private Semaphore sem_queue;

    public Server(int port, Database db) throws IOException {
        this.db = db;
        serverSocket = new ServerSocket(port);
        requests = new LinkedList<>();
        sem_producer = new Semaphore(0);
        sem_queue = new Semaphore(max_queue);
    }

    @Override
    public void run() {
        while(true){
            if(Thread.currentThread().getName().equals("producer")){
            	runProducer();
            }
            else{
            	runConsumer();
            }
        }
    }

    public void runProducer() {
        try {

            System.out.println("Waiting for client on port " +
                    serverSocket.getLocalPort() + "...");

            Request request = null;
            try {
                request = new Request(serverSocket.accept());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException | InvalidSignatureException e) {
                e.printStackTrace();
            }

            // Add request to Queue and decrement the semaphore count of allowed number of requests
            sem_queue.acquire();
            requests.add(request);

            System.out.println("Request created from: " + request.getAddr());

            // Allow consumer to handle requests
            sem_producer.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void runConsumer() {

        try {
        	// Handling a request decrements the count of requests to handle
            sem_producer.acquire();

            if(!requests.isEmpty()){
                db.selectAllGoods();
                Request request = requests.remove();
                // More requests can be added to the Queue
                sem_queue.release();

                Character op = request.operation;


                Date date = new Date();
                long now = date.getTime();

                if (op.equals('S')){
                    int reply = db.checkIntentionToSell(request.gid, request.origin);
                    Message message = new Message(-1,-1, 'R', now, reply);
                    try {
                        request.write(message);
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                }



                /*
                String msg = request.getMessage();



                // Not a valid message
                if (msg.isEmpty()) return;
                String[] tokens = msg.split(" ");
                
                if (tokens[0].equals("getListOfGoods")) {
                	request.write(db.getListOfGoods());
                } else if (tokens[0].equals("getStateOfGood") && tokens.length == 2) {
                	int id;
                	try {
                		id = Integer.parseInt(tokens[1]);
                	} catch (NumberFormatException nfe) {
                		// Not a valid message
                		return;
                	}
                	request.write(db.getStateOfGood(id));
                } else if (tokens[0].equals("intentionToSell") && tokens.length == 2) {
                    int id;
                    try {
                        id = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException nfe) {
                        // Not a valid message
                        return;
                    }
                    request.write( db.checkIntentionToSell(id));
                } else {
                	// Not a valid message
                	return;
                }*/

            }
        } catch ( InterruptedException e) {
            e.printStackTrace();
        }
    }

}