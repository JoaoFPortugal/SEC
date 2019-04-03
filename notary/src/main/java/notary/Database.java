package notary;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class Database {

	private Connection conn;
	private String url;

	Database(String url) throws IOException {
		this.url = url;
		this.conn = null;
		
		/**
		 * Check if database file exists because the connect method creates it if it doesn't 
		 */
		File f = new File(this.url);
		if(!(f.exists() && !f.isDirectory())) {
			throw new RuntimeException("Database file not found.");
		}
		
		connect();
	}


	/**
	 * Connect to a database
	 * http://www.sqlitetutorial.net/sqlite-java/sqlite-jdbc-driver/
	 */
	private void connect() {
		String url = "jdbc:sqlite:" + this.url;
		try {
			this.conn = DriverManager.getConnection(url);
			System.out.println("Connection to SQLite has been established.");

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Select all users
	 * http://www.sqlitetutorial.net/sqlite-java/select/
	 * 
	 * Select with parameters: http://www.sqlitetutorial.net/sqlite-java/select/
	 */
	public void selectAllUsers() {
		String sql = "SELECT uid FROM users";

		// try-with-resources
		// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
		try (Statement stmt = this.conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			while (rs.next()) {
				System.out.println(rs.getInt("uid"));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void selectAllGoods() {
		String sql = "SELECT gid, owner_id, for_sale FROM goods";

		try (Statement stmt = this.conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			while (rs.next()) {
				System.out.println(rs.getInt("gid") + "\t" + rs.getInt("owner_id") +
						"\t" + rs.getInt("for_sale"));
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}


	public String getStateOfGood(int id){

		String sql = "SELECT owner_id, for_sale FROM goods WHERE gid = ?" ;

		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {

			pstmt.setInt(1, id);

			ResultSet rs  = pstmt.executeQuery();

			while (rs.next()) {
				int uid = rs.getInt("owner_id");
				int for_sale = rs.getInt("for_sale");
				return(uid + " " + (for_sale == 1 ? "true" : "false"));
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return("null");
	}

	public int checkIntentionToSell(int uid, int gid){

		String sql = "UPDATE goods SET for_sale = ? WHERE gid = ? AND owner_id = ?" ;

		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {

			pstmt.setInt(1, 1);
			pstmt.setInt(2, gid);
			pstmt.setInt(3, uid);

			pstmt.executeQuery();


		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		int result=-1;
		String sql2 = "SELECT for_sale FROM goods WHERE gid = ? AND owner_id = ?" ;
		try (PreparedStatement pstmt  = conn.prepareStatement(sql2)) {

			pstmt.setInt(1, gid);
			pstmt.setInt(2, uid);

			ResultSet rs  = pstmt.executeQuery();

			result= rs.getInt(1);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return(0);

		}

		return(result);

	}
	
	public String getListOfGoods() {
		
		String sql = "SELECT gid, owner_id, for_sale FROM goods";
		String output = "";
		
		try (Statement stmt = this.conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				output += rs.getInt("gid") + " " + rs.getInt("owner_id") +
						" " + (rs.getInt("for_sale") == 1 ? "true" : "false") + " ";
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return (output.isEmpty() ? "null" : output);
	}
}