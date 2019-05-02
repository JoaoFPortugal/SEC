package hds_security;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Random;

public class Message {

	private char operation;
	private int origin;
	private int destination;
	private long now;
	private int gid;
	private long nonce;

	Random rand = new Random();

	public Message(int origin, int destination, char operation, long now, int gid, long nonce) {
		this.origin = origin;
		this.destination = destination;
		this.operation = operation;
		this.now = now;
		this.gid = gid;
		this.nonce = nonce;
	}

	public Message(int origin, int destination, char operation, int gid) {
		this.origin = origin;
		this.destination = destination;
		this.operation = operation;
		this.gid = gid;
		this.now = createTimeStamp();
		this.nonce = createNonce();
	}

	public long createTimeStamp() {
		Date date = new Date();
		now = date.getTime();
		return now;
	}

	public long createNonce() {
		return rand.nextLong();
	}

	public long getNonce() {
		return this.nonce;
	}

	public long getNow() {
		return this.now;
	}

	public int getOrigin() {
		return origin;
	}

	public int getContent() {
		return gid;
	}

	public void print() {
		System.out.println();
		System.out.println(this.operation);
		System.out.println(this.origin);
		System.out.println(this.destination);
		System.out.println(this.now);
		System.out.println(this.gid);

		System.out.println();
	}

	public byte[] toBytes() {

		ByteBuffer bb = ByteBuffer.allocate(30);

		bb.putChar(operation);
		bb.putInt(origin);
		bb.putInt(destination);
		bb.putLong(now);
		bb.putLong(nonce);
		bb.putInt(gid);

		return bb.array();

	}

	public static Message fromBytes(byte[] mbytes) {

		ByteBuffer bb = ByteBuffer.wrap(mbytes);

		char moperation = bb.getChar();
		int morigin = bb.getInt();
		int mdestination = bb.getInt();
		long mnow = bb.getLong();
		long mnonce = bb.getLong();
		int mgid = bb.getInt();

		return new Message(morigin, mdestination, moperation, mnow, mgid, mnonce);

	}

}