package notary;
import java.net.*;
import java.io.*;

public class Request {
    private Socket client;

    public Request(Socket client){
        this.client = client;
    }


    public String getMessageFromClient() {
        DataInputStream in = null;
        String message= null;

        try {

            in = new DataInputStream(client.getInputStream());
            message = in.readUTF();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }



    public DataOutputStream getOutputFromClient(){
        DataOutputStream out = null;

        try {

            out = new DataOutputStream(client.getOutputStream());

        } catch (IOException e){
            e.printStackTrace();
        }

            return out;
    }
}
