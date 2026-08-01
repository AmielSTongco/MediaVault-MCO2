package application.view;

import application.model.Media;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public interface MediaTableOwner {

	TableView<Media> getMediaTable();

	TableColumn<Media, Number> getNumberColumn();

	TableColumn<Media, Media> getTitleColumn();

	TableColumn<Media, String> getCreatorColumn();

	TableColumn<Media, String> getYearColumn();

	TableColumn<Media, String> getStatusColumn();

	TableColumn<Media, String> getRatingColumn();

	TableColumn<Media, String> getReviewColumn();

	TableColumn<Media, String> getInfoColumn();
}