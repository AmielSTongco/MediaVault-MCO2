package application.model;

public class Song extends Media{
	
	private String album;
	private int yearReleased;
	private int runtimeSeconds;

    public Song(String title,
    			Status status,
    			double userRating,
    			String album,
    			String artist,
    			int yearReleased,
    			int runtimeSeconds,
    			String review)
    			 {
    	
        super(0,
              Type.SONG,
              title,
              artist,
              status,
              userRating,
              review,
              "");
    	
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
    	int minutes = runtimeSeconds/60;
        int seconds = runtimeSeconds%60;

        return String.format("%d:%02d", minutes, seconds);
    }

	public void setSongId(int songId) {
		this.mediaId = songId;
		
	}
	
	private void updateMediaInfo() {
        setMediaInfo("from album \"" + fitToSpace(album, 18)  + "\" of duration " + getRuntimeString());
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
