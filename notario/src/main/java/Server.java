
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

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String [] args) {
        int port = 6066;
        try {
            Server server = new Server(port);
            Thread t1 = new Thread(server,"thread1");
            Thread t2 = new Thread(server,"thread2");
            t1.start();
            t2.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}