package application.model;

public class Season extends MediaPlaylist {

	private int showId;
	private int seasonNumber;
	private int episodeCount;

	public Season(int seasonId, int showId, int seasonNumber, String title, String imagePath, int episodeCount, int completedCount, int inProgressCount, int plannedCount, double avgRating) {
		super(seasonId, title, imagePath, episodeCount, completedCount, inProgressCount, plannedCount, avgRating);
		this.showId = showId;
		this.seasonNumber = seasonNumber;
		this.episodeCount = episodeCount;
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
	
	public int getEpisodeCount() {
		return episodeCount;
	}
	
	public void setEpisodeCount(int episodeCount) {
		this.episodeCount = episodeCount;
	}
}