package application.model;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaylist {
	
	private int playlistId;
	private String title;
	private List<? extends Media> medias = new ArrayList<>();
	
	// constructor
	public MediaPlaylist(String title, List<? extends Media> medias, int playlistId) {
		this.title = title;
		this.medias = medias;
		this.playlistId = playlistId;
	}
	
	// getters and setters
	public String getTitle() {return title;}
	public List<? extends Media> getMedias() {return medias;}
	public int getPlaylistId() {return playlistId;}
	
	public void setTitle(String title) {this.title = title;}
	public void setMedias(List<Media> medias) {this.medias = medias;}
	public void setPlaylistId(int playlistId) {this.playlistId = playlistId;}
}
