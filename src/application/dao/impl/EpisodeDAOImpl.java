package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.model.Episode;
import application.model.Status;

public class EpisodeDAOImpl {

	private final Connection conn;
	private final int userId;

	public EpisodeDAOImpl(Connection conn, int userId) {
		this.conn = conn;
		this.userId = userId;
	}

	public void addEpisodes(int seasonId, List<Episode> episodes) throws SQLException {
		String insertEpisodeSql = "INSERT OR IGNORE INTO episodes(season_id, episode_number, title, writer, image_path) VALUES(?, ?, ?, ?, ?)";
		String updateEpisodeSql = "UPDATE episodes SET title = ?, writer = ?, image_path = ? WHERE season_id = ? AND episode_number = ?";

		try(PreparedStatement insertStmt = conn.prepareStatement(insertEpisodeSql);
			PreparedStatement updateStmt = conn.prepareStatement(updateEpisodeSql)) {

			for(Episode episode : episodes) {
				insertStmt.setInt(1, seasonId);
				insertStmt.setInt(2, episode.getEpisodeNumber());
				insertStmt.setString(3, episode.getTitle());
				insertStmt.setString(4, episode.getWriter());
				insertStmt.setString(5, episode.getImagePath());
				insertStmt.addBatch();

				updateStmt.setString(1, episode.getTitle());
				updateStmt.setString(2, episode.getWriter());
				updateStmt.setString(3, episode.getImagePath());
				updateStmt.setInt(4, seasonId);
				updateStmt.setInt(5, episode.getEpisodeNumber());
				updateStmt.addBatch();
			}

			insertStmt.executeBatch();
			updateStmt.executeBatch();
		}
	}

	public List<Episode> getEpisodesBySeasonId(int seasonId) throws SQLException {
		List<Episode> episodes = new ArrayList<>();

		String sql = "SELECT e.id, e.season_id, e.episode_number, e.title, e.writer, e.image_path, "
				   + "er.status, er.user_rating, er.review "
				   + "FROM episodes e "
				   + "LEFT JOIN episodes_reviews er ON e.id = er.episode_id AND er.user_id = ? "
				   + "WHERE e.season_id = ? "
				   + "ORDER BY e.episode_number";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, seasonId);

			try(ResultSet rs = stmt.executeQuery()) {
				while(rs.next()) {
					Status status = null;
					String statusString = rs.getString("status");

					if(statusString != null && !statusString.isBlank())
						status = Status.fromDbString(statusString);

					Episode episode = new Episode(
						rs.getInt("id"),
						rs.getInt("season_id"),
						rs.getInt("episode_number"),
						rs.getString("title"),
						rs.getString("writer"),
						status,
						rs.getDouble("user_rating"),
						rs.getString("review"),
						rs.getString("image_path")
					);

					episode.setMediaId(rs.getInt("id"));
					episodes.add(episode);
				}
			}
		}

		return episodes;
	}

	public void updateEpisodeReview(int episodeId, Status status, double rating, String review) throws SQLException {
		String sql = "INSERT INTO episodes_reviews(user_id, episode_id, status, user_rating, review) "
				   + "VALUES(?, ?, ?, ?, ?) "
				   + "ON CONFLICT(user_id, episode_id) DO UPDATE SET "
				   + "status = excluded.status, "
				   + "user_rating = excluded.user_rating, "
				   + "review = excluded.review";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, episodeId);

			if(status == null)
				stmt.setNull(3, java.sql.Types.VARCHAR);
			else
				stmt.setString(3, status.toDbString());

			stmt.setDouble(4, rating);
			stmt.setString(5, review);
			stmt.executeUpdate();
		}
	}

	public boolean hasEpisodes(int seasonId) throws SQLException {
		String sql = "SELECT 1 FROM episodes WHERE season_id = ? LIMIT 1";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, seasonId);

			try(ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		}
	}

	public void deleteEpisodesBySeasonId(int seasonId) throws SQLException {
		String sql = "DELETE FROM episodes WHERE season_id = ?";

		try(PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, seasonId);
			stmt.executeUpdate();
		}
	}
}