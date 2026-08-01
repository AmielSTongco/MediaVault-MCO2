package application.model;

public class Episode {

	private int episodeId;
	private int seasonId;
	private int episodeNumber;
	private String title;
	private String imagePath;
	private Status status;
	private double userRating;
	private String review;

	public Episode(int episodeId, int seasonId, int episodeNumber, String title, String imagePath, Status status, double userRating, String review) {
		this.episodeId = episodeId;
		this.seasonId = seasonId;
		this.episodeNumber = episodeNumber;
		this.title = title;
		this.imagePath = imagePath;
		this.status = status;
		this.userRating = userRating;
		this.review = review;
	}

	public Episode(int episodeNumber, String title, String imagePath) {
		this(0, 0, episodeNumber, title, imagePath, null, 0.0, "");
	}

	public int getEpisodeId() {
		return episodeId;
	}

	public void setEpisodeId(int episodeId) {
		this.episodeId = episodeId;
	}

	public int getSeasonId() {
		return seasonId;
	}

	public void setSeasonId(int seasonId) {
		this.seasonId = seasonId;
	}

	public int getEpisodeNumber() {
		return episodeNumber;
	}

	public String getTitle() {
		return title;
	}

	public String getImagePath() {
		return imagePath;
	}

	public Status getStatus() {
		return status;
	}

	public double getUserRating() {
		return userRating;
	}

	public String getReview() {
		if(review == null || review.isBlank())
			return "/--/";

		return review;
	}

	public String getStatusString() {
		if(status == null)
			return "/--/";

		return status.toDbString();
	}

	public String getRatingString() {
		if(userRating <= 0)
			return "/--/";

		return String.format("%.1f", userRating);
	}
}