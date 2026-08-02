package application.model;

public class Episode extends Media {

	private int episodeId;
	private int seasonId;
	private int episodeNumber;
	private String writer;

	public Episode(int episodeId, int seasonId, int episodeNumber, String title, String writer, Status status, double userRating, String review, String imagePath) {
		super(title, writer, 0, status == null ? Status.PLANNED : status, userRating, review == null ? "" : review, imagePath);

		this.episodeId = episodeId;
		this.seasonId = seasonId;
		this.episodeNumber = episodeNumber;
		this.writer = writer == null || writer.isBlank() ? "/--/" : writer;

		setCreator(this.writer);
		setYearString("/--/");
		setMediaInfo("Episode " + episodeNumber);
	}

	public Episode(int episodeNumber, String title, String writer, String imagePath) {
		this(0, 0, episodeNumber, title, writer, Status.PLANNED, 0.0, "", imagePath);
	}

	public Episode(int episodeNumber, String title, String imagePath) {
		this(0, 0, episodeNumber, title, "/--/", Status.PLANNED, 0.0, "", imagePath);
	}

	public int getEpisodeId() {
		return episodeId;
	}

	public void setEpisodeId(int episodeId) {
		this.episodeId = episodeId;
		setMediaId(episodeId);
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

	public void setEpisodeNumber(int episodeNumber) {
		this.episodeNumber = episodeNumber;
		setMediaInfo("Episode " + episodeNumber);
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		if(writer == null || writer.isBlank())
			this.writer = "/--/";
		else
			this.writer = writer;

		setCreator(this.writer);
	}
}