package notary;

import java.net.*;
import java.io.*;
import java.util.Stack;

public class Server extends Thread {
    private ServerSocket serverSocket;
    Stack<Socket> addresses;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        addresses = new Stack<Socket>();
    }

    @Override
    public void run() {
        while(true){
            if(Thread.currentThread().getName().equals("thread1")){
                runreceiver();
            }
            else{
                runsender();
            }
        }
    }

    public void runreceiver() {
        try {
            System.out.println("Waiting for client on port " +
                    serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            addresses.push(server);
            System.out.println("pushed");

            System.out.println("Just connected to " + server.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(server.getInputStream());

            System.out.println(in.readUTF());
        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void runsender(){
        try {
            if(!addresses.empty()){
                Socket server = addresses.pop();
                System.out.println(addresses.empty());
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
                        + "\nGoodbye!");

                // Sleep 500 ms
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}