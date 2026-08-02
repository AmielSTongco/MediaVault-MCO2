package application.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	
	/*
	 * Manages creation and reuse of the application's
	 * SQLite database connection
	 */

	private static String url = "jdbc:sqlite:data/user.db";
	private static Connection conn;
    
	/**
	 * Opens the database connection if one is not already active.
	 *
	 * @return active database connection
	 * @throws SQLException if the database cannot be opened
	 */
	public static Connection connect() throws SQLException {
		// Creates the database directory if it does not exist
		new File("data").mkdirs();

		// Opens a new connection only when necessary
		if(conn == null || conn.isClosed()) {
			conn = DriverManager.getConnection(url);
			System.out.println("Connected to database!");
		}
		
		return conn;
	}
	
	/**
	 * Closes the active database connection.
	 *
	 * @throws SQLException if the connection cannot be closed
	 */
	public static void close() throws SQLException {
		if(conn != null && !conn.isClosed())
			conn.close();
	}
}