package application.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.model.Season;

public class SeasonDAOImpl {

	private final Connection conn;
	private final int userId;

	public SeasonDAOImpl(Connection conn, int userId) {
		this.conn = conn;
		this.userId = userId;
	}

	public void generateSeasons(int showId, int numOfSeasons, List<String> imagePaths) throws SQLException {
		String insertSql = "INSERT OR IGNORE INTO seasons(show_id, season_number, title, image_path) VALUES(?, ?, ?, ?)";
		String updateSql = "UPDATE seasons SET image_path = ? WHERE show_id = ? AND title = ?";

		try(PreparedStatement insertStmt = conn.prepareStatement(insertSql);
			PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

			for(int i = 1; i <= numOfSeasons; i++) {
				String imagePath = "";

				if(imagePaths != null && i - 1 < imagePaths.size() && imagePaths.get(i - 1) != null)
					imagePath = imagePaths.get(i - 1);

				insertStmt.setInt(1, showId);
				insertStmt.setInt(2, i);
				insertStmt.setString(3, "Season " + i);
				insertStmt.setString(4, imagePath);
				insertStmt.addBatch();

				updateStmt.setString(1, imagePath);
				updateStmt.setInt(2, showId);
				updateStmt.setString(3, "Season " + i);
				updateStmt.addBatch();
			}

			insertStmt.executeBatch();
			updateStmt.executeBatch();
		}
	}

	public List<Season> getSeasonsByShowId(int showId) throws SQLException {
		List<Season> seasons = new ArrayList<>();

		String sql = "SELECT s.id, s.show_id, s.season_number, s.title, s.image_path, "
				   + "COUNT(e.id) AS total_count, "
				   + "SUM(CASE WHEN er.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count, "
				   + "SUM(CASE WHEN er.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) AS in_progress_count, "
				   + "SUM(CASE WHEN er.status = 'PLANNED' THEN 1 ELSE 0 END) AS planned_count, "
				   + "AVG(er.user_rating) AS avg_rating "
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
					Season season = new Season(
						rs.getInt("id"),
						rs.getInt("show_id"),
						rs.getInt("season_number"),
						rs.getString("title"),
						rs.getString("image_path"),
						rs.getInt("total_count"),
						rs.getInt("completed_count"),
						rs.getInt("in_progress_count"),
						rs.getInt("planned_count"),
						rs.getDouble("avg_rating")
					);

					seasons.add(season);
				}
			}
		}

		return seasons;
	}
}