package notary;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Server extends Thread {
    private ServerSocket serverSocket;
    private Queue<Request> requests;
    private Semaphore sem;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        requests = new LinkedList<>();
        sem = new Semaphore(1);
    }

    @Override
    public void run() {
        while(true){
            if(Thread.currentThread().getName().equals("producer")){
                runproducer();
            }
            else{
                runconsumer();
            }
        }
    }

    public void runproducer() {
        try {

            System.out.println("Waiting for client on port " +
                    serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();

            Request request = new Request(server);
            requests.add(request);

            System.out.println("pushed");

            System.out.println("Just connected to " + server.getRemoteSocketAddress());



            sem.release();

            //System.out.println(in.readUTF());
        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void runconsumer(){

        try {
            System.out.println("a");
            sem.acquire();
            if(!requests.isEmpty()){
                Request request = requests.remove();
                String message = request.getMessageFromClient();

                System.out.println(requests.isEmpty());

                DataOutputStream out = request.getOutputFromClient();
                out.writeUTF("Thank you for connecting" + "\nYES on "+ message + "\nGoodbye!");

            }
            Thread.sleep(1000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}