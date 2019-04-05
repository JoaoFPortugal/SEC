package hds_user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

public class User {

	private List<Good> setOfGoods;
	private List<UserInfo> setOfUsers;
	private final int uid;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public User(int id){
		this.uid = id;
		loadPubKey();
		loadPrivKey();
		setOfGoods = new ArrayList<>();
		setOfUsers = new ArrayList<>();
	}

	private void loadPubKey(){
		try{
			byte[] pub = Files.readAllBytes(Paths.get("./src/main/resources/" + uid + "_public_key.txt"));
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pub);
			KeyFactory kf = KeyFactory.getInstance("EC");

			this.publicKey = kf.generatePublic(keySpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}


	private void loadPrivKey(){
		try{
			byte[] priv = Files.readAllBytes(Paths.get("./src/main/resources/" + uid + "_private_key.txt"));
			PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(priv);
			KeyFactory kf = KeyFactory.getInstance("EC");
			this.privateKey = kf.generatePrivate(ks);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	public void setUserList(ArrayList<UserInfo> users) {
		setOfUsers = users;
	}
	
	public void setGoodList(ArrayList<Good> goods) {
		setOfGoods = goods;
	}
	
	public void printAllGoods() {
		for (Good g : setOfGoods) {
			Main.println(g.getID() + "\t" + g.getOwner() + "\t" + g.getForSale());
		}
	}
	
	public void printAllUsers() {
		for (UserInfo u : setOfUsers) {
			Main.println(u.getUid() + "\t" + u.getIp() + "\t" + u.getPort());
		}
	}
}