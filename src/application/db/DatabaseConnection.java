package application.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	
	private static String url = "jdbc:sqlite:data/user.db";
	private static Connection conn;
    
	public static Connection connect() throws SQLException {
		new File("data").mkdirs();

		if (conn == null || conn.isClosed()) {
			conn = DriverManager.getConnection(url);
			System.out.println("Connected to database!");
		}
		
		return conn;
	}
	
	public static void close() throws SQLException {
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
	}
}