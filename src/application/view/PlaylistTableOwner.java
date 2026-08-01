package application.view;

import application.model.MediaPlaylist;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public interface PlaylistTableOwner<T extends MediaPlaylist> {

	TableView<T> getMediaPlaylistTable();

	TableColumn<T, Number> getNumberColumn();

	TableColumn<T, T> getTitleColumn();

	TableColumn<T, String> getTotalColumn();

	TableColumn<T, String> getCompletedColumn();

	TableColumn<T, String> getInProgressColumn();

	TableColumn<T, String> getPlannedColumn();

	TableColumn<T, String> getAvgRatingColumn();
}