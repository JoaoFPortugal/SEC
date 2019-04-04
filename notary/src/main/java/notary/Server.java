package notary;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import hds_security.Message;
import notary.exceptions.InvalidSignatureException;

public class Server extends Thread {
	
    private Database db;
    private ServerSocket serverSocket;
    
    // http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html
    private BlockingQueue<Request> requests;
    
    // Does not allow more than 'max_queue' requests on the queue (for resources concern)
    private int max_queue = 1024;

    public Server(int port, Database db) throws IOException {
        this.db = db;
        serverSocket = new ServerSocket(port);
        
        /**
         * 1st parameter: capacity - the capacity of this queue
         * 2nd parameter: fair - if true then queue accesses for threads blocked on
         * insertion or removal, are processed in FIFO order; if false the access
         * order is unspecified. 
         */
        requests = new ArrayBlockingQueue<Request>(max_queue, true);
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

            // 'put' blocks, 'add' throws exception
            requests.put(request);

            System.out.println("Request created from: " + request.getAddr());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void runConsumer() {

        try {
        	// 'take' blocks, 'remove' throws exception
            Request request = requests.take();

            Date date = new Date();
            long now = date.getTime();

            if (request.operation == 'S'){
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

        } catch ( InterruptedException e) {
            e.printStackTrace();
        }
    }

}