package notary;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class Database {

	private Connection conn;
	private String url;

	Database(String url) {
		this.url = url;
		this.conn = null;

		/**
		 * Check if database file exists because the connect method creates it if it
		 * doesn't
		 */
		File f = new File(this.url);
		if (!(f.exists() && !f.isDirectory())) {
			throw new RuntimeException("Database file not found.");
		}

		connect();
		
		/**
		 * Use batch statements for transactions
		 * https://stackoverflow.com/questions/9601030/transaction-in-java-sqlite3
		 */
		try {
			if(this.conn.getMetaData().supportsBatchUpdates()) {
				System.out.println("SQL Driver supports batch updates.");
			} else {
				System.err.println("SQL Driver does not support batch updates.");
				System.exit(0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("SQL Driver does not support batch updates.");
			System.exit(0);
		}
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
	 * Select all users http://www.sqlitetutorial.net/sqlite-java/select/
	 * 
	 * Select with parameters: http://www.sqlitetutorial.net/sqlite-java/select/
	 */

	public HashMap<String, Integer> getStateOfGood(int id) {

		String sql = "SELECT owner_id, for_sale FROM goods WHERE gid = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, id);

			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				HashMap<String, Integer> map = new HashMap<String, Integer>();
				map.put("owner_id", rs.getInt("owner_id"));
				map.put("for_sale", rs.getInt("for_sale"));
				//logQuery();
				return map;
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return null;
	}

	public int intentionToSell(int uid, int gid) {

		String sql = "UPDATE goods SET for_sale = 1 WHERE gid = ? AND owner_id = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, gid);
			pstmt.setInt(2, uid);
			
			pstmt.execute();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		int result;
		String sql2 = "SELECT for_sale FROM goods WHERE gid = ? AND owner_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {

			pstmt.setInt(1, gid);
			pstmt.setInt(2, uid);
			ResultSet rs = pstmt.executeQuery();
			result = rs.getInt(1);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return 0;
		}
		return result;

	}

	public boolean transferGood(int gid, int owner, int buyer) {		
		// Check if good belongs to the 'owner'
		String sql = "SELECT owner_id, for_sale FROM goods WHERE gid = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, gid);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				int uid = rs.getInt("owner_id");
				int for_sale = rs.getInt("for_sale");
				if (uid != owner || for_sale != 1) {
					return false;
				}
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}

		// Change good's 'owner' and 'for_sale'.
		String sql2 = "UPDATE goods SET owner_id = ?, for_sale = 0 WHERE gid = ? AND owner_id = ? AND for_sale = 1";

		try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {

			pstmt.setInt(1, buyer);
			pstmt.setInt(2, gid);
			pstmt.setInt(3, owner);

			pstmt.execute();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	public void logQuery(int uid, String query, String result, long timestamp, String error, String attack) {
		String sql2 = "INSERT INTO log(uid, query, result, timestamp, error, attack) VALUES(?,?,?,?,?,?)";

		try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {

			pstmt.setInt(1, uid);
			pstmt.setString(2, query);
			pstmt.setString(3, result);
			pstmt.setLong(4, timestamp);
			pstmt.setString(5, error);
			pstmt.setString(6, attack);

			pstmt.execute();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	// Not being used
	public String getListOfUsers() {

		String sql = "SELECT uid FROM users";
		String output = "";

		try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				output += rs.getInt("uid") + "\n";
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return (output.isEmpty() ? "null" : output);
	}

	// Not being used
	public String getListOfGoods() {

		String sql = "SELECT gid, owner_id, for_sale FROM goods";
		String output = "";

		try (Statement stmt = this.conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				output += rs.getInt("gid") + " " + rs.getInt("owner_id") + " "
						+ (rs.getInt("for_sale") == 1 ? "true" : "false") + " ";
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return (output.isEmpty() ? "null" : output);
	}
}