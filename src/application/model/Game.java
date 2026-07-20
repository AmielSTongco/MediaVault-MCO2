package application.model;

public class Game extends Media{
	
	private String genre;
	private int yearReleased;
	private int avgPlaytimeMins;
	
	public Game(String title, String creator, int yearReleased, Status status, double userRating, String review, String genre, int avgPlaytimeMins) {
		super(0,
	          Type.GAME,
	          title,
	          creator,
	          status,
	          userRating,
	          review,
	          "");
		
		this.yearReleased = yearReleased;
		this.genre = genre;
		this.avgPlaytimeMins = avgPlaytimeMins;
		
		updateMediaInfo();
        updateYearString(String.valueOf(yearReleased));
	}
	
	// getters and setters
	public String getGenre() {return genre;}
	public int getAvgPlaytimeMins() {return avgPlaytimeMins;}
	public int getYearReleased() {return yearReleased;}
	public void setAvgPlaytimeMins(int avgPlaytimeMins) {this.avgPlaytimeMins = avgPlaytimeMins;}
	
	private void updateMediaInfo() {
        setMediaInfo("in genre \"" + fitToSpace(this.genre, 18)  + "\" of average playtime " + this.avgPlaytimeMins + " mins");
    }
	
	private void updateYearString(String yearString) {
		setYearString(yearString);
	}
	
	private static String fitToSpace(String text, int width) {
	    if (text == null) {
	        return "";
	    }

	    if (text.length() <= width) {
	        return text;
	    }

	    return text.substring(0, width - 3) + "...";
	}
}