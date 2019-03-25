import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;

public class User {

    private String name;
    private HashMap<String,Good> setofGoods;        //name and good

    public User(String name){
        this.name = name;
    }
    public static void main(String [] args) {
        String serverName = "localhost";
        int port = 6066;
        try {
            System.out.println("Connecting to server on port " + port);
            Socket client = new Socket(serverName, port);

            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            out.writeUTF("Hello from " + client.getLocalSocketAddress());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);

            System.out.println("Server says " + in.readUTF());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}