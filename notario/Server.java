
import java.net.*;
import java.io.*;

public class Server extends Thread {
    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);

    }

    @Override
    public void run() {
        while(true){
            if(Thread.currentThread().getName().equals("thread1")){
                runreceiver();
            }
            else{
               // runsender();
            }
        }
    }

    public void runreceiver() {
        while (true) {

            try {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();

                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());

                System.out.println(in.readUTF());
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
                        + "\nGoodbye!");
                server.close();

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
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