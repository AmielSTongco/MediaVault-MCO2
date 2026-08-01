package application.model;

public class Season extends MediaPlaylist {

	private int showId;
	private int seasonNumber;

	public Season(int seasonId, int showId, String title, String imagePath, int totalCount, int completedCount, int inProgressCount, int plannedCount, double avgRating) {
		super(seasonId, title, imagePath, totalCount, completedCount, inProgressCount, plannedCount, avgRating);
		this.showId = showId;
	}

	public int getShowId() {
		return showId;
	}

	public void setShowId(int showId) {
		this.showId = showId;
	}

	public int getSeasonNumber() {
		return seasonNumber;
	}

	public void setSeasonNumber(int seasonNumber) {
		this.seasonNumber = seasonNumber;
	}
}