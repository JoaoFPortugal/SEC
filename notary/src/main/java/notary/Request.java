package notary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import hds_security.CitizenCard;
import hds_security.HashMessage;
import hds_security.Message;
import hds_security.SignMessage;
import notary.exceptions.InvalidSignatureException;
import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class Request {
	private Socket addr;
	public int origin;
	public int destin;
	public char operation;
	public long now;
	public int gid;
	private DataOutputStream out;
	private DataInputStream in;

	public Request(Socket sock) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, InvalidSignatureException {
		System.out.println("Received packet");
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


	public void writeServer(Message message) throws IOException, PteidException, InvocationTargetException, IllegalAccessException, PKCS11Exception, NoSuchMethodException, ClassNotFoundException {
		CitizenCard citizenCard = new CitizenCard();
		byte[] secondmsg = citizenCard.signMessage(message.toBytes());
		byte[] firstmsg = message.toBytes();
		byte[] finalmsg = new byte[firstmsg.length+secondmsg.length];
		System.arraycopy(firstmsg,0,finalmsg,0,firstmsg.length);
		System.arraycopy(secondmsg,0,finalmsg,firstmsg.length,secondmsg.length);

		out.writeInt(finalmsg.length);
		out.write(finalmsg,0,finalmsg.length);
	}

	public void fromBytes(byte[] mbytes) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException, InvalidSignatureException {

		ByteBuffer bb = ByteBuffer.wrap(mbytes);
		operation = bb.getChar();
		origin = bb.getInt();
		destin = bb.getInt();
		now = bb.getLong();
		gid = bb.getInt();

		byte[] unwrapmsg = new byte[mbytes.length-22];
		byte[] originalmessage = new byte[22];

		System.arraycopy(mbytes,0,originalmessage,0,22);
		System.arraycopy(mbytes,22,unwrapmsg,0,unwrapmsg.length);

		HashMessage hashedoriginal = new HashMessage();
		byte[] hashedcontent = hashedoriginal.hashBytes(originalmessage);
		SignMessage sign = new SignMessage();

		if(!sign.verify(hashedcontent,unwrapmsg,getPublicKey(origin))){
			throw new InvalidSignatureException();
		}
	}


	private PublicKey getPublicKey(int origin) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
		byte[] pub = Files.readAllBytes(Paths.get("./src/main/resources/" + origin + "_public_key.txt"));
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pub);
		KeyFactory kf = KeyFactory.getInstance("EC");

		return kf.generatePublic(keySpec);
	}


	public String getAddr() {
		return addr.getRemoteSocketAddress().toString();
	}
}
