package application.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import application.model.Game;
import application.model.Media;
import application.model.Show;
import application.model.Song;
import application.model.Status;
import application.model.Type;


/**
 * Data Access Object that handles common database operations
 * for different media types, including songs, games, and shows.
 *
 * <p>This class performs operations for the currently logged-in user using
 * the provided database connection and user ID.</p>
 */
public class MediaDAO{
	
	Connection conn;
	int userId;

	/**
	 * Creates a MediaDAO using the given database connection and user ID.
	 *
	 * @param conn active database connection
	 * @param userId current user ID
	 */
	public MediaDAO(Connection conn, int userId) {
		this.conn = conn;
		this.userId = userId;
	}

	/**
	 * Checks whether a media item already exists in its corresponding table.
	 *
	 * @param media media item to search for
	 * @return true if media already exists, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean hasMedia(Media media) throws SQLException {
		int mediaId = -1;
		String table = "";

		// Selects corresponding media table
		if(media instanceof Song)
			table = "songs";
		else if(media instanceof Game)
			table = "games";
		else if(media instanceof Show)
			table = "shows";

		if(!table.isBlank())
		{
			String sql = "SELECT id FROM " + table + " WHERE title = ? AND creator = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setString(1, media.getTitle());
				stmt.setString(2, media.getCreator());

				try(ResultSet rs = stmt.executeQuery())
				{
					if(rs.next())
						mediaId = rs.getInt("id");
				}
			}
		}

		return mediaId > 0;
	}

	/**
	 * Adds a media item and associates it with the current user's default playlist.
	 *
	 * @param media media item to add
	 * @return database ID of added media
	 * @throws SQLException if a database error occurs
	 */
	public int addMedia(Media media) throws SQLException {
		int mediaId = -1;

		String table = "";
		String playlistTable = "";
		String junctionTable = "";
		String reviewTable = "";
		String playlistTitle = "";
		String junctionMediaIdColumn = "";

		// Selects tables and columns for media type
		if(media instanceof Song)
		{
			table = "songs";
			playlistTable = "songs_playlists";
			junctionTable = "songs_playlist_items";
			reviewTable = "songs_reviews";
			playlistTitle = "all_songs";
			junctionMediaIdColumn = "song_id";
		}
		else if(media instanceof Game)
		{
			table = "games";
			playlistTable = "games_playlists";
			junctionTable = "games_playlist_items";
			reviewTable = "games_reviews";
			playlistTitle = "all_games";
			junctionMediaIdColumn = "game_id";
		}
		else if(media instanceof Show)
		{
			table = "shows";
			playlistTable = "shows_playlists";
			junctionTable = "shows_playlist_items";
			reviewTable = "shows_reviews";
			playlistTitle = "all_shows";
			junctionMediaIdColumn = "show_id";
		}

		// Searches for existing media
		String findMediaSql = "SELECT id FROM " + table + " WHERE title = ? AND creator = ?";

		try(PreparedStatement stmt = conn.prepareStatement(findMediaSql))
		{
			stmt.setString(1, media.getTitle());
			stmt.setString(2, media.getCreator());

			try(ResultSet rs = stmt.executeQuery())
			{
				if(rs.next())
					mediaId = rs.getInt("id");
			}
		}

		// Inserts media when not yet stored
		if(mediaId <= 0)
		{
			String insertMediaSql = "INSERT INTO " + table + " (title, creator, image_path) VALUES (?, ?, ?)";

			try(PreparedStatement stmt = conn.prepareStatement(insertMediaSql, Statement.RETURN_GENERATED_KEYS))
			{
				stmt.setString(1, media.getTitle());
				stmt.setString(2, media.getCreator());
				stmt.setString(3, media.getImagePath());
				stmt.executeUpdate();

				try(ResultSet keys = stmt.getGeneratedKeys())
				{
					if(keys.next())
						mediaId = keys.getInt(1);
				}
			}
		}

		// Updates media-specific information
		switch(table)
		{
			case "songs":
				Song song = (Song)media;
				String updateSongSql = "UPDATE songs SET album = ?, year = ?, runtime_seconds = ?, image_path = ? WHERE id = ?";

				try(PreparedStatement stmt = conn.prepareStatement(updateSongSql))
				{
					stmt.setString(1, song.getAlbum());
					stmt.setInt(2, song.getYearReleased());
					stmt.setInt(3, song.getRuntimeSeconds());
					stmt.setString(4, song.getImagePath());
					stmt.setInt(5, mediaId);
					stmt.executeUpdate();
				}
				break;

			case "games":
				Game game = (Game)media;
				String updateGameSql = "UPDATE games SET avg_playtime_mins = ?, year = ?, genre = ?, image_path = ? WHERE id = ?";

				try(PreparedStatement stmt = conn.prepareStatement(updateGameSql))
				{
					stmt.setInt(1, game.getAvgPlaytimeMins());
					stmt.setInt(2, game.getYearReleased());
					stmt.setString(3, game.getGenre());
					stmt.setString(4, game.getImagePath());
					stmt.setInt(5, mediaId);
					stmt.executeUpdate();
				}
				break;

			case "shows":
				Show show = (Show)media;
				String updateShowSql = "UPDATE shows SET num_of_seasons = ?, year_start = ?, year_end = ?, airing = ?, genre = ?, image_path = ?, api_id = ? WHERE id = ?";

				try(PreparedStatement stmt = conn.prepareStatement(updateShowSql))
				{
					stmt.setInt(1, show.getNumOfSeasons());
					stmt.setInt(2, show.getYearStart());
					stmt.setInt(3, show.getYearEnd());
					stmt.setBoolean(4, show.isAiring());
					stmt.setString(5, show.getGenre());
					stmt.setString(6, show.getImagePath());
					stmt.setInt(7, show.getApiId());
					stmt.setInt(8, mediaId);
					stmt.executeUpdate();
				}
				break;
		}

		int playlistId = -1;

		// Finds default playlist
		String findPlaylistSql = "SELECT id FROM " + playlistTable + " WHERE user_id = ? AND title = ?";

		try(PreparedStatement stmt = conn.prepareStatement(findPlaylistSql))
		{
			stmt.setInt(1, userId);
			stmt.setString(2, playlistTitle);

			try(ResultSet rs = stmt.executeQuery())
			{
				if(rs.next())
					playlistId = rs.getInt("id");
			}
		}

		// Creates default playlist when missing
		if(playlistId == -1)
		{
			String insertPlaylistSql = "INSERT INTO " + playlistTable + " (user_id, title) VALUES (?, ?)";

			try(PreparedStatement stmt = conn.prepareStatement(insertPlaylistSql, Statement.RETURN_GENERATED_KEYS))
			{
				stmt.setInt(1, userId);
				stmt.setString(2, playlistTitle);
				stmt.executeUpdate();

				try(ResultSet keys = stmt.getGeneratedKeys())
				{
					if(keys.next())
						playlistId = keys.getInt(1);
				}
			}
		}

		// Adds media to default playlist
		String insertItemSql = "INSERT OR IGNORE INTO " + junctionTable + " (playlist_id, " + junctionMediaIdColumn + ") VALUES (?, ?)";

		try(PreparedStatement stmt = conn.prepareStatement(insertItemSql))
		{
			stmt.setInt(1, playlistId);
			stmt.setInt(2, mediaId);
			stmt.executeUpdate();
		}

		// Creates user review entry
		String insertReviewSql = "INSERT OR IGNORE INTO " + reviewTable + " (user_id, " + junctionMediaIdColumn + ", status, user_rating, review) VALUES (?, ?, ?, ?, ?)";

		try(PreparedStatement stmt = conn.prepareStatement(insertReviewSql))
		{
			stmt.setInt(1, userId);
			stmt.setInt(2, mediaId);
			stmt.setString(3, media.getStatus().toDbString());
			stmt.setDouble(4, media.getUserRating());
			stmt.setString(5, media.getReview());
			stmt.executeUpdate();
		}

		return mediaId;
	}

	/**
	 * Creates the current user's review entry for a media item.
	 *
	 * @param media media containing review information
	 * @throws SQLException if a database error occurs
	 */
	public void addMediaReview(Media media) throws SQLException {
		String reviewTable = "";
		String idColumn = "";

		// Selects review table and media ID column
		if(media instanceof Song)
		{
			reviewTable = "songs_reviews";
			idColumn = "song_id";
		}
		else if(media instanceof Game)
		{
			reviewTable = "games_reviews";
			idColumn = "game_id";
		}
		else if(media instanceof Show)
		{
			reviewTable = "shows_reviews";
			idColumn = "show_id";
		}

		if(!reviewTable.isBlank())
		{
			String sql = "INSERT OR IGNORE INTO " + reviewTable + " (user_id, " + idColumn + ", status, user_rating, review) VALUES (?, ?, ?, ?, ?)";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setInt(1, userId);
				stmt.setInt(2, media.getMediaId());
				stmt.setString(3, media.getStatus().toDbString());
				stmt.setDouble(4, media.getUserRating());
				stmt.setString(5, media.getReview());
				stmt.executeUpdate();
			}
		}
	}

	/**
	 * Retrieves all songs from the current user's default song playlist.
	 *
	 * @return list of the current user's songs
	 * @throws SQLException if a database error occurs
	 */
	public List<Song> getSongsByUser() throws SQLException {
		List<Song> songs = new ArrayList<>();

		String sql = "SELECT m.id, m.title, m.creator, m.year, mr.status, mr.user_rating, mr.review, m.image_path, "
				   + "m.album, m.runtime_seconds "
				   + "FROM songs_playlists mp "
				   + "INNER JOIN songs_playlist_items mpi ON mp.id = mpi.playlist_id "
				   + "INNER JOIN songs m ON mpi.song_id = m.id "
				   + "LEFT JOIN songs_reviews mr ON m.id = mr.song_id AND mr.user_id = ? "
				   + "WHERE mp.user_id = ? AND mp.title = 'all_songs'";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setInt(1, userId);
			stmt.setInt(2, userId);

			try(ResultSet rs = stmt.executeQuery())
			{
				while(rs.next())
				{
					String statusString = rs.getString("status");
					Status status = statusString == null ? Status.PLANNED : Status.fromDbString(statusString);

					String review = rs.getString("review");

					if(review == null)
						review = "";

					// Creates song using retrieved values
					Song song = new Song(rs.getString("title"), status, rs.getDouble("user_rating"), rs.getString("album"), rs.getString("creator"), rs.getInt("year"), rs.getInt("runtime_seconds"), review, rs.getString("image_path"));

					song.setMediaId(rs.getInt("id"));
					songs.add(song);
				}
			}
		}

		return songs;
	}
	
	
	/**
	 * Retrieves all games from the current user's default game playlist.
	 *
	 * @return list of the current user's games
	 * @throws SQLException if a database error occurs
	 */
	public List<Game> getGamesByUser() throws SQLException {
		List<Game> games = new ArrayList<>();

		String sql = "SELECT m.id, m.title, m.creator, m.year, mr.status, mr.user_rating, mr.review, m.genre, m.image_path, "
				   + "m.avg_playtime_mins "
				   + "FROM games_playlists mp "
				   + "INNER JOIN games_playlist_items mpi ON mp.id = mpi.playlist_id "
				   + "INNER JOIN games m ON mpi.game_id = m.id "
				   + "LEFT JOIN games_reviews mr ON m.id = mr.game_id AND mr.user_id = ? "
				   + "WHERE mp.user_id = ? AND mp.title = 'all_games'";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setInt(1, userId);
			stmt.setInt(2, userId);

			try(ResultSet rs = stmt.executeQuery())
			{
				while(rs.next())
				{
					String statusString = rs.getString("status");
					Status status = statusString == null ? Status.PLANNED : Status.fromDbString(statusString);

					String review = rs.getString("review");

					if(review == null)
						review = "";

					// Creates game using retrieved values
					Game game = new Game(
						rs.getString("title"),
						rs.getString("creator"),
						rs.getInt("year"),
						status,
						rs.getDouble("user_rating"),
						review,
						rs.getString("genre"),
						rs.getInt("avg_playtime_mins"),
						rs.getString("image_path")
					);

					game.setMediaId(rs.getInt("id"));
					games.add(game);
				}
			}
		}

		return games;
	}

	/**
	 * Retrieves all shows from the current user's default show playlist.
	 *
	 * @return list of the current user's shows
	 * @throws SQLException if a database error occurs
	 */
	public List<Show> getShowsByUser() throws SQLException {
		List<Show> shows = new ArrayList<>();

		String sql = "SELECT m.id, m.title, m.creator, m.year_start, m.year_end, m.genre, mr.status, mr.user_rating, mr.review, m.image_path, "
				   + "m.num_of_seasons, m.num_of_episodes, m.avg_mins_per_ep, m.airing, m.api_id "
				   + "FROM shows_playlists mp "
				   + "INNER JOIN shows_playlist_items mpi ON mp.id = mpi.playlist_id "
				   + "INNER JOIN shows m ON mpi.show_id = m.id "
				   + "LEFT JOIN shows_reviews mr ON m.id = mr.show_id AND mr.user_id = ? "
				   + "WHERE mp.user_id = ? AND mp.title = 'all_shows'";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setInt(1, userId);
			stmt.setInt(2, userId);

			try(ResultSet rs = stmt.executeQuery())
			{
				while(rs.next())
				{
					String statusString = rs.getString("status");
					Status status = statusString == null ? Status.PLANNED : Status.fromDbString(statusString);

					String review = rs.getString("review");

					if(review == null)
						review = "";

					// Creates show using retrieved values
					Show show = new Show(
						rs.getString("title"),
						rs.getString("creator"),
						rs.getInt("year_start"),
						rs.getInt("year_end"),
						status,
						rs.getDouble("user_rating"),
						review,
						rs.getString("genre"),
						rs.getInt("num_of_seasons"),
						rs.getBoolean("airing"),
						rs.getString("image_path")
					);

					show.setMediaId(rs.getInt("id"));
					show.setApiId(rs.getInt("api_id"));
					shows.add(show);
				}
			}
		}

		return shows;
	}

	/**
	 * Retrieves a media item owned by the current user using its ID and type.
	 *
	 * @param mediaId database ID of the media
	 * @param type media type to retrieve
	 * @return matching media item, or null if none exists
	 * @throws SQLException if a database error occurs
	 */
	public Media getMediaOfUserById(int mediaId, Type type) throws SQLException {
		if(type == Type.SONG)
		{
			// Retrieves requested song
			String sql = """
				SELECT s.id, s.title, s.album, s.creator, s.year, s.runtime_seconds,
				       sr.status, sr.user_rating, sr.review, s.image_path
				FROM songs_playlists sp
				INNER JOIN songs_playlist_items spi ON sp.id = spi.playlist_id
				INNER JOIN songs s ON spi.song_id = s.id
				LEFT JOIN songs_reviews sr ON s.id = sr.song_id AND sr.user_id = sp.user_id
				WHERE sp.user_id = ? AND s.id = ?
				""";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setInt(1, userId);
				stmt.setInt(2, mediaId);

				try(ResultSet rs = stmt.executeQuery())
				{
					if(rs.next())
					{
						String statusString = rs.getString("status");
						Status status = statusString == null ? Status.PLANNED : Status.fromDbString(statusString);

						String review = rs.getString("review");

						if(review == null)
							review = "";

						Song song = new Song(
							rs.getString("title"),
							status,
							rs.getDouble("user_rating"),
							rs.getString("album"),
							rs.getString("creator"),
							rs.getInt("year"),
							rs.getInt("runtime_seconds"),
							review,
							rs.getString("image_path")
						);

						song.setMediaId(rs.getInt("id"));
						return song;
					}
				}
			}
		}

		else if(type == Type.GAME)
		{
			// Retrieves requested game
			String sql = """
				SELECT g.id, g.title, g.creator, g.year, g.genre,
				       g.avg_playtime_mins, gr.status, gr.user_rating, gr.review, g.image_path
				FROM games_playlists gp
				INNER JOIN games_playlist_items gpi ON gp.id = gpi.playlist_id
				INNER JOIN games g ON gpi.game_id = g.id
				LEFT JOIN games_reviews gr ON g.id = gr.game_id AND gr.user_id = gp.user_id
				WHERE gp.user_id = ? AND g.id = ?
				""";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setInt(1, userId);
				stmt.setInt(2, mediaId);

				try(ResultSet rs = stmt.executeQuery())
				{
					if(rs.next())
					{
						String statusString = rs.getString("status");
						Status status = statusString == null ? Status.PLANNED : Status.fromDbString(statusString);

						String review = rs.getString("review");

						if(review == null)
							review = "";

						Game game = new Game(
							rs.getString("title"),
							rs.getString("creator"),
							rs.getInt("year"),
							status,
							rs.getDouble("user_rating"),
							review,
							rs.getString("genre"),
							rs.getInt("avg_playtime_mins"),
							rs.getString("image_path")
						);

						game.setMediaId(rs.getInt("id"));
						return game;
					}
				}
			}
		}

		else if(type == Type.SHOW)
		{
			// Retrieves requested show
			String sql = """
				SELECT s.id, s.title, s.creator, s.year_start, s.year_end,
				       s.genre, s.num_of_seasons, s.airing,
				       s.image_path, s.api_id,
				       sr.status, sr.user_rating, sr.review
				FROM shows_playlists sp
				INNER JOIN shows_playlist_items spi ON sp.id = spi.playlist_id
				INNER JOIN shows s ON spi.show_id = s.id
				LEFT JOIN shows_reviews sr ON s.id = sr.show_id AND sr.user_id = sp.user_id
				WHERE sp.user_id = ? AND s.id = ?
				""";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setInt(1, userId);
				stmt.setInt(2, mediaId);

				try(ResultSet rs = stmt.executeQuery())
				{
					if(rs.next())
					{
						String statusString = rs.getString("status");
						Status status = statusString == null ? Status.PLANNED : Status.fromDbString(statusString);

						String review = rs.getString("review");

						if(review == null)
							review = "";

						Show show = new Show(
							rs.getString("title"),
							rs.getString("creator"),
							rs.getInt("year_start"),
							rs.getInt("year_end"),
							status,
							rs.getDouble("user_rating"),
							review,
							rs.getString("genre"),
							rs.getInt("num_of_seasons"),
							rs.getBoolean("airing"),
							rs.getString("image_path")
						);

						show.setMediaId(rs.getInt("id"));
						show.setApiId(rs.getInt("api_id"));
						return show;
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Retrieves a song owned by the current user using its database ID.
	 *
	 * @param songId song ID
	 * @return matching song, or null if none exists
	 * @throws SQLException if a database error occurs
	 */
	public Song getSongOfUserById(int songId) throws SQLException {
		Media media = getMediaOfUserById(songId, Type.SONG);
		return media instanceof Song ? (Song)media : null;
	}

	/**
	 * Retrieves a game owned by the current user using its database ID.
	 *
	 * @param gameId game ID
	 * @return matching game, or null if none exists
	 * @throws SQLException if a database error occurs
	 */
	public Game getGameOfUserById(int gameId) throws SQLException {
		Media media = getMediaOfUserById(gameId, Type.GAME);
		return media instanceof Game ? (Game)media : null;
	}

	/**
	 * Retrieves a show owned by the current user using its database ID.
	 *
	 * @param showId show ID
	 * @return matching show, or null if none exists
	 * @throws SQLException if a database error occurs
	 */
	public Show getShowOfUserById(int showId) throws SQLException {
		Media media = getMediaOfUserById(showId, Type.SHOW);
		return media instanceof Show ? (Show)media : null;
	}

	/**
	 * Updates the current user's status for a media item.
	 *
	 * @param media media whose status will be updated
	 * @param newStatus new media status
	 * @throws SQLException if a database error occurs
	 */
	public void updateMediaStatus(Media media, Status newStatus) throws SQLException {
		String table = null;

		// Selects matching review table
		if(media instanceof Song)
			table = "song";
		else if(media instanceof Game)
			table = "game";
		else if(media instanceof Show)
			table = "show";

		if(table != null)
		{
			String sql = "INSERT INTO " + table + "s_reviews(user_id, " + table + "_id, status) "
					   + "VALUES(?, ?, ?) "
					   + "ON CONFLICT(user_id, " + table + "_id) DO UPDATE SET "
					   + "status = excluded.status";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setInt(1, userId);
				stmt.setInt(2, findMediaId(media));
				stmt.setString(3, newStatus.toDbString());
				stmt.executeUpdate();
			}
		}
	}
	
	/**
	 * Updates the current user's rating for a media item.
	 *
	 * @param media media whose rating will be updated
	 * @param rating new user rating
	 * @throws SQLException if a database error occurs
	 */
	public void updateMediaRating(Media media, double rating) throws SQLException {
		String table = null;

		// Selects matching review table
		if(media instanceof Song)
			table = "song";
		else if(media instanceof Game)
			table = "game";
		else if(media instanceof Show)
			table = "show";

		if(table != null)
		{
			String sql = "UPDATE " + table + "s_reviews "
					   + "SET user_rating = ? "
					   + "WHERE user_id = ? AND " + table + "_id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setDouble(1, rating);
				stmt.setInt(2, userId);
				stmt.setInt(3, findMediaId(media));
				stmt.executeUpdate();
			}
		}
	}

	/**
	 * Updates the current user's review for a media item.
	 *
	 * @param media media whose review will be updated
	 * @param review new review text
	 * @throws SQLException if a database error occurs
	 */
	public void updateMediaReview(Media media, String review) throws SQLException {
		String table = null;

		// Selects matching review table
		if(media instanceof Song)
			table = "song";
		else if(media instanceof Game)
			table = "game";
		else if(media instanceof Show)
			table = "show";

		if(table != null)
		{
			String sql = "UPDATE " + table + "s_reviews "
					   + "SET review = ? "
					   + "WHERE user_id = ? AND " + table + "_id = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setString(1, review);
				stmt.setInt(2, userId);
				stmt.setInt(3, findMediaId(media));
				stmt.executeUpdate();
			}
		}
	}

	/**
	 * Removes a media item from all playlists owned by the current user.
	 *
	 * @param media media item to remove
	 * @return 1 if media was removed, otherwise 0
	 * @throws SQLException if a database error occurs
	 */
	public int deleteMedia(Media media) throws SQLException {
		if(media instanceof Song)
			return deleteSong(media.getTitle(), media.getCreator());
		else if(media instanceof Game)
			return deleteGame(media.getTitle(), media.getCreator());
		else if(media instanceof Show)
			return deleteShow(media.getTitle(), media.getCreator());

		return 0;
	}

	/**
	 * Removes a song from all playlists owned by the current user.
	 *
	 * @param title song title
	 * @param creator song artist
	 * @return 1 if song was removed, otherwise 0
	 * @throws SQLException if a database error occurs
	 */
	public int deleteSong(String title, String creator) throws SQLException {
		int songId = -1;
		String findSongSql = "SELECT id FROM songs WHERE title = ? AND creator = ?";

		// Finds song ID
		try(PreparedStatement stmt = conn.prepareStatement(findSongSql))
		{
			stmt.setString(1, title);
			stmt.setString(2, creator);

			try(ResultSet rs = stmt.executeQuery())
			{
				if(rs.next())
					songId = rs.getInt("id");
			}
		}

		if(songId == -1)
			return 0;

		// Removes song from current user's playlists
		String deleteItemSql = "DELETE FROM songs_playlist_items WHERE song_id = ? AND playlist_id IN (SELECT id FROM songs_playlists WHERE user_id = ?)";

		try(PreparedStatement stmt = conn.prepareStatement(deleteItemSql))
		{
			stmt.setInt(1, songId);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}

		return 1;
	}

	/**
	 * Removes a game from all playlists owned by the current user.
	 *
	 * @param title game title
	 * @param creator game developer
	 * @return 1 if game was removed, otherwise 0
	 * @throws SQLException if a database error occurs
	 */
	public int deleteGame(String title, String creator) throws SQLException {
		int gameId = -1;
		String findGameSql = "SELECT id FROM games WHERE title = ? AND creator = ?";

		// Finds game ID
		try(PreparedStatement stmt = conn.prepareStatement(findGameSql))
		{
			stmt.setString(1, title);
			stmt.setString(2, creator);

			try(ResultSet rs = stmt.executeQuery())
			{
				if(rs.next())
					gameId = rs.getInt("id");
			}
		}

		if(gameId == -1)
			return 0;

		// Removes game from current user's playlists
		String deleteItemSql = "DELETE FROM games_playlist_items WHERE game_id = ? AND playlist_id IN (SELECT id FROM games_playlists WHERE user_id = ?)";

		try(PreparedStatement stmt = conn.prepareStatement(deleteItemSql))
		{
			stmt.setInt(1, gameId);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}

		return 1;
	}
	
	/**
	 * Removes a show from all playlists owned by the current user.
	 *
	 * @param title show title
	 * @param creator show creator
	 * @return 1 if show was removed, otherwise 0
	 * @throws SQLException if a database error occurs
	 */
	public int deleteShow(String title, String creator) throws SQLException {
		int showId = -1;
		String findShowSql = "SELECT id FROM shows WHERE title = ? AND creator = ?";

		// Finds show ID
		try(PreparedStatement stmt = conn.prepareStatement(findShowSql))
		{
			stmt.setString(1, title);
			stmt.setString(2, creator);

			try(ResultSet rs = stmt.executeQuery())
			{
				if(rs.next())
					showId = rs.getInt("id");
			}
		}

		if(showId == -1)
			return 0;

		// Removes show from current user's playlists
		String deleteItemSql = "DELETE FROM shows_playlist_items WHERE show_id = ? AND playlist_id IN (SELECT id FROM shows_playlists WHERE user_id = ?)";

		try(PreparedStatement stmt = conn.prepareStatement(deleteItemSql))
		{
			stmt.setInt(1, showId);
			stmt.setInt(2, userId);
			stmt.executeUpdate();
		}

		return 1;
	}

	/**
	 * Retrieves media ID using its title and creator.
	 *
	 * @param media media item to search for
	 * @return matching media ID, or -1 if none exists
	 * @throws SQLException if a database error occurs
	 */
	public int findMediaId(Media media) throws SQLException {
		String table = null;

		// Selects matching media table
		if(media instanceof Song)
			table = "songs";
		else if(media instanceof Game)
			table = "games";
		else if(media instanceof Show)
			table = "shows";

		if(table != null)
		{
			String sql = "SELECT id FROM " + table + " WHERE title = ? AND creator = ?";

			try(PreparedStatement stmt = conn.prepareStatement(sql))
			{
				stmt.setString(1, media.getTitle());
				stmt.setString(2, media.getCreator());

				try(ResultSet rs = stmt.executeQuery())
				{
					if(rs.next())
						return rs.getInt("id");
				}
			}
		}

		return -1;
	}

}
