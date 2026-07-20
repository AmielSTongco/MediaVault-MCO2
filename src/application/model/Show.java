package application.model;

public class Show extends Media {
	
	private String genre;
	private int yearStart;
	private int yearEnd;
	private int numOfSeasons;
	private int numOfEpisodes;
	private int avgMinsPerEp;
	private boolean airing;
	
	// constructor
	public Show(String title, String creator, int yearStart, int yearEnd, Status status, double userRating, String review, String genre, int numOfSeasons, boolean airing) {
		super(0,
		      Type.GAME,
		      title,
		      creator,
		      status,
		      userRating,
		      review,
		      "");
		
		this.yearStart = yearStart;
		this.yearEnd = yearEnd;
		this.genre = genre;
		this.numOfSeasons = numOfSeasons;
		this.airing = airing;
		
		updateMediaInfo();
		if(airing)
			updateYearString(String.valueOf(yearStart) + " - /--/");
		else
			updateYearString(String.valueOf(yearStart) + " - " + String.valueOf(yearEnd));
	}
	
	// getters and setters
	public int getNumOfSeasons() {return numOfSeasons;}
	public int getYearStart() {return yearStart;}
	public int getYearEnd() {return yearEnd;}
	public int getNumOfEpisodes() {return numOfEpisodes;}
	public int getAvgMinsPerEp() {return avgMinsPerEp;}
	public boolean isAiring() {return airing;}
	public String getGenre() {return genre;}
	
	public void setNumOfSeasons(int numOfSeasons) {this.numOfSeasons=numOfSeasons;}
	public void setNumOfEpisodes(int numOfEpisodes) {this.numOfEpisodes=numOfEpisodes;}
	public void setAvgMinsPerEp(int avgMinsPerEp) {this.avgMinsPerEp=avgMinsPerEp;}
	
	private void updateMediaInfo() {
		if(airing)
			setMediaInfo("still airing, in genre \"" + fitToSpace(this.genre, 18)  + "\" with " + this.numOfSeasons + " seasons");
		else
			setMediaInfo("finished airing, in genre \"" + fitToSpace(this.genre, 18)  + "\" with " + this.numOfSeasons + " seasons");
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