package notary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import hds_security.Message;
import hds_security.SecureSession;
import hds_security.exceptions.InvalidSignatureException;
import hds_security.exceptions.NullPublicKeyException;
import hds_security.exceptions.ReplayAttackException;

public class Request {

	private Socket addr;
	private DataOutputStream out;
	private DataInputStream in;

	private Message msg;

	public Request(Socket sock, SecureSession nss) throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException, InvalidKeyException, SignatureException, InvalidSignatureException,
			IllegalAccessException, ReplayAttackException, NullPublicKeyException {
		this.addr = sock;
		out = new DataOutputStream(addr.getOutputStream());
		in = new DataInputStream(addr.getInputStream());
		this.msg = nss.readFromUser(in);
	}
	
	public Message getMessage() {
		return msg;
	}

	public DataOutputStream getDataOutputStream() {
		return out;
	}

	public DataInputStream getDataInputStream() {
		return in;
	}

	public String getAddr() {
		return addr.getRemoteSocketAddress().toString();
	}
}
