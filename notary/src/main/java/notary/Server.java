package notary;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

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
                try {
                    runConsumer();
                } catch (PteidException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (PKCS11Exception e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
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

    public void runConsumer() throws PteidException, IllegalAccessException, PKCS11Exception, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {

        try {
            // 'take' blocks, 'remove' throws exception
            Request request = requests.take();

            Date date = new Date();
            long now = date.getTime();

            if (request.operation == 'S'){
                int reply = db.checkIntentionToSell(request.origin, request.gid);
                Message message = new Message(-1,-1, 'R', now, reply);
                try {
                    request.writeServer(message);
                } catch(IOException e){
                    e.printStackTrace();
                }
            }

            else if ( request.operation=='G'){
                String query_result = db.getStateOfGood(request.gid);
                String[] splitted_query = query_result.split(" ");
                Message message = new Message(Integer.valueOf(splitted_query[0]), -1, 'R', now, Integer.valueOf(splitted_query[1]));
                try {
                    request.writeServer(message);
                } catch(IOException e){
                    e.printStackTrace();
                }
            }

            else if ( request.operation=='T'){
                int reply = db.transferGood(request.gid, request.origin, request.destin);
                Message message = new Message(-1, -1, 'R', now, reply);
                try {
                    request.writeServer(message);
                } catch(IOException e){
                    e.printStackTrace();
                }
            }



        } catch ( InterruptedException e) {
            e.printStackTrace();
        }
    }
}