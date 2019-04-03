package notary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import hds_security.Message;

public class Request {
	private Socket addr;
	public int origin;
	public int destin;
	public char operation;
	public long now;
	public int gid;
	private DataOutputStream out;
	private DataInputStream in;

	public Request(Socket sock) throws IOException {
		this.addr = sock;
		out = new DataOutputStream(addr.getOutputStream());
		in = new DataInputStream(addr.getInputStream());

		fromBytes(read());

	}

	private byte[] read() throws IOException {
		int msgLen = in.readInt();
		byte[] msg = new byte[msgLen];
		in.readFully(msg);
		return msg;
	}

	public void write(Message message) throws IOException {
		byte[] msg = message.toBytes();
		out.writeInt(msg.length);
		out.write(msg, 0, msg.length);
	}

	public void fromBytes(byte[] mbytes) {

		ByteBuffer bb = ByteBuffer.wrap(mbytes);

		operation = bb.getChar();
		origin = bb.getInt();
		destin = bb.getInt();
		now = bb.getLong();
		gid = bb.getInt();

	}

	public String getAddr() {
		return addr.getRemoteSocketAddress().toString();
	}
}
