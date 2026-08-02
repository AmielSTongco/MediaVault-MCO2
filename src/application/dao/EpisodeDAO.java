package application.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.model.Episode;
import application.model.Status;

public class EpisodeDAO {
	
	/*
	 * Handles database operations involving episodes
	 * and user-specific episode reviews
	 */

	private final Connection conn;
	private final int userId;
	
	/**
	 * Creates an EpisodeDAO using the given database connection and user ID.
	 *
	 * @param conn active database connection
	 * @param userId current user ID
	 */
	public EpisodeDAO(Connection conn, int userId) {
		this.conn = conn;
		this.userId = userId;
	}
	
	/**
	 * Adds episodes to a season and creates their user review entries.
	 *
	 * @param seasonId parent season ID
	 * @param episodes episodes to add
	 * @throws SQLException if a database error occurs
	 */
	public void addEpisodes(int seasonId, List<Episode> episodes) throws SQLException {
		String insertEpisodeSql = "INSERT OR IGNORE INTO episodes(season_id, episode_number, title, writer, year_released, image_path) VALUES(?, ?, ?, ?, ?, ?)";
		String updateEpisodeSql = "UPDATE episodes SET title = ?, writer = ?, year_released = ?, image_path = ? WHERE season_id = ? AND episode_number = ?";
		String findEpisodeSql = "SELECT id FROM episodes WHERE season_id = ? AND episode_number = ?";
		String insertReviewSql = "INSERT OR IGNORE INTO episodes_reviews(user_id, episode_id, status, user_rating, review) VALUES(?, ?, ?, ?, ?)";

		boolean previousAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		// Performs episode insertion as one transaction
		try(PreparedStatement insertStmt = conn.prepareStatement(insertEpisodeSql);
			PreparedStatement updateStmt = conn.prepareStatement(updateEpisodeSql);
			PreparedStatement findStmt = conn.prepareStatement(findEpisodeSql);
			PreparedStatement reviewStmt = conn.prepareStatement(insertReviewSql))
		{
			for(Episode episode : episodes)
			{
				// Inserts episode when not yet stored
				insertStmt.setInt(1, seasonId);
				insertStmt.setInt(2, episode.getEpisodeNumber());
				insertStmt.setString(3, episode.getTitle());
				insertStmt.setString(4, episode.getWriter());
				insertStmt.setInt(5, episode.getYearReleased());
				insertStmt.setString(6, episode.getImagePath());
				insertStmt.executeUpdate();

				// Updates API information of existing episode
				updateStmt.setString(1, episode.getTitle());
				updateStmt.setString(2, episode.getWriter());
				updateStmt.setInt(3, episode.getYearReleased());
				updateStmt.setString(4, episode.getImagePath());
				updateStmt.setInt(5, seasonId);
				updateStmt.setInt(6, episode.getEpisodeNumber());
				updateStmt.executeUpdate();

				findStmt.setInt(1, seasonId);
				findStmt.setInt(2, episode.getEpisodeNumber());

				try(ResultSet rs = findStmt.executeQuery())
				{
					if(rs.next())
					{
						int episodeId = rs.getInt("id");

						episode.setEpisodeId(episodeId);
						episode.setSeasonId(seasonId);

						// Creates user review entry
						reviewStmt.setInt(1, userId);
						reviewStmt.setInt(2, episodeId);

						if(episode.getStatus() == null)
							reviewStmt.setString(3, Status.PLANNED.toDbString());
						else
							reviewStmt.setString(3, episode.getStatus().toDbString());

						reviewStmt.setDouble(4, episode.getUserRating());
						reviewStmt.setString(5, episode.getReview() == null ? "" : episode.getReview());
						reviewStmt.executeUpdate();
					}
				}
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
	
	/**
	 * Updates the image path of an episode.
	 *
	 * @param episodeId episode ID
	 * @param imagePath new image path
	 * @return number of updated rows
	 * @throws SQLException if a database error occurs
	 */
	public int updateEpisodeImage(int episodeId, String imagePath) throws SQLException {
		String sql = "UPDATE episodes SET image_path = ? WHERE id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setString(1, imagePath);
			stmt.setInt(2, episodeId);

			return stmt.executeUpdate();
		}
	}
	
	/**
	 * Creates or updates the current user's episode review.
	 *
	 * @param episode episode containing updated review values
	 * @return number of affected rows
	 * @throws SQLException if a database error occurs
	 */
	public int updateEpisodeReview(Episode episode) throws SQLException {
		String sql = "INSERT INTO episodes_reviews(user_id, episode_id, status, user_rating, review) "
				   + "VALUES(?, ?, ?, ?, ?) "
				   + "ON CONFLICT(user_id, episode_id) DO UPDATE SET "
				   + "status = excluded.status, "
				   + "user_rating = excluded.user_rating, "
				   + "review = excluded.review";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setInt(1, userId);
			stmt.setInt(2, episode.getEpisodeId());

			if(episode.getStatus() == null)
				stmt.setString(3, Status.PLANNED.toDbString());
			else
				stmt.setString(3, episode.getStatus().toDbString());

			stmt.setDouble(4, episode.getUserRating());
			stmt.setString(5, episode.getReview() == null ? "" : episode.getReview());

			return stmt.executeUpdate();
		}
	}
	
	/**
	 * Updates the main information of an episode.
	 *
	 * @param episode episode containing updated values
	 * @param seasonId parent season ID
	 * @return number of updated rows
	 * @throws SQLException if a database error occurs
	 */
	public int updateEpisode(Episode episode, int seasonId) throws SQLException {
		String sql = "UPDATE episodes SET title = ?, writer = ?, year_released = ?, episode_number = ?, image_path = ? WHERE id = ? AND season_id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setString(1, episode.getTitle());
			stmt.setString(2, episode.getWriter());
			stmt.setInt(3, episode.getYearReleased());
			stmt.setInt(4, episode.getEpisodeNumber());
			stmt.setString(5, episode.getImagePath());
			stmt.setInt(6, episode.getEpisodeId());
			stmt.setInt(7, seasonId);

			return stmt.executeUpdate();
		}
	}
	
	/**
	 * Checks whether every episode belonging to a show is completed.
	 *
	 * @param showId show ID
	 * @return true if all episodes are completed, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean areAllEpisodesCompletedForShow(int showId) throws SQLException {
		String sql = "SELECT COUNT(e.id) AS total_episodes, "
				   + "SUM(CASE WHEN er.status = ? THEN 1 ELSE 0 END) AS completed_episodes "
				   + "FROM seasons s "
				   + "JOIN episodes e ON e.season_id = s.id "
				   + "LEFT JOIN episodes_reviews er ON er.episode_id = e.id AND er.user_id = ? "
				   + "WHERE s.show_id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setString(1, Status.COMPLETED.toDbString());
			stmt.setInt(2, userId);
			stmt.setInt(3, showId);

			try(ResultSet rs = stmt.executeQuery())
			{
				boolean completed = false;

				if(rs.next())
				{
					int totalEpisodes = rs.getInt("total_episodes");
					int completedEpisodes = rs.getInt("completed_episodes");

					completed = totalEpisodes > 0 && totalEpisodes == completedEpisodes;
				}

				return completed;
			}
		}
	}
	
	/**
	 * Checks whether a show can be marked as completed.
	 *
	 * @param showId show ID
	 * @return true if no episode remains incomplete, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean canCompleteShow(int showId) throws SQLException {
		String sql = "SELECT COUNT(e.id) AS total_episodes, "
				   + "SUM(CASE WHEN er.status = ? THEN 0 ELSE 1 END) AS incomplete_episodes "
				   + "FROM seasons s "
				   + "JOIN episodes e ON e.season_id = s.id "
				   + "LEFT JOIN episodes_reviews er ON er.episode_id = e.id AND er.user_id = ? "
				   + "WHERE s.show_id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setString(1, Status.COMPLETED.toDbString());
			stmt.setInt(2, userId);
			stmt.setInt(3, showId);

			try(ResultSet rs = stmt.executeQuery())
			{
				boolean canComplete = false;

				if(rs.next())
				{
					int totalEpisodes = rs.getInt("total_episodes");
					int incompleteEpisodes = rs.getInt("incomplete_episodes");

					canComplete = totalEpisodes > 0 && incompleteEpisodes == 0;
				}

				return canComplete;
			}
		}
	}
	
	/**
	 * Retrieves episodes belonging to a season.
	 *
	 * @param seasonId season ID
	 * @return list of episodes ordered by episode number
	 * @throws SQLException if a database error occurs
	 */
	public List<Episode> getEpisodesBySeasonId(int seasonId) throws SQLException {
		List<Episode> episodes = new ArrayList<>();

		String sql = "SELECT e.id, e.season_id, e.episode_number, e.title, e.writer, e.year_released, e.image_path, "
				   + "er.status, er.user_rating, er.review "
				   + "FROM episodes e "
				   + "LEFT JOIN episodes_reviews er ON e.id = er.episode_id AND er.user_id = ? "
				   + "WHERE e.season_id = ? "
				   + "ORDER BY e.episode_number";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setInt(1, userId);
			stmt.setInt(2, seasonId);

			try(ResultSet rs = stmt.executeQuery())
			{
				while(rs.next())
				{
					Status status = Status.PLANNED;
					String statusString = rs.getString("status");

					if(statusString != null && !statusString.isBlank())
						status = Status.fromDbString(statusString);

					// Creates episode using database values
					Episode episode = new Episode(rs.getInt("id"), rs.getInt("season_id"), rs.getInt("episode_number"), rs.getString("title"), rs.getString("writer"), rs.getInt("year_released"), status, rs.getDouble("user_rating"), rs.getString("review"), rs.getString("image_path"));

					episode.setEpisodeId(rs.getInt("id"));
					episode.setSeasonId(rs.getInt("season_id"));
					episodes.add(episode);
				}
			}
		}

		return episodes;
	}
	
	/**
	 * Creates or updates user review values for an episode.
	 *
	 * @param episodeId episode ID
	 * @param status episode status
	 * @param rating user rating
	 * @param review user review
	 * @throws SQLException if a database error occurs
	 */
	public void updateEpisodeReview(int episodeId, Status status, double rating, String review) throws SQLException {
		String sql = "INSERT INTO episodes_reviews(user_id, episode_id, status, user_rating, review) "
				   + "VALUES(?, ?, ?, ?, ?) "
				   + "ON CONFLICT(user_id, episode_id) DO UPDATE SET "
				   + "status = excluded.status, "
				   + "user_rating = excluded.user_rating, "
				   + "review = excluded.review";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setInt(1, userId);
			stmt.setInt(2, episodeId);

			if(status == null)
				stmt.setString(3, Status.PLANNED.toDbString());
			else
				stmt.setString(3, status.toDbString());

			stmt.setDouble(4, rating);
			stmt.setString(5, review == null ? "" : review);
			stmt.executeUpdate();
		}
	}
	
	/**
	 * Checks whether a season contains at least one episode.
	 *
	 * @param seasonId season ID
	 * @return true if the season contains episodes, otherwise false
	 * @throws SQLException if a database error occurs
	 */
	public boolean hasEpisodes(int seasonId) throws SQLException {
		String sql = "SELECT 1 FROM episodes WHERE season_id = ? LIMIT 1";

		try(PreparedStatement stmt = conn.prepareStatement(sql))
		{
			stmt.setInt(1, seasonId);

			try(ResultSet rs = stmt.executeQuery())
			{
				return rs.next();
			}
		}
	}
	
	/**
	 * Deletes all episodes and reviews belonging to a season.
	 *
	 * @param seasonId season ID
	 * @throws SQLException if a database error occurs
	 */
	public void deleteEpisodesBySeasonId(int seasonId) throws SQLException {
		String findEpisodesSql = "SELECT id FROM episodes WHERE season_id = ?";
		String deleteReviewsSql = "DELETE FROM episodes_reviews WHERE episode_id = ?";
		String deleteEpisodesSql = "DELETE FROM episodes WHERE season_id = ?";

		boolean previousAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		// Performs deletion as one transaction
		try(PreparedStatement findStmt = conn.prepareStatement(findEpisodesSql);
			PreparedStatement reviewStmt = conn.prepareStatement(deleteReviewsSql);
			PreparedStatement episodeStmt = conn.prepareStatement(deleteEpisodesSql))
		{
			findStmt.setInt(1, seasonId);

			try(ResultSet rs = findStmt.executeQuery())
			{
				while(rs.next())
				{
					reviewStmt.setInt(1, rs.getInt("id"));
					reviewStmt.addBatch();
				}
			}

			reviewStmt.executeBatch();

			episodeStmt.setInt(1, seasonId);
			episodeStmt.executeUpdate();

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
	
	/**
	 * Deletes an episode and its user review entries.
	 *
	 * @param episodeId episode ID
	 * @return number of deleted episode rows
	 * @throws SQLException if a database error occurs
	 */
	public int deleteEpisode(int episodeId) throws SQLException {
		String deleteReviewSql = "DELETE FROM episodes_reviews WHERE episode_id = ?";
		String deleteEpisodeSql = "DELETE FROM episodes WHERE id = ?";

		boolean previousAutoCommit = conn.getAutoCommit();
		conn.setAutoCommit(false);

		// Performs deletion as one transaction
		try {
			try(PreparedStatement stmt = conn.prepareStatement(deleteReviewSql))
			{
				stmt.setInt(1, episodeId);
				stmt.executeUpdate();
			}

			int deleted;

			try(PreparedStatement stmt = conn.prepareStatement(deleteEpisodeSql))
			{
				stmt.setInt(1, episodeId);
				deleted = stmt.executeUpdate();
			}

			conn.commit();
			return deleted;
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