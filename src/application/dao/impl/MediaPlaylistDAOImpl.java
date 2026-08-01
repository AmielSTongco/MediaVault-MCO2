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
	 * @param playlistPicturePath the file path of the chosen playlist picture
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
	public boolean createPlaylist(String name, String playlistPicturePath, Type mediaType) throws SQLException {
		String normalizedName = name.trim().toLowerCase();

		if(normalizedName.isEmpty())
			return false;

		String tableName = mediaType.getTitle().toLowerCase() + "_playlists";
		String reservedName = "all_" + mediaType.getTitle().toLowerCase();

		if(normalizedName.equals(reservedName) || normalizedName.equals(reservedName.replace("_", " ")))
			return false;

		String orderSql = "SELECT COALESCE(MAX(display_order), 0) + 1 "
						+ "FROM " + tableName + " "
						+ "WHERE user_id = ?";

		int displayOrder = 1;

		try(PreparedStatement stmt = conn.prepareStatement(orderSql)) {
			stmt.setInt(1, userId);

			ResultSet rs = stmt.executeQuery();

			if(rs.next())
				displayOrder = rs.getInt(1);
		}

		String sql = "INSERT INTO " + tableName
				   + " (user_id, title, image_path, display_order) VALUES (?, ?, ?, ?)";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setString(2, name.trim());
			stmt.setString(3, playlistPicturePath);
			stmt.setInt(4, displayOrder);

			stmt.executeUpdate();
			return true;
		}
		catch(SQLException e) {
			if(e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed"))
				return false;

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
		
		String media = mediaType.toLowerCase();

		if(media.endsWith("s"))
			media = media.substring(0, media.length() - 1);

		String tableName = media + "s_playlist_items";
		String idColumn = media + "_id";

		String orderSql = "SELECT COALESCE(MAX(display_order), 0) + 1 "
				+ "FROM " + tableName + " WHERE playlist_id = ?";

		int displayOrder = 1;

		try(PreparedStatement stmt = conn.prepareStatement(orderSql)) {
			stmt.setInt(1, playlistId);

			ResultSet rs = stmt.executeQuery();

			if(rs.next())
				displayOrder = rs.getInt(1);
		}

		String sql = "INSERT OR IGNORE INTO " + tableName
			+ " (playlist_id, " + idColumn + ", display_order) VALUES (?, ?, ?)";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, playlistId);
			stmt.setInt(2, mediaId);
			stmt.setInt(3, displayOrder);
			stmt.executeUpdate();
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
	    
	    reorderPlaylistItems(playlistId, type.name());
	}
	
	public List<Media> getMediasInPlaylist(int playlistId, Type mediaType) throws SQLException {

		if(mediaType == Type.SONG)
			return new ArrayList<>(getSongsInPlaylist(playlistId));

		if(mediaType == Type.GAME)
			return new ArrayList<>(getGamesInPlaylist(playlistId));

		if(mediaType == Type.SHOW)
			return new ArrayList<>(getShowsInPlaylist(playlistId));

		return new ArrayList<>();
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
		
		String sql = "SELECT m.id, m.title, m.creator, m.year, mr.status, mr.user_rating, mr.review, m.image_path, "
	            + "m.album, m.runtime_seconds "
	            + "FROM songs_playlists mp "
	            + "JOIN songs_playlist_items mpi "
	            + "ON mp.id = mpi.playlist_id "
	            + "JOIN songs m "
	            + "ON mpi.song_id = m.id "
	            + "JOIN songs_reviews mr "
	            + "ON m.id = mr.song_id AND mr.user_id = mp.user_id "
	            + "WHERE mp.user_id = ? AND mp.id = ? "
	            + "ORDER BY mpi.display_order ASC";
			
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
							  			  review,
							  			  rs.getString("image_path"));
	    			 
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
		
		String sql = "SELECT m.id, m.title, m.creator, m.year, mr.status, mr.user_rating, mr.review, m.image_path, "
		            + "m.genre, m.avg_playtime_mins "
		            + "FROM games_playlists mp "
		            + "JOIN games_playlist_items mpi "
		            + "ON mp.id = mpi.playlist_id "
		            + "JOIN games m "
		            + "ON mpi.game_id = m.id "
		            + "JOIN games_reviews mr "
		            + "ON m.id = mr.game_id AND mr.user_id = mp.user_id "
		            + "WHERE mp.user_id = ? AND mp.id = ? "
		            + "ORDER BY mpi.display_order ASC";
			
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
	                        rs.getInt("avg_playtime_mins"),
	                        rs.getString("image_path"));
				    
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
		            + "WHERE mp.user_id = ? AND mp.id = ? "
		            + "ORDER BY mpi.display_order ASC";
			
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
	public List<MediaPlaylist> getPlaylistsByUser(int userId, Type mediaType) throws SQLException {

	    List<MediaPlaylist> playlists = new ArrayList<>();
	    String tableName = mediaType.getTitle().toLowerCase() + "_playlists";
	    
	    String defaultTitle = "all_" + mediaType.getTitle().toLowerCase();

	    String sql = "SELECT id, title, image_path FROM " + tableName + " WHERE user_id = ? "
	    		+ "ORDER BY CASE WHEN title = ? THEN 0 ELSE 1 END, display_order ASC";

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, userId);
	        stmt.setString(2, defaultTitle);

	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            int playlistId = rs.getInt("id");

	            int completedCount = countStatusedMedia(playlistId, Status.COMPLETED, mediaType);
	            int inProgressCount = countStatusedMedia(playlistId, Status.IN_PROGRESS, mediaType);
	            int plannedCount = countStatusedMedia(playlistId, Status.PLANNED, mediaType);
	            int totalCount = completedCount + inProgressCount + plannedCount;
	            double avgRatingCount = calculateAvgRating(playlistId, mediaType);
	            
	            MediaPlaylist playlist = new MediaPlaylist(
	            		playlistId,  
	            		rs.getString("title"),
	            		rs.getString("image_path"),
	            		totalCount,
	            		completedCount,
	            		inProgressCount,
	            		plannedCount,
	            		avgRatingCount
	            	);

	            playlists.add(playlist);
	        }
	    }

	    return playlists;
	}
	
	/**
	 * Deletes the specified playlist and all of its items.
	 *
	 * @param playlistId the ID of the playlist to delete
	 * @param mediaType  the type of media the playlist holds (e.g. "Song", "Game", "Show")
	 * @throws SQLException if a database access error occurs
	 * @pre  {@code playlistId} refers to an existing playlist
	 * @post all rows in the corresponding playlist-items table with this
	 *       {@code playlistId} are removed, and the playlist itself is removed
	 *       from the corresponding playlists table
	 */
	public void deletePlaylist(int playlistId, String mediaType) throws SQLException {
		String media = mediaType.toLowerCase();

		if(media.endsWith("s"))
			media = media.substring(0, media.length() - 1);

		String itemsTable = media + "s_playlist_items";
		String playlistsTable = media + "s_playlists";

		conn.setAutoCommit(false);

		try {
			String sql = "DELETE FROM " + itemsTable + " WHERE playlist_id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, playlistId);
				stmt.executeUpdate();
			}

			sql = "DELETE FROM " + playlistsTable + " WHERE id = ? AND user_id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql)) {
				stmt.setInt(1, playlistId);
				stmt.setInt(2, userId);
				stmt.executeUpdate();
			}

			reorderPlaylists(userId, media);
			conn.commit();
		}
		catch(SQLException e) {
			conn.rollback();
			throw e;
		}
		finally {
			conn.setAutoCommit(true);
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
	public int countStatusedMedia(int playlistId, Status status, Type mediaType) throws SQLException {

	    String media = mediaType.name().toLowerCase();

	    String sql = "SELECT COUNT(*) FROM " + media + "s_reviews mr "
	               + "JOIN " + media + "s_playlist_items mpi "
	               + "ON mr." + media + "_id = mpi." + media + "_id "
	               + "WHERE mpi.playlist_id = ? "
	               + "AND mr.user_id = ? "
	               + "AND LOWER(REPLACE(mr.status, '_', ' ')) = LOWER(?)";

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
	public double calculateAvgRating(int playlistId, Type mediaType) throws SQLException {

	    String media = mediaType.name().toLowerCase();

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
	
	public String getPlaylistImage(int playlistId, Type type) throws SQLException {
		String imagePath = getCustomPlaylistImage(playlistId, type);

		if(imagePath != null && !imagePath.isBlank())
			return imagePath;

		switch(type) {
			case SONG:
				return "/resources/application/images/icons/default-song-playlist-icon.png";

			case GAME:
				return "/resources/application/images/icons/default-game-playlist-icon.png";

			case SHOW:
				return "/resources/application/images/icons/default-show-playlist-icon.png";
		}

		return null;
	}
	
	private String getCustomPlaylistImage(int playlistId, Type type) throws SQLException {
		String table = switch(type) {
			case SONG -> "songs_playlists";
			case GAME -> "games_playlists";
			case SHOW -> "shows_playlists";
		};

		String sql = "SELECT image_path FROM " + table + " WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, playlistId);

			try(ResultSet rs = stmt.executeQuery()) {
				if(rs.next())
					return rs.getString("image_path");
			}
		}

		return null;
	}
	
	private void reorderPlaylistItems(int playlistId, String mediaType) throws SQLException {
		String media = mediaType.toLowerCase();

		if(media.endsWith("s"))
			media = media.substring(0, media.length() - 1);

		String tableName = media + "s_playlist_items";
		String idColumn = media + "_id";

		String selectSql = "SELECT " + idColumn + " FROM " + tableName + " WHERE playlist_id = ? ORDER BY display_order";
		List<Integer> mediaIds = new ArrayList<>();

		try(PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setInt(1, playlistId);

			ResultSet rs = stmt.executeQuery();

			while(rs.next())
				mediaIds.add(rs.getInt(idColumn));
		}

		String updateSql = "UPDATE " + tableName + " SET display_order = ? WHERE playlist_id = ? AND " + idColumn + " = ?";

		try(PreparedStatement stmt = conn.prepareStatement(updateSql)) {
			for(int i=0; i < mediaIds.size(); i++) {
				stmt.setInt(1, i + 1);
				stmt.setInt(2, playlistId);
				stmt.setInt(3, mediaIds.get(i));
				stmt.addBatch();
			}

			stmt.executeBatch();
		}
	}
	
	private void reorderPlaylists(int userId, String mediaType) throws SQLException {
		String media = mediaType.toLowerCase();

		if(media.endsWith("s"))
			media = media.substring(0, media.length() - 1);

		String tableName = media + "s_playlists";
		String selectSql = "SELECT id FROM " + tableName + " WHERE user_id = ? ORDER BY display_order";
		List<Integer> playlistIds = new ArrayList<>();

		try(PreparedStatement stmt = conn.prepareStatement(selectSql)) {
			stmt.setInt(1, userId);

			ResultSet rs = stmt.executeQuery();

			while(rs.next())
				playlistIds.add(rs.getInt("id"));
		}

		String updateSql = "UPDATE " + tableName + " SET display_order = ? WHERE id = ? AND user_id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(updateSql)) {
			for(int i=0; i < playlistIds.size(); i++) {
				stmt.setInt(1, i + 1);
				stmt.setInt(2, playlistIds.get(i));
				stmt.setInt(3, userId);
				stmt.addBatch();
			}

			stmt.executeBatch();
		}
	}
	
	public void updateMediaOrder(int playlistId, List<Media> mediaItems, Type mediaType) throws SQLException {
		String media = mediaType.name().toLowerCase();
		String tableName = media + "s_playlist_items";
		String idColumn = media + "_id";

		String sql = "UPDATE " + tableName
				   + " SET display_order = ? WHERE playlist_id = ? AND " + idColumn + " = ?";

		conn.setAutoCommit(false);

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			for(int i=0; i < mediaItems.size(); i++) {
				stmt.setInt(1, i + 1);
				stmt.setInt(2, playlistId);
				stmt.setInt(3, mediaItems.get(i).getMediaId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			conn.commit();
		}
		catch(SQLException e) {
			conn.rollback();
			throw e;
		}
		finally {
			conn.setAutoCommit(true);
		}
	}
	
	public void updatePlaylistOrder(List<MediaPlaylist> playlists, Type mediaType) throws SQLException {
		String tableName = mediaType.getTitle().toLowerCase() + "_playlists";
		String defaultTitle = "all_" + mediaType.getTitle().toLowerCase();
		String sql = "UPDATE " + tableName + " SET display_order = ? WHERE id = ? AND user_id = ?";

		conn.setAutoCommit(false);

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			int order = 1;

			for(MediaPlaylist playlist : playlists) {
				if(playlist.getTitle().equals(defaultTitle))
					stmt.setInt(1, 0);
				else {
					stmt.setInt(1, order);
					order++;
				}

				stmt.setInt(2, playlist.getPlaylistId());
				stmt.setInt(3, userId);
				stmt.addBatch();
			}

			stmt.executeBatch();
			conn.commit();
		}
		catch(SQLException e) {
			conn.rollback();
			throw e;
		}
		finally {
			conn.setAutoCommit(true);
		}
	}
}