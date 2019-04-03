package hds_security;

import java.nio.ByteBuffer;
import java.util.Date;

public class Message {


    private char operation;
    private int origin;
    private int destination;
    private long now;
    private int gid;



    public Message(int origin, int destination, char operation, long now, int gid){
        this.origin = origin;
        this.destination = destination;
        this.operation = operation;
        this.now = now;
        this.gid = gid;
    }


    public long createTimeStamp(){
        Date date = new Date();
        now = date.getTime();
        return now;
    }


    public int getOrigin(){
        return origin;
    }

    public int getContent(){return gid;}

    public void print(){

        System.out.println();
        System.out.println(this.operation);
        System.out.println(this.origin);
        System.out.println(this.destination);
        System.out.println(this.now);
        System.out.println(this.gid);

        System.out.println();

    }




    public byte[] toBytes(){


        ByteBuffer bb = ByteBuffer.allocate(22);

        bb.putChar(operation);
        bb.putInt(origin);
        bb.putInt(destination);
        bb.putLong(now);
        bb.putInt(gid);

        return bb.array();

    }

    public static Message fromBytes(byte[] mbytes){


        ByteBuffer bb = ByteBuffer.wrap(mbytes);

        char  moperation = bb.getChar();
        int   morigin = bb.getInt();
        int   mdestination  = bb.getInt();
        long  mnow = bb.getLong();
        int   mgid = bb.getInt();


        return new Message(morigin, mdestination, moperation, mnow, mgid);

    }


}