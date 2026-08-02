package application.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.model.Season;
import application.model.Show;

public class SeasonDAO {
	
	/*
	 * Handles database operations involving seasons
	 * and their connection to shows and episodes
	 */

	private final Connection conn;
	private final int userId;
	
	/**
	 * Creates a SeasonDAO using the given database connection and user ID.
	 *
	 * @param conn active database connection
	 * @param userId current user ID
	 */
	public SeasonDAO(Connection conn, int userId) {
		this.conn = conn;
		this.userId = userId;
	}
	
	/**
	 * Generates the specified number of seasons for a show.
	 *
	 * @param showId parent show ID
	 * @param numberOfSeasons number of seasons to generate
	 * @param seasonImagePaths image paths belonging to each season
	 * @throws SQLException if a database error occurs
	 */
	public void generateSeasons(int showId, int numberOfSeasons, List<String> seasonImagePaths) throws SQLException {
		String sql = "INSERT INTO seasons(show_id, title, display_order, image_path, season_number, episode_count) "
				   + "VALUES(?, ?, ?, ?, ?, 0) "
				   + "ON CONFLICT(show_id, season_number) DO UPDATE SET "
				   + "title = excluded.title, "
				   + "display_order = excluded.display_order, "
				   + "image_path = excluded.image_path";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Creates or updates each season
			for(int seasonNumber=1; seasonNumber<=numberOfSeasons; seasonNumber++) {
				String imagePath = "";

				if(seasonImagePaths != null && seasonNumber-1 < seasonImagePaths.size()) {
					String storedPath = seasonImagePaths.get(seasonNumber-1);

					if(storedPath != null)
						imagePath = storedPath;
				}

				stmt.setInt(1, showId);
				stmt.setString(2, "Season " + seasonNumber);
				stmt.setInt(3, seasonNumber);
				stmt.setString(4, imagePath);
				stmt.setInt(5, seasonNumber);
				stmt.addBatch();
			}

			stmt.executeBatch();
		}
	}
	
	/**
	 * Updates the saved image path of a season.
	 *
	 * @param seasonId season ID
	 * @param imagePath new image path
	 * @return number of updated rows
	 * @throws SQLException if a database error occurs
	 */
	public int updateSeasonImagePath(int seasonId, String imagePath) throws SQLException {
		String sql = "UPDATE seasons SET image_path = ? WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, imagePath);
			stmt.setInt(2, seasonId);

			return stmt.executeUpdate();
		}
	}
	
	/**
	 * Adds or updates multiple seasons belonging to a show.
	 *
	 * @param showId parent show ID
	 * @param seasons seasons to add
	 * @throws SQLException if a database error occurs
	 */
	public void addSeasons(int showId, List<Season> seasons) throws SQLException {
		String insertSql = "INSERT OR IGNORE INTO seasons(show_id, season_number, title, image_path) VALUES(?, ?, ?, ?)";
		String updateSql = "UPDATE seasons SET title = ?, image_path = ? WHERE show_id = ? AND season_number = ?";

		try(PreparedStatement insertStmt = conn.prepareStatement(insertSql);
			PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
			
			// Prepares season insertions and updates
			for(Season season : seasons) {
				insertStmt.setInt(1, showId);
				insertStmt.setInt(2, season.getSeasonNumber());
				insertStmt.setString(3, season.getTitle());
				insertStmt.setString(4, season.getImagePath());
				insertStmt.addBatch();

				updateStmt.setString(1, season.getTitle());
				updateStmt.setString(2, season.getImagePath());
				updateStmt.setInt(3, showId);
				updateStmt.setInt(4, season.getSeasonNumber());
				updateStmt.addBatch();
			}

			insertStmt.executeBatch();
			updateStmt.executeBatch();
		}
	}
	
	/**
	 * Adds one season to a show.
	 *
	 * @param showId parent show ID
	 * @param season season to add
	 * @return number of inserted rows
	 * @throws SQLException if a database error occurs
	 */
	public int addSeason(int showId, Season season) throws SQLException {
		String sql = "INSERT INTO seasons(show_id, title, season_number, episode_count, image_path) VALUES (?, ?, ?, ?, ?)";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, showId);
			stmt.setString(2, season.getTitle());
			stmt.setInt(3, season.getSeasonNumber());
			stmt.setInt(4, season.getEpisodeCount());
			stmt.setString(5, season.getImagePath());

			return stmt.executeUpdate();
		}
	}
	
	/**
	 * Retrieves all seasons belonging to a show.
	 *
	 * @param showId parent show ID
	 * @return seasons ordered by season number
	 * @throws SQLException if a database error occurs
	 */
	public List<Season> getSeasonsByShowId(int showId) throws SQLException {
		List<Season> seasons = new ArrayList<>();

		String sql = "SELECT s.id, s.show_id, s.season_number, s.title, s.image_path, "
				   + "COUNT(e.id) AS episode_count, "
				   + "SUM(CASE WHEN er.status = 'completed' THEN 1 ELSE 0 END) AS completed_count, "
				   + "SUM(CASE WHEN er.status = 'in_progress' THEN 1 ELSE 0 END) AS in_progress_count, "
				   + "SUM(CASE WHEN er.status = 'planned' OR er.status IS NULL THEN 1 ELSE 0 END) AS planned_count, "
				   + "COALESCE(AVG(CASE WHEN er.user_rating > 0 THEN er.user_rating END), 0) AS avg_rating "
				   + "FROM seasons s "
				   + "LEFT JOIN episodes e ON e.season_id = s.id "
				   + "LEFT JOIN episodes_reviews er ON er.episode_id = e.id AND er.user_id = ? "
				   + "WHERE s.show_id = ? "
				   + "GROUP BY s.id, s.show_id, s.season_number, s.title, s.image_path "
				   + "ORDER BY s.season_number";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, showId);

			try(ResultSet rs = stmt.executeQuery()) {
				while(rs.next()) {
					// Creates season using retrieved statistics
					Season season = new Season(rs.getInt("id"), rs.getInt("show_id"), rs.getInt("season_number"), rs.getString("title"), rs.getString("image_path"), rs.getInt("episode_count"), rs.getInt("completed_count"), rs.getInt("in_progress_count"), rs.getInt("planned_count"), rs.getDouble("avg_rating"));

					seasons.add(season);
				}
			}
		}

		return seasons;
	}
	
	/**
	 * Permanently deletes a show from the database.
	 *
	 * @param show show to delete
	 * @return number of deleted rows
	 * @throws SQLException if a database error occurs
	 */
	public int deleteShowPermanently(Show show) throws SQLException {
		String sql = "DELETE FROM shows WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, show.getMediaId());

			return stmt.executeUpdate();
		}
	}
	
	/**
	 * Checks whether a season number already exists for a show.
	 *
	 * @param showId parent show ID
	 * @param seasonNumber season number to check
	 * @return true if the season already exists, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean seasonExists(int showId, int seasonNumber) throws SQLException {
		String sql = "SELECT 1 FROM seasons WHERE show_id = ? AND season_number = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, showId);
			stmt.setInt(2, seasonNumber);

			try(ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}
}