package application.view;

import application.model.MediaPlaylist;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public interface PlaylistTableOwner {

	TableView<MediaPlaylist> getMediaPlaylistTable();

	TableColumn<MediaPlaylist, Number> getNumberColumn();

	TableColumn<MediaPlaylist, MediaPlaylist> getTitleColumn();

	TableColumn<MediaPlaylist, String> getTotalColumn();

	TableColumn<MediaPlaylist, String> getCompletedColumn();

	TableColumn<MediaPlaylist, String> getInProgressColumn();

	TableColumn<MediaPlaylist, String> getPlannedColumn();

	TableColumn<MediaPlaylist, String> getAvgRatingColumn();
}