package application.model;

public class Episode extends Media {

	private int episodeId;
	private int seasonId;
	private int episodeNumber;
	private int seasonNumber;
	private int yearReleased;
	private String writer;

	public Episode(int episodeId, int seasonId, int episodeNumber, String title, String writer, int yearReleased, Status status, double userRating, String review, String imagePath) {
		super(title, writer, yearReleased, status == null ? Status.PLANNED : status, userRating, review == null ? "" : review, imagePath);

		this.seasonId = seasonId;
		this.episodeNumber = episodeNumber;
		this.yearReleased = yearReleased;
		this.writer = writer == null || writer.isBlank() ? "/--/" : writer;

		setEpisodeId(episodeId);
		setCreator(this.writer);

		if(yearReleased > 0)
			setYearString(String.valueOf(yearReleased));
		else
			setYearString("/--/");

		setMediaInfo("Episode " + episodeNumber);
	}

	public Episode(int episodeNumber, String title, String writer, String imagePath) {
		this(0, 0, episodeNumber, title, writer, 0, Status.PLANNED, 0.0, "", imagePath);
	}

	public Episode(int episodeNumber, String title, String imagePath) {
		this(0, 0, episodeNumber, title, "/--/", 0, Status.PLANNED, 0.0, "", imagePath);
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

	public int getSeasonNumber() {
		return seasonNumber;
	}

	public void setSeasonNumber(int seasonNumber) {
		this.seasonNumber = seasonNumber;
	}

	public int getYearReleased() {
		return yearReleased;
	}

	public void setYearReleased(int yearReleased) {
		this.yearReleased = yearReleased;

		if(yearReleased > 0)
			setYearString(String.valueOf(yearReleased));
		else
			setYearString("/--/");
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