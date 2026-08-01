package application.model;

public class Song extends Media{
	
	private String album;
	private int yearReleased;
	private int runtimeSeconds;

    public Song(String title, Status status, double userRating, String album, String artist, int yearReleased, int runtimeSeconds, String review, String imagePath) {
    	
        super(0, Type.SONG, title, artist, status, userRating, review, "", imagePath);
        this.yearReleased = yearReleased;
    	this.album = album;
        this.runtimeSeconds = runtimeSeconds;
        
        updateMediaInfo();
        updateYearString(String.valueOf(yearReleased));
    }
    
    // getters
    public String getAlbum() {return album;}
    public int getYearReleased() {return yearReleased;}
    public int getRuntimeSeconds() {return runtimeSeconds;}
    public String getRuntimeString() {
    	
    	if(runtimeSeconds == 0)
    		return "/--/";
    	
    	int minutes = runtimeSeconds/60;
        int seconds = runtimeSeconds%60;

        return String.format("%dm %02ds", minutes, seconds);
    }

	public void setSongId(int songId) {
		this.mediaId = songId;
	}
	
	private void updateMediaInfo() {
        setMediaInfo(getRuntimeString() + ", from album \"" + fitToSpace(album, 20)  + "\"");
    }
	
	private void updateYearString(String yearString) {
		if(yearString.equals("0"))
			setYearString("/--/");
		else
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
