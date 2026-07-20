package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.model.Type;
import application.model.Media;
import application.model.MediaPlaylist;
import application.model.Song;
import application.model.Game;
import application.model.Show;
import application.model.Status;

public class MediaPlaylistDAOImpl {
	
	private Connection conn;
	private int userId;
	
	public MediaPlaylistDAOImpl(Connection conn, int userId) {
		this.conn = conn;
		this.userId = userId;
	}
	
	/**
	 * Creates a new playlist with the given name for the current user.
	 * The playlist name is validated against reserved names (e.g. "all_songs",
	 * "all songs") before insertion. If the name is reserved or already exists
	 * for this user, the playlist is not created.
	 *
	 * @param name the desired name for the new playlist
	 * @param mediaType the type of media the playlist is for (e.g. "Song", "Game",
	 *                  "Show"); used to determine which playlists table to insert into
	 * @return {@code true} if the playlist was successfully created; otherwise {@code false}
	 *         if {@code name} is a reserved playlist name or a playlist with that
	 *         name already exists for this user
	 * @throws SQLException if a database access error occurs while preparing or
	 *                      executing the statement
	 * @pre  {@code name} and {@code mediaType} are non-null; {@code userId} refers
	 *       to a valid, existing user
	 * @post if {@code name} is not reserved and does not already exist for this
	 *       user, a new row is inserted into the corresponding playlists table
	 *       with {@code userId} and {@code name}; otherwise, no row is inserted
	 */
	public boolean createPlaylist(String name, String mediaType) throws SQLException {

	    String normalizedName = name.trim().toLowerCase();
	    String normalizedType = mediaType.trim().toLowerCase();

	    if (normalizedName.isEmpty()) {
	        System.out.println(" - Please input a title!");
	        return false;
	    }

	    String tableName;
	    String reservedName;

	    switch (normalizedType) {
	        case "song":
	            tableName = "songs_playlists";
	            reservedName = "all_songs";
	            break;

	        case "game":
	            tableName = "games_playlists";
	            reservedName = "all_games";
	            break;

	        case "show":
	            tableName = "shows_playlists";
	            reservedName = "all_shows";
	            break;

	        default:
	            System.out.println(" - Invalid media type: " + mediaType);
	            return false;
	    }

	    String normalizedReservedName = reservedName.replace("_", " ");

	    if (normalizedName.equals(reservedName)
	            || normalizedName.equals(normalizedReservedName)) {

	        System.out.println(" - \"" + name + "\" is a reserved playlist name.");
	        return false;
	    }

	    String sql = "INSERT INTO " + tableName
	               + " (user_id, title) VALUES (?, ?)";

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, userId);
	        stmt.setString(2, name.trim());

	        stmt.executeUpdate();
	        return true;

	    } catch (SQLException e) {

	        if (e.getMessage() != null
	                && e.getMessage().contains("UNIQUE constraint failed")) {

	            System.out.println(" -");
	            System.out.println(" - Playlist \"" + name + "\" already exists!");
	            return false;
	        }

	        System.out.println(" - Failed to create " + mediaType + " playlist.");
	        System.out.println(" - SQL error: " + e.getMessage());

	        throw e;
	    }
	}
	
	/**
	 * Adds a media item to the specified playlist.
	 * Inserts a new entry into the appropriate playlist-items table based on
	 * {@code mediaType} (e.g. {@code songs_playlist_items}, {@code games_playlist_items}).
	 * If the media item is already in the playlist, the insert is silently ignored
	 * due to the {@code INSERT OR IGNORE} keyword.
	 *
	 * @param playlistId the ID of the playlist to add the media to
	 * @param media	the ID of the media item to add
	 * @param status the watch/play/listen status associated with this entry
	 * @param rating the user's rating for this media item
	 * @param review the user's review text for this media item
	 * @param mediaType the type of media (e.g. "Song", "Game", "Show"); used to
	 *                  determine which playlist-items table to insert into
	 * @post if the media item was not already in the playlist, a new row is added
	 *       to the corresponding playlist-items table linking {@code playlistId}
	 *       and {@code mediaId}; otherwise, the playlist-items table remains unchanged
	 * @throws SQLException if a database access error occurs while preparing or
	 *                       executing the statement
	 */
	public void addMediaToPlaylist(int playlistId, int mediaId, Status status, double rating, String review, String mediaType) throws SQLException {

        String sql = "INSERT OR IGNORE INTO " + mediaType.toLowerCase() + "s_playlist_items (playlist_id, " + mediaType.toLowerCase() + "_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, playlistId);
            stmt.setInt(2, mediaId);
            stmt.executeUpdate();

        } catch (SQLException e) {
                System.out.println(e.getMessage());
        }
    }
	
	/**
	 * Removes a media item from the specified playlist.
	 *
	 * @param playlistId the ID of the playlist to remove the media from
	 * @param mediaId    the ID of the media item to remove
	 * @param mediaType  the type of media (e.g. "Song", "Game", "Show"); used to
	 *                   determine which playlist-items table to delete from
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} refers to an existing playlist
	 * @post if a matching row existed, it is removed from the corresponding
	 *       playlist-items table; otherwise, the table is unchanged
	 */
	public void removeMediaFromPlaylist(int playlistId, int mediaId, Type type) throws SQLException {

	    String tableName;
	    String mediaIdColumn;
	    String mediaName;

	    switch (type) {
	        case SONG:
	            tableName = "songs_playlist_items";
	            mediaIdColumn = "song_id";
	            mediaName = "Song";
	            break;

	        case GAME:
	            tableName = "games_playlist_items";
	            mediaIdColumn = "game_id";
	            mediaName = "Game";
	            break;

	        case SHOW:
	            tableName = "shows_playlist_items";
	            mediaIdColumn = "show_id";
	            mediaName = "Show";
	            break;

	        default:
	            throw new IllegalArgumentException("Unsupported media type: " + type);
	    }

	    String sql = "DELETE FROM " + tableName +
	                 " WHERE playlist_id = ? AND " + mediaIdColumn + " = ?";

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, playlistId);
	        stmt.setInt(2, mediaId);

	        int rowsDeleted = stmt.executeUpdate();

	        if (rowsDeleted > 0) {
	            System.out.println(" - " + mediaName + " removed from playlist.");
	        } else {
	            System.out.println(" - " + mediaName + " was not found in this playlist.");
	        }
	    }
	}
	
	/**
	 * Retrieves all songs in the given playlist for the current user.
	 *
	 * @param playlistId the playlist to retrieve songs from
	 * @return list of {@link Song} items in the playlist; empty list if none
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} belongs to an existing song playlist owned by {@code userId}
	 * @post returns the playlist's contents; no data is modified
	 */
	public List<Song> getSongsInPlaylist(int playlistId) throws SQLException {
		
		List<Song> mediaItems = new ArrayList<Song>();
		
		String sql = "SELECT m.id, m.title, m.creator, m.year, mr.status, mr.user_rating, mr.review, "
	            + "m.album, m.runtime_seconds "
	            + "FROM songs_playlists mp "
	            + "JOIN songs_playlist_items mpi "
	            + "ON mp.id = mpi.playlist_id "
	            + "JOIN songs m "
	            + "ON mpi.song_id = m.id "
	            + "JOIN songs_reviews mr "
	            + "ON m.id = mr.song_id AND mr.user_id = mp.user_id "
	            + "WHERE mp.user_id = ? AND mp.id = ?";
			
			try (PreparedStatement stmt = conn.prepareStatement(sql)){
				stmt.setInt(1, userId);
				stmt.setInt(2, playlistId);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					
					String statusString = rs.getString("status");
	
				    Status status = statusString == null
				            ? Status.PLANNED
				            : Status.fromDbString(statusString);
	
				    String review = rs.getString("review");
				    if (review == null) {
				        review = "";
				    }
				    
				    String title = rs.getString("title");
				    String creator = rs.getString("creator");
				    int year = rs.getInt("year");
				    double user_rating = rs.getDouble("user_rating");
				    
				    Song media = new Song(title,
							  			  status,
							  			  user_rating,
							  			  rs.getString("album"),
							  			  creator,
							  			  year,
							  			  rs.getInt("runtime_seconds"),
							  			  review);
	    			 
				    media.setMediaId(rs.getInt("id"));
					mediaItems.add(media);
				}
			}
		
		return mediaItems;
	}
	
	/**
	 * Retrieves all games in the given playlist for the current user.
	 *
	 * @param playlistId the playlist to retrieve games from
	 * @return list of {@link Game} items in the playlist; empty list if none
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} belongs to an existing game playlist owned by {@code userId}
	 * @post returns the playlist's contents; no data is modified
	 */
	public List<Game> getGamesInPlaylist(int playlistId) throws SQLException {
		
		List<Game> mediaItems = new ArrayList<Game>();
		
		String sql = "SELECT m.id, m.title, m.creator, m.year, mr.status, mr.user_rating, mr.review, "
		            + "m.genre, m.avg_playtime_mins "
		            + "FROM games_playlists mp "
		            + "JOIN games_playlist_items mpi "
		            + "ON mp.id = mpi.playlist_id "
		            + "JOIN games m "
		            + "ON mpi.game_id = m.id "
		            + "JOIN games_reviews mr "
		            + "ON m.id = mr.game_id AND mr.user_id = mp.user_id "
		            + "WHERE mp.user_id = ? AND mp.id = ?";
			
			try (PreparedStatement stmt = conn.prepareStatement(sql)){
				stmt.setInt(1, userId);
				stmt.setInt(2, playlistId);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					
					String statusString = rs.getString("status");
	
				    Status status = statusString == null
				            ? Status.PLANNED
				            : Status.fromDbString(statusString);
	
				    String review = rs.getString("review");
				    if (review == null) {
				        review = "";
				    }
				    
				    Game media = new Game(
	                        rs.getString("title"),
	                        rs.getString("creator"),
	                        rs.getInt("year"),
	                        status,
	                        rs.getDouble("user_rating"),
	                        review,
	                        rs.getString("genre"),
	                        rs.getInt("avg_playtime_mins"));
				    
				    media.setMediaId(rs.getInt("id"));
					mediaItems.add(media);
				}
			}
		
		return mediaItems;
	}
	
	/**
	 * Retrieves all shows in the given playlist for the current user.
	 *
	 * @param playlistId the playlist to retrieve shows from
	 * @return list of {@link Show} items in the playlist; empty list if none
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} belongs to an existing show playlist owned by {@code userId}
	 * @post returns the playlist's contents; no data is modified
	 */
	public List<Show> getShowsInPlaylist(int playlistId) throws SQLException {
		
		List<Show> mediaItems = new ArrayList<Show>();
		
		String sql = "SELECT m.id, m.title, m.creator, m.year_start, m.year_end, mr.status, mr.user_rating, mr.review, "
		            + "m.genre, m.num_of_seasons, m.num_of_episodes, m.avg_mins_per_ep, m.airing "
		            + "FROM shows_playlists mp "
		            + "JOIN shows_playlist_items mpi "
		            + "ON mp.id = mpi.playlist_id "
		            + "JOIN shows m "
		            + "ON mpi.show_id = m.id "
		            + "JOIN shows_reviews mr "
		            + "ON m.id = mr.show_id AND mr.user_id = mp.user_id "
		            + "WHERE mp.user_id = ? AND mp.id = ?";
			
			try (PreparedStatement stmt = conn.prepareStatement(sql)){
				stmt.setInt(1, userId);
				stmt.setInt(2, playlistId);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					
					String statusString = rs.getString("status");
	
				    Status status = statusString == null
				            ? Status.PLANNED
				            : Status.fromDbString(statusString);
	
				    String review = rs.getString("review");
				    if (review == null) {
				        review = "";
				    }
				    
				    Show media = new Show(
	                        rs.getString("title"),
	                        rs.getString("creator"),
	                        rs.getInt("year_start"),
	                        rs.getInt("year_end"),
	                        status,
	                        rs.getDouble("user_rating"),
	                        review,
	                        rs.getString("genre"),
	                        rs.getInt("num_of_seasons"),
	                        rs.getBoolean("airing"));
				    
				    media.setMediaId(rs.getInt("id"));
					mediaItems.add(media);
				}
			}
		
		return mediaItems;
	}
	
	/**
	 * Retrieves all media items in the specified playlist for the current user.
	 *
	 * @param playlistId the ID of the playlist to retrieve media from
	 * @param mediaType  the type of media in the playlist ("Song", "Game", or "Show")
	 * @return a list of {@link Media} objects (as their concrete subtype) in the
	 *         playlist; empty if the playlist has no items
	 * @throws SQLException if a database access error occurs
	 * @throws IllegalArgumentException if {@code mediaType} is not "Song", "Game", or "Show"
	 * @pre  {@code playlistId} refers to an existing playlist owned by {@code userId}
	 * @post returns the full list of media in the playlist; database state is unchanged
	 */
	public List<MediaPlaylist> getPlaylistsByUser(int userId, String mediaType) throws SQLException {
		
		List<MediaPlaylist> playlists = new ArrayList<>();
		List<? extends Media> items = new ArrayList<>();
		String sql = "SELECT id, title FROM " + mediaType.toLowerCase() + "s_playlists WHERE user_id = ?";
		
		try (PreparedStatement stmt = conn.prepareStatement(sql)){
			stmt.setInt(1, userId);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int playlistId = rs.getInt("id");
				
				if(mediaType.equalsIgnoreCase("song"))
				{
					items = getSongsInPlaylist(playlistId);
				}
				else if(mediaType.equalsIgnoreCase("game"))
				{
					items = getGamesInPlaylist(playlistId);
				}
				else if(mediaType.equalsIgnoreCase("show"))
				{
					items = getShowsInPlaylist(playlistId);
				}
				
				MediaPlaylist playlist = new MediaPlaylist(rs.getString("title"), items, playlistId);
				
				playlists.add(playlist);
			}
		}
		
		return playlists;
	}
	
	/**
	 * Deletes the specified playlist and all of its items.
	 *
	 * @param playlistId the ID of the playlist to delete
	 * @param mediaType  the type of media the playlist holds (e.g. "Song", "Song", "Song")
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} refers to an existing playlist
	 * @post all rows in the corresponding playlist-items table with this
	 *       {@code playlistId} are removed, and the playlist itself is removed
	 *       from the corresponding playlists table
	 */
	public void deletePlaylist(int playlistId, String mediaType) throws SQLException {
		
		String sql = "DELETE FROM " + mediaType + "s_playlist_items WHERE playlist_id = ?";
			
		try (PreparedStatement stmt = conn.prepareStatement(sql)){
			stmt.setInt(1, playlistId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
			
		sql = "DELETE FROM " + mediaType + "s_playlists WHERE id = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)){
			stmt.setInt(1, playlistId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Counts how many media items in the given playlist have the specified status.
	 *
	 * @param playlistId the ID of the playlist to check
	 * @param status the status to count (e.g. PLANNED, IN_PROGRESS, COMPLETED)
	 * @param mediaType the type of media in the playlist (e.g. "Song", "Game", "Show")
	 * @return the number of media items in the playlist with the given status
	 *         for the current user; 0 if none match
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} refers to an existing playlist owned by {@code userId}
	 * @post returns a count; no data is modified
	 */
	public int countStatusedMedia(int playlistId, Status status, String mediaType) throws SQLException {
	    
		String media = mediaType.toLowerCase();
		
		String sql = "SELECT COUNT(*) FROM " + media + "s_reviews mr "
				   + "JOIN " + media + "s_playlist_items mpi ON mr." + media + "_id = mpi." + media +"_id "
				   + "WHERE mpi.playlist_id = ? AND mr.user_id = ? AND LOWER(REPLACE(mr.status, '_', ' ')) = LOWER(?)";
	
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, playlistId);
			stmt.setInt(2, userId);
		    stmt.setString(3, status.toDbString());
	
		    try (ResultSet rs = stmt.executeQuery()) {
		    	return rs.next() ? rs.getInt(1) : 0;
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;

	}
	
	/**
	 * Calculates the average user rating of completed media items in the given playlist.
	 *
	 * @param playlistId the ID of the playlist to calculate the average for
	 * @param mediaType the type of media in the playlist (e.g. "Song", "Game", "Show")
	 * @return the average rating of media marked as {@code COMPLETED} in the
	 *         playlist for the current user; 0.0 if there are none
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} refers to an existing playlist owned by {@code userId}
	 * @post returns an average rating; no data is modified
	 */
	public double calculateAvgRating(int playlistId, String mediaType) throws SQLException {

	    String media = mediaType.toLowerCase();

	    String sql =
	        "SELECT AVG(mr.user_rating) " +
	        "FROM " + media + "s_reviews mr " +
	        "JOIN " + media + "s_playlist_items mpi " +
	        "ON mr." + media + "_id = mpi." + media + "_id " +
	        "WHERE mpi.playlist_id = ? " +
	        "AND mr.user_id = ? " +
	        "AND LOWER(REPLACE(mr.status, '_', ' ')) = LOWER(?)";

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, playlistId);
	        stmt.setInt(2, userId);
	        stmt.setString(3, Status.COMPLETED.toDbString());

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getDouble(1);
	            }
	        }
	    }

	    return 0.0;
	}
	
	/**
	 * Updates the stored review (status, rating, and review text) for the given
	 * media item, identified by title and creator, for the current user.
	 *
	 * @param media the media item whose updated status, rating, and review should
	 *              be persisted; its type determines which reviews table is
	 *              updated
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code media} is an instance of {@link Song}, {@link Game}, or
	 *       {@link Show}; a review row already exists for {@code userId} and a
	 *       media item matching {@code media}'s title and creator
	 * @post the matching row in the corresponding reviews table has its
	 *       {@code status}, {@code user_rating}, and {@code review} fields
	 *       updated to reflect {@code media}'s current values
	 */
	public void updateAllPlaylists(Media media) throws SQLException {
		
		String mediaType = null;
		
		if (media instanceof Song) {
			mediaType = "song";
		}
		if (media instanceof Game) {
			mediaType = "game";
		}
		if (media instanceof Show) {
			mediaType = "show";
		}
		
		// make use of review table
		// basically update the reviews of a media
		String sql = "UPDATE " + mediaType + "s_reviews " + 
    			"SET status = ?, user_rating = ?, review = ? " +
				"WHERE user_id = ? AND " + mediaType + "_id = " +
					"(SELECT id FROM " + mediaType + "s " +
					"WHERE title = ? AND creator = ?)";
	    
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, media.getStatus().toDbString());
	        stmt.setDouble(2, media.getUserRating());
	        stmt.setString(3, media.getReview());
	        stmt.setInt(4, userId);
	        stmt.setString(5, media.getTitle());
	        stmt.setString(6, media.getCreator());

	        stmt.executeUpdate();
	    }
	}
}