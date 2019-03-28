package notary;

import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class Server extends Thread {
    private ServerSocket serverSocket;
    private Queue<Socket> addresses;
    private Queue<String> messages;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        addresses = new LinkedList<>();
        messages = new LinkedList<>();
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
            addresses.add(server);
            System.out.println("pushed");

            System.out.println("Just connected to " + server.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(server.getInputStream());
            messages.add(in.readUTF());

            //System.out.println(in.readUTF());
        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void runconsumer(){
        try {
            if(!addresses.isEmpty() || !messages.isEmpty()){
                Socket server = addresses.remove();
                String message = messages.remove();
                System.out.println(addresses.isEmpty());
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
                        + "\nYES on "+ message + "\nGoodbye!");

                // Sleep 500 ms
            }
            Thread.sleep(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}