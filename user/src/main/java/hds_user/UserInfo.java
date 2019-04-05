package hds_user;

public class UserInfo {
	private int uid;
	private String ip;
	private int port;
	
	public UserInfo(int uid, String ip, int port) {
		this.uid = uid;
		this.ip = ip;
		this.port = port;
	}
	
	public int getUid() { return uid; }
	public String getIp() { return ip; }
	public int getPort() { return port; }
}
