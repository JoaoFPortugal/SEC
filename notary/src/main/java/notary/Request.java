package notary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Request {
    private Socket addr;
    private String msg;
    private DataOutputStream out;
	private DataInputStream in;
    
    public Request(Socket sock) throws IOException {
    	this.addr = sock;
    	out = new DataOutputStream(addr.getOutputStream());
		in = new DataInputStream(addr.getInputStream());
		this.msg = in.readUTF();
    }
    
    public String read() throws IOException {
		return in.readUTF();
	}
	
	public void write(String msg) throws IOException {
		out.writeUTF(msg);
	}

	public String getMessage() {
		return msg;
	}
	
	public String getAddr() {
		return addr.getRemoteSocketAddress().toString();
	}
}
