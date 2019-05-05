package hds_security;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class Utility {
	private Utility() {}
	
	public static byte[] hexToBytes(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
	
	public static int readIntFromFile(String filename) {
		int i = -1;
		try (FileInputStream fis = new FileInputStream(filename); Scanner scanner = new Scanner(fis);) {
			i = scanner.nextInt();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not read from file '" + filename + "'.");
			System.exit(0);
		}
		return i;
	}
	
	/**
	 * Returns the number of milliseconds since January 1, 1970.
	 */
	public static long createTimeStamp() {
		return new Date().getTime();
	}
	
	/**
	 * Returns a random long. 
	 */
	public static long createNonce() {
		return new Random().nextLong();
	}
}
