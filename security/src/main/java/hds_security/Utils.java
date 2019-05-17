package hds_security;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import pteidlib.PteidException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import javax.swing.*;

public class Utils {

	private Utils() {
	}

	/**
	 * I/O stuff
	 */

	public static void println(String str) {

		System.out.println(str);
	}

	/**
	 * This method contemplates if the program is being run in Eclipse or not, as
	 * Eclipse does not support System.console. Same for Gradle.
	 */
	public static String readString(String prompt) {

		String input = "";
		Console c = System.console();

		if (c == null) {
			input = JOptionPane.showInputDialog(prompt);
		} else {
			println(prompt);
			input = c.readLine();

		}
		return input;
	}

	/**
	 * This method contemplates if the program is being run in Eclipse or not, as
	 * Eclipse does not support System.console. Same for Gradle.
	 */
	public static String readPassword(String prompt) {

		String input = "";
		Console c = System.console();

		if (c == null) {
			input = JOptionPane.showInputDialog(prompt);
		} else {
			char[] in = System.console().readPassword(prompt);
			input = String.valueOf(in);
		}
		return input;
	}

	public static int readInt(String prompt) {
		int i;
		while (true) {
			try {
				i = Integer.parseInt(readString(prompt));
				break;
			} catch (NumberFormatException e) {
				println("Not a valid number.");
			}
		}
		return i;
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
	 * Security stuff
	 */

	public static byte[] hexToBytes(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
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

	/**
	 * Creates an hash of the message and signs the hash
	 */
	private static byte[] sign(Message msg, PrivateKey privKey)
			throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		byte[] mbytes = msg.toBytes();
		byte[] signedmessage = SignMessage.sign(HashMessage.hashBytes(mbytes), privKey);
		// Final message = message + signed hash
		byte[] finalmsg = new byte[mbytes.length + signedmessage.length];
		System.arraycopy(mbytes, 0, finalmsg, 0, mbytes.length);
		System.arraycopy(signedmessage, 0, finalmsg, mbytes.length, signedmessage.length);
		return finalmsg;
	}

	public static void write(Message msg, DataOutputStream out, PrivateKey privKey)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		byte[] finalmsg = sign(msg, privKey);
		out.writeInt(finalmsg.length);
		out.write(finalmsg, 0, finalmsg.length);
	}

	private static byte[] signWithCC(Message msg) throws ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException, IllegalAccessException, PteidException, PKCS11Exception {
		CitizenCard citizenCard = new CitizenCard();
		byte[] firstmsg = msg.toBytes();
		byte[] secondmsg = citizenCard.signMessage(HashMessage.hashBytes(firstmsg));
		// Final message = message + signed hash
		byte[] finalmsg = new byte[firstmsg.length + secondmsg.length];
		System.arraycopy(firstmsg, 0, finalmsg, 0, firstmsg.length);
		System.arraycopy(secondmsg, 0, finalmsg, firstmsg.length, secondmsg.length);
		return finalmsg;
	}

	public static void writeWithCC(Message message, DataOutputStream out)
			throws IOException, PteidException, InvocationTargetException, IllegalAccessException, PKCS11Exception,
			NoSuchMethodException, ClassNotFoundException {
		byte[] finalmsg = signWithCC(message);
		out.writeInt(finalmsg.length);
		out.write(finalmsg, 0, finalmsg.length);
	}
}
