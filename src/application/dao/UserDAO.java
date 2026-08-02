package application.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import application.db.DatabaseInitializer;

public class UserDAO {
	
	/*
	 * Handles database operations involving user accounts,
	 * credentials, profile pictures, and account deletion
	 */
	
	private Connection conn;

	/**
	 * Creates a UserDAO using the given database connection.
	 *
	 * @param conn active database connection
	 */
	public UserDAO(Connection conn) {
		this.conn = conn;
	}

	/**
	 * Verifies entered login credentials.
	 *
	 * @param username username to verify
	 * @param password password to verify
	 * @return true if matching account exists, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean login(String username, String password) throws SQLException {
		String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			stmt.setString(2, password);

			try(ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Retrieves user ID using a username.
	 *
	 * @param username username to search for
	 * @return matching user ID, or -1 if none exists
	 * @throws SQLException if a database error occurs
	 */
	public int getUserID(String username) throws SQLException {
		String sql = "SELECT id FROM users WHERE username = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);

			try(ResultSet rs = stmt.executeQuery()) {
				if(rs.next())
					return rs.getInt("id");
			}
		}

		return -1;
	}

	/**
	 * Checks whether a username is already registered.
	 *
	 * @param username username to check
	 * @return true if username exists, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean usernameExists(String username) throws SQLException {
		String sql = "SELECT 1 FROM users WHERE username = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);

			try(ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Creates a new user and registers default playlists.
	 *
	 * @param username new username
	 * @param password new password
	 * @throws SQLException if a database error occurs
	 */
	public void addUser(String username, String password) throws SQLException {
		int userId = -1;
		String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

		try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, username);
			stmt.setString(2, password);
			stmt.executeUpdate();

			// Retrieves generated user ID
			try(ResultSet keys = stmt.getGeneratedKeys()) {
				if(keys.next())
					userId = keys.getInt(1);
			}
		}
		catch(SQLException e) {
			if(e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed"))
				System.out.println("Username '" + username + "' is already taken.");
			else
				throw e;
		}

		// Creates default playlists for new user
		if(userId != -1)
			DatabaseInitializer.registerUser(conn, userId);
	}

	/**
	 * Verifies current password of a user.
	 *
	 * @param userId user ID
	 * @param password password to verify
	 * @return true if password matches, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean verifyPassword(int userId, String password) throws SQLException {
		String sql = "SELECT 1 FROM users WHERE id = ? AND password = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setString(2, password);

			try(ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	/**
	 * Updates username of a user.
	 *
	 * @param userId user ID
	 * @param username new username
	 * @throws SQLException if a database error occurs
	 */
	public void updateUsername(int userId, String username) throws SQLException {
		String sql = "UPDATE users SET username = ? WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, username);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}
	}

	/**
	 * Updates password of a user.
	 *
	 * @param userId user ID
	 * @param password new password
	 * @throws SQLException if a database error occurs
	 */
	public void updatePassword(int userId, String password) throws SQLException {
		String sql = "UPDATE users SET password = ? WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, password);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}
	}

	/**
	 * Updates profile picture path of a user.
	 *
	 * @param userId user ID
	 * @param path new profile picture path
	 * @throws SQLException if a database error occurs
	 */
	public void updateProfilePicture(int userId, String path) throws SQLException {
		String sql = "UPDATE users SET profile_picture = ? WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, path);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}
	}

	/**
	 * Retrieves saved profile picture path of a user.
	 *
	 * @param userId user ID
	 * @return saved profile picture path, or null if none exists
	 * @throws SQLException if a database error occurs
	 */
	public String getProfilePicture(int userId) throws SQLException {
		String sql = "SELECT profile_picture FROM users WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);

			try(ResultSet rs = stmt.executeQuery()) {
				if(rs.next())
					return rs.getString("profile_picture");
			}
		}

		return null;
	}

	/**
	 * Deletes a user and all user-owned playlists.
	 *
	 * @param userId user ID
	 * @throws SQLException if a database error occurs
	 */
	public void deleteUser(int userId) throws SQLException {
		boolean previousAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		try {
			// Deletes song playlists
			String sql = "DELETE FROM songs_playlists WHERE user_id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				stmt.executeUpdate();
			}

			// Deletes game playlists
			sql = "DELETE FROM games_playlists WHERE user_id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				stmt.executeUpdate();
			}

			// Deletes show playlists
			sql = "DELETE FROM shows_playlists WHERE user_id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				stmt.executeUpdate();
			}

			// Deletes user account
			sql = "DELETE FROM users WHERE id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, userId);
				stmt.executeUpdate();
			}

			conn.commit();
		}
		catch(SQLException e) {
			conn.rollback();
			throw e;
		}
		finally {
			conn.setAutoCommit(previousAutoCommit);
		}
	}
}