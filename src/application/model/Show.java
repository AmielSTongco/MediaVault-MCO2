package application.model;
import java.util.List;
import java.util.ArrayList;

public class Show extends Media {
	
	private String genre;
	private int yearStart;
	private int yearEnd;
	private int numOfSeasons;
	private int numOfEpisodes;
	private int avgMinsPerEp;
	private boolean airing;
	private int apiId;
	private List<String> seasonImagePaths = new ArrayList<>();
	private List<List<String>> episodeImagePaths = new ArrayList<>();
	
	// constructor
	public Show(String title, String creator, int yearStart, int yearEnd, Status status, double userRating, String review, String genre, int numOfSeasons, boolean airing, String imagePath) {
		super(0,
		      Type.SHOW,
		      title,
		      creator,
		      status,
		      userRating,
		      review,
		      "", imagePath);
		
		this.yearStart = yearStart;
		this.yearEnd = yearEnd;
		this.genre = genre;
		this.numOfSeasons = numOfSeasons;
		this.airing = airing;
		
		updateMediaInfo();
		if(airing)
		{
			if(yearStart != 0)
				updateYearString(String.valueOf(yearStart) + " - /--/");
			else
				updateYearString("/--/" + " - /--/");
		}
		else
		{
			if(yearStart!=0 && yearEnd != 0)
				updateYearString(String.valueOf(yearStart) + " - " + String.valueOf(yearEnd));
			else if(yearStart!=0 && yearEnd == 0)
				updateYearString(String.valueOf(yearStart) + " - /--/");
			else
				updateYearString("/--/" + " - /--/");
		}
	}
	
	// getters and setters
	public int getNumOfSeasons() {return numOfSeasons;}
	public int getYearStart() {return yearStart;}
	public int getYearEnd() {return yearEnd;}
	public int getNumOfEpisodes() {return numOfEpisodes;}
	public int getAvgMinsPerEp() {return avgMinsPerEp;}
	public boolean isAiring() {return airing;}
	public String getGenre() {return genre;}
	public List<String> getSeasonImagePaths() {
		return seasonImagePaths;
	}
	public int getApiId() {
		return apiId;
	}
	public List<List<String>> getEpisodeImagePaths() {
		return episodeImagePaths;
	}
	
	
	public List<String> getEpisodeImagePaths(int seasonNumber) {
		if(seasonNumber <= 0 || seasonNumber > episodeImagePaths.size())
			return new ArrayList<>();

		return episodeImagePaths.get(seasonNumber - 1);
	}
	
	
	public void setNumOfSeasons(int numOfSeasons) {this.numOfSeasons=numOfSeasons;}
	public void setNumOfEpisodes(int numOfEpisodes) {this.numOfEpisodes=numOfEpisodes;}
	public void setAvgMinsPerEp(int avgMinsPerEp) {this.avgMinsPerEp=avgMinsPerEp;}
	public void setSeasonImagePaths(List<String> seasonImagePaths) {
		if(seasonImagePaths == null)
			this.seasonImagePaths = new ArrayList<>();
		else
			this.seasonImagePaths = seasonImagePaths;
	}

	public void setApiId(int apiId) {
		this.apiId = apiId;
	}
	public void setEpisodeImagePaths(List<List<String>> episodeImagePaths) {
		if(episodeImagePaths == null)
			this.episodeImagePaths = new ArrayList<>();
		else
			this.episodeImagePaths = episodeImagePaths;
	}

	
	private void updateMediaInfo() {
		if(airing)
			setMediaInfo("airing, in genre \"" + fitToSpace(this.genre, 18)  + "\" with " + this.numOfSeasons + " seasons");
		else
			setMediaInfo("not airing, in genre \"" + fitToSpace(this.genre, 18)  + "\" with " + this.numOfSeasons + " seasons");
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