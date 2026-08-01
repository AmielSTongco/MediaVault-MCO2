package application.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import application.db.DatabaseInitializer;

public class UserDAO {
	
	private Connection conn;

    public UserDAO(Connection conn) {
        this.conn = conn;
    }

	/**
	 * Verifies a user's login credentials.
	 *
	 * @param username the username to check
	 * @param password the password to check
	 * @return {@code true} if a user with the given username and password exists;
	 *         {@code false} otherwise
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code username} and {@code password} are non-null
	 * @post no data is modified
	 */
    public boolean login(String username, String password) throws SQLException {

        String sql = """
            SELECT 1
            FROM users
            WHERE username = ? AND password = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
	/**
	 * Retrieves the ID of the user with the given username.
	 *
	 * @param username the username to look up
	 * @return the user's ID if found; {@code -1} if no such user exists
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code username} is non-null
	 * @post no data is modified
	 */
    public int getUserID(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        return -1;
    }
    
	/**
	 * Checks whether a user with the given username exists.
	 *
	 * @param username the username to check
	 * @return {@code true} if the username exists; {@code false} otherwise
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code username} is non-null
	 * @post no data is modified
	 */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

	/**
	 * Creates a new user with the given username and password, then registers
	 * default entries for the new user.
	 *
	 * @param username the username for the new user
	 * @param password the password for the new user
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code username} and {@code password} are non-null
	 * @post if {@code username} is not already taken, a new row is inserted into
	 *       the users table and {@link DatabaseInitializer#registerUser} is
	 *       called with the new user's ID; if {@code username} is already taken,
	 *       no user is created and no registration occurs
	 */
    public void addUser(String username, String password) throws SQLException {
        int userId = -1;
    	String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, password);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
		        if (keys.next()) {
		        	userId = keys.getInt(1);
		        }
		    }
            
        } catch (SQLException e) {
			if (e.getMessage().contains("UNIQUE constraint failed")) {
		        System.out.println("Username '" + username + "' is already taken.");
		    } else {
		        System.out.println(e.getMessage());
		    }
		}
        
        // add "all" entries category if user is added
     	if (userId != -1) {
     		DatabaseInitializer.registerUser(conn, userId);
     	}
    }
    
    public boolean verifyPassword(int userId, String password) throws SQLException {
    	String sql = "SELECT 1 FROM users WHERE id = ? AND password = ?";

    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
    		ps.setInt(1, userId);
    		ps.setString(2, password);

    		try(ResultSet rs = ps.executeQuery()) {
    			return rs.next();
    		}
    	}
    }

    public void updateUsername(int userId, String username) throws SQLException {
    	String sql = "UPDATE users SET username = ? WHERE id = ?";

    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
    		ps.setString(1, username);
    		ps.setInt(2, userId);
    		ps.executeUpdate();
    	}
    }

    public void updatePassword(int userId, String password) throws SQLException {
    	String sql = "UPDATE users SET password = ? WHERE id = ?";

    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
    		ps.setString(1, password);
    		ps.setInt(2, userId);
    		ps.executeUpdate();
    	}
    }

    public void updateProfilePicture(int userId, String path) throws SQLException {
    	String sql = "UPDATE users SET profile_picture = ? WHERE id = ?";

    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
    		ps.setString(1, path);
    		ps.setInt(2, userId);
    		ps.executeUpdate();
    	}
    }

    public String getProfilePicture(int userId) throws SQLException {
    	String sql = "SELECT profile_picture FROM users WHERE id = ?";

    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
    		ps.setInt(1, userId);

    		try(ResultSet rs = ps.executeQuery()) {
    			if(rs.next())
    				return rs.getString("profile_picture");
    		}
    	}

    	return null;
    }

    public void deleteUser(int userId) throws SQLException {
	    	String sql = "DELETE FROM songs_playlists WHERE user_id = ?";
	    	
	    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
	    		ps.setInt(1, userId);
	    		ps.executeUpdate();
	    	}
	    	
	    	sql = "DELETE FROM games_playlists WHERE user_id = ?";
	    	
	    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
	    		ps.setInt(1, userId);
	    		ps.executeUpdate();
	    	}
	    	
	    	sql = "DELETE FROM shows_playlists WHERE id = ?";
	    	
	    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
	    		ps.setInt(1, userId);
	    		ps.executeUpdate();
	    	}
	    	
	    	sql = "DELETE FROM users WHERE id = ?";
	
	    	try(PreparedStatement ps = conn.prepareStatement(sql)) {
	    		ps.setInt(1, userId);
	    		ps.executeUpdate();
	    	}
    }
}