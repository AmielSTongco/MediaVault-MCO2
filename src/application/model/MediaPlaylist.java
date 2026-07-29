package application.model;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaylist {
	
	private int playlistId;
	private String title;
	private List<? extends Media> medias = new ArrayList<>();
	
	// summary fields calculated by SQLite
	private int totalCount;
    private int completedCount;
    private int inProgressCount;
    private int plannedCount;
    private double avgRating;
	
	// constructor
	public MediaPlaylist(String title, List<? extends Media> medias, int playlistId) {
		this.title = title;
		this.medias = medias;
		this.playlistId = playlistId;
	}
	
	// constructor to be populated by DAO
    public MediaPlaylist(int playlistId, String title, int totalCount, int completedCount, int inProgressCount, int plannedCount, double avgRating) {
        this.playlistId = playlistId;
        this.title = title;
        this.totalCount = totalCount;
        this.completedCount = completedCount;
        this.inProgressCount = inProgressCount;
        this.plannedCount = plannedCount;
        this.avgRating = avgRating;
    }
	
	// getters and setters
	public String getTitle() {return title;}
	public List<? extends Media> getMedias() {return medias;}
	public int getPlaylistId() {return playlistId;}
	public int getTotalCount() {return totalCount;}
    public int getCompletedCount() {return completedCount;}
    public int getInProgressCount() {return inProgressCount;}
    public int getPlannedCount() {return plannedCount;}
    public double getAvgRating() {return avgRating;}
	
	public void setTitle(String title) {this.title = title;}
	public void setMedias(List<Media> medias) {this.medias = medias;}
	public void setPlaylistId(int playlistId) {this.playlistId = playlistId;}
}
