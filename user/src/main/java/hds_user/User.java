package hds_user;

import hds_security.HashMessage;

import java.util.HashMap;

public class User {

	private HashMap<String, Good> setofGoods; // name and good

	public User() {
		this.setofGoods = new HashMap<>();
		HashMessage hashed = new HashMessage();
	}

}