package hds_security;

import java.nio.ByteBuffer;

/**
 * Messages sent by the Notary
 */
public class Message {
	
	public static final int length = 30;

	protected char operation;
	protected int origin;
	protected int destination;
	protected long now;
	protected int gid;
	protected long nonce;
	protected int tag;

	// FIXME Throw exception if arguments are invalid
	public Message(int origin, int destination, char operation, int gid) {
		this.origin = origin;
		this.destination = destination;
		this.operation = operation;
		this.gid = gid;
		this.now = Utils.createTimeStamp();
		this.nonce = Utils.createNonce();
	}

	public Message(int origin, int destination, char operation, int gid, int tag) {
		this.origin = origin;
		this.destination = destination;
		this.operation = operation;
		this.gid = gid;
		this.now = Utils.createTimeStamp();
		this.nonce = Utils.createNonce();
		this.tag = tag;
	}

	// FIXME Throw exception if arguments are invalid
	public Message(int origin, char operation, int gid) {
		this.origin = origin;
		this.destination = -1;
		this.operation = operation;
		this.gid = gid;
		this.now = Utils.createTimeStamp();
		this.nonce = Utils.createNonce();
	}

	// FIXME Throw exception if arguments are invalid
	public Message(char operation, int gid) {
		this.origin = -1;
		this.destination = -1;
		this.operation = operation;
		this.gid = gid;
		this.now = Utils.createTimeStamp();
		this.nonce = Utils.createNonce();
	}

	// Used by 'fromBytes'
	protected Message(int origin, int destination, char operation, long now, int gid, long nonce) {
		this.origin = origin;
		this.destination = destination;
		this.operation = operation;
		this.now = now;
		this.gid = gid;
		this.nonce = nonce;
	}

	public int getTag(){
		return tag;
	}

	public char getOperation() {
		return this.operation;
	}
	
	public long getNonce() {
		return this.nonce;
	}

	/**
	 * Returns the timestamp of this message 
	 */
	public long getNow() {
		return this.now;
	}

	public int getOrigin() {
		return origin;
	}
	
	public int getDestination() {
		return destination;
	}

	public int getGoodID() {
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
		ByteBuffer bb = ByteBuffer.allocate(length);

		bb.putChar(operation);
		bb.putInt(origin);
		bb.putInt(destination);
		bb.putLong(now);
		bb.putLong(nonce);
		bb.putInt(gid);

		return bb.array();
	}

	public boolean isEqual(Message m){
		if(this.operation == m.getOperation() || this.origin == m.getOrigin() || this.destination == m.getDestination() || this.now == m.getNow() || this.gid == m.getGoodID() || this.nonce == m.getNonce()){
			return true;
		}
		return false;
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