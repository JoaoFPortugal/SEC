package hds_user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

public class User {

	private HashMap<String, Good> setofGoods; // name and good
	private final String name;
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public User(String name) {
		this.name = name;
		//loadPubKey();
		//loadPrivKey();
		this.setofGoods = new HashMap<>();
	}

	private void loadPubKey(){
		try{

			FileInputStream fis = new FileInputStream("resources/" + name + "public_key.txt");
			byte[] pub = fis.readAllBytes();
			fis.close();
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pub);
			KeyFactory kf = KeyFactory.getInstance("EC");

			this.publicKey = kf.generatePublic(keySpec);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}


	private void loadPrivKey(){
		try{
			FileInputStream fis = new FileInputStream("resources/" + name + "private_key.txt");
			byte[] priv = fis.readAllBytes();
			fis.close();
			PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(priv);
			KeyFactory kf = KeyFactory.getInstance("EC");
			this.privateKey = kf.generatePrivate(ks);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}