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
		
		int uid=-1;
		int for_sale=-1;
		
		String sql = "SELECT owner_id, for_sale FROM goods WHERE gid = ?" ;

		try (PreparedStatement pstmt  = conn.prepareStatement(sql)) {

			pstmt.setInt(1, id);
			
			ResultSet rs  = pstmt.executeQuery();
			
			while (rs.next()) {
				uid = rs.getInt("owner_id");
				for_sale = rs.getInt("for_sale");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		if (uid == -1) {
			return("null");
		}
		String owner_id = Integer.toString(uid);

		String good_for_sale;
		
		good_for_sale = (for_sale == 1 ? "true" : "false");
		
		return(owner_id + " " + good_for_sale);
	}
	
	public String getListOfGoods() {
		
		String sql = "SELECT gid, owner_id, for_sale FROM goods";
		String output = "";
		
		try (Statement stmt = this.conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			// loop through the result set
			while (rs.next()) {
				output += rs.getInt("gid") + " " + rs.getInt("owner_id") +
						" " + (rs.getInt("for_sale") == 1 ? "true" : "false") + " ";
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return output;
	}
}