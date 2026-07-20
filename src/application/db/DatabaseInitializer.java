package application.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
	
	public void initialize(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement()){
			stmt.execute("PRAGMA foreign_keys = ON");
			
			// create users table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS users (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				username TEXT UNIQUE,
				password TEXT NOT NULL
			)""");
			
			// create games table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS games (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				title TEXT NOT NULL UNIQUE,
				creator TEXT,
				year INTEGER,
				genre TEXT,
				avg_playtime_mins INTEGER
			)""");
			
			// create songs table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS songs (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				title TEXT NOT NULL,
				album TEXT,
				creator TEXT,
				year INTEGER,
				runtime_seconds INTEGER,
				UNIQUE(title, creator)
			)""");
			
			// create shows table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS shows (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				title TEXT NOT NULL UNIQUE,
				genre TEXT,
				creator TEXT,
				year_start INTEGER,
				year_end INTEGER,
				num_of_seasons INTEGER,
				num_of_episodes INTEGER,
				avg_mins_per_ep INTEGER,
				airing BOOLEAN
			)""");
			
			// create medias table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS medias (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					type TEXT,
					title TEXT NOT NULL UNIQUE,
					creator TEXT,
					year TEXT,
					info TEXT,
					UNIQUE(title, creator)
			)""");
			
			// create seasons table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS seasons (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				show_id INTEGER NOT NULL,
				title TEXT,
				FOREIGN KEY (show_id) REFERENCES shows(id)
			)""");
			
			// create episodes table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS episodes (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				season_id INTEGER NOT NULL,
				episode_number INTEGER NOT NULL,
				title TEXT,
				FOREIGN KEY (season_id) REFERENCES seasons(id)
			)""");
			
			// create games_playlists table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS games_playlists (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				title TEXT NOT NULL,
				UNIQUE (user_id, title),
				
				FOREIGN KEY (user_id) REFERENCES users(id)
			)""");
			
			// create songs_playlists table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS songs_playlists (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				title TEXT NOT NULL,
				user_rating REAL,
				UNIQUE (user_id, title),
				
				UNIQUE(user_id, title),
				FOREIGN KEY (user_id) REFERENCES users(id)
			)""");
			
			// create shows_playlists table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS shows_playlists (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				title TEXT NOT NULL,
				UNIQUE (user_id, title),
				
				FOREIGN KEY (user_id) REFERENCES users(id)
			)""");
			
			// create medias_playlists table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS medias_playlists (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				title TEXT NOT NULL,
				UNIQUE (user_id, title)
			)""");
			
			// create games_playlist_items table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS games_playlist_items (
				playlist_id INTEGER NOT NULL,
			    game_id INTEGER NOT NULL,
			    
			    PRIMARY KEY (playlist_id, game_id),
				FOREIGN KEY (playlist_id) REFERENCES games_playlists(id),
				FOREIGN KEY (game_id) REFERENCES games(id)
			)""");
			
			// create songs_playlist_items table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS songs_playlist_items (
				playlist_id INTEGER NOT NULL,
			    song_id INTEGER NOT NULL,
			    status TEXT,
			    user_rating REAL,
			    review TEXT,
				
				UNIQUE(playlist_id, song_id),
				
				FOREIGN KEY (playlist_id) REFERENCES songs_playlists(id),
				FOREIGN KEY (song_id) REFERENCES songs(id)
 			)""");
			
			// create shows_playlist_items table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS shows_playlist_items (
				playlist_id INTEGER NOT NULL,
				show_id INTEGER NOT NULL,
				
				PRIMARY KEY (playlist_id, show_id),
				FOREIGN KEY (playlist_id) REFERENCES shows_playlists(id),
				FOREIGN KEY (show_id) REFERENCES shows(id)
			)""");
			
			// create medias_playlist_items table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS shows_playlist_items (
				type TEXT,
				playlist_id INTEGER NOT NULL,
			    media_id INTEGER NOT NULL,
			    status TEXT,
			    user_rating REAL,
			    review TEXT,
				
				UNIQUE(playlist_id, song_id),
				
				PRIMARY KEY (playlist_id, show_id),
				FOREIGN KEY (playlist_id) REFERENCES shows_playlists(id),
				FOREIGN KEY (show_id) REFERENCES shows(id)
			)""");
			
			// create genres table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS genres (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				genre TEXT NOT NULL UNIQUE
			)""");
			
			// create game_genres table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS game_genres (
				game_id INTEGER NOT NULL,
				genre_id INTEGER NOT NULL,
				
				PRIMARY KEY (game_id, genre_id),
				FOREIGN KEY (game_id) REFERENCES games(id),
				FOREIGN KEY (genre_id) REFERENCES genres(id)
			)""");
			
			// create songs_genres table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS songs_genres (
				song_id INTEGER NOT NULL,
				genre_id INTEGER NOT NULL,
				
				PRIMARY KEY (song_id, genre_id),
				FOREIGN KEY (song_id) REFERENCES songs(id),
				FOREIGN KEY (genre_id) REFERENCES genres(id)
			)""");
			
			// create shows_genres table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS shows_genres (
				show_id INTEGER NOT NULL,
				genre_id INTEGER NOT NULL,
				
				PRIMARY KEY (show_id, genre_id),
				FOREIGN KEY (show_id) REFERENCES shows(id),
				FOREIGN KEY (genre_id) REFERENCES genres(id)
			)""");

			// create games_reviews table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS games_reviews (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				game_id INTEGER NOT NULL,
				status TEXT,
				user_rating REAL,
				review TEXT,
				UNIQUE (user_id, game_id),
				FOREIGN KEY (user_id) REFERENCES users(id),
				FOREIGN KEY (game_id) REFERENCES games(id)
			)""");
			
			// create songs_reviews table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS songs_reviews (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				song_id INTEGER NOT NULL,
				status TEXT,
				user_rating REAL,
				review TEXT,
				
				UNIQUE (user_id, song_id),
				FOREIGN KEY (user_id) REFERENCES users(id),
				FOREIGN KEY (song_id) REFERENCES songs(id)
			)""");
			
			// create shows_reviews table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS shows_reviews (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				show_id INTEGER NOT NULL,
				status TEXT,
				user_rating REAL,
				review TEXT,
				UNIQUE (user_id, show_id),
				FOREIGN KEY (user_id) REFERENCES users(id),
				FOREIGN KEY (show_id) REFERENCES shows(id)
			)""");
			
			// create seasons_reviews table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS seasons_reviews (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				season_id INTEGER NOT NULL,
				status TEXT,
				UNIQUE (user_id, season_id),
				FOREIGN KEY (user_id) REFERENCES users(id),
				FOREIGN KEY (season_id) REFERENCES seasons(id)
			)""");

			// create episodes_reviews table
			stmt.execute("""
				CREATE TABLE IF NOT EXISTS episodes_reviews (
				id INTEGER PRIMARY KEY AUTOINCREMENT,
				user_id INTEGER NOT NULL,
				episode_id INTEGER NOT NULL,
				status TEXT,
				user_rating REAL,

				UNIQUE (user_id, episode_id),
				FOREIGN KEY (user_id) REFERENCES users(id),
				FOREIGN KEY (episode_id) REFERENCES episode(id)
			)""");
			
			System.out.println("Tables initialized.");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
	
	public static int registerUser(Connection conn, int userId) throws SQLException {
		// add "all" entries category if user is added
		String sql = "INSERT OR IGNORE INTO games_playlists (user_id, title) VALUES (?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, "all_games");
			pstmt.executeUpdate();
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
		
		sql = "INSERT OR IGNORE INTO songs_playlists (user_id, title) VALUES (?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, "all_songs");
			pstmt.executeUpdate();
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
		
		sql = "INSERT OR IGNORE INTO shows_playlists (user_id, title) VALUES (?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, "all_shows");
			pstmt.executeUpdate();
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
		
		return userId;
	}
}
