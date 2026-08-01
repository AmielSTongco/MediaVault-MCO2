package application.controller;

import java.util.List;

import application.model.Media;
import application.model.MediaPlaylist;
import application.dao.impl.MediaPlaylistDAOImpl;
import application.model.Type;
import application.view.MediaTableOwner;
import application.view.TableBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class MediaPlaylistsItemsController extends BaseMediaPageController implements MediaTableOwner {

	@FXML
	private Button addButton;
	
	@FXML
	private Button searchButton;

	@FXML
	private Button homeButton;

	@FXML
	private Text pageLabel;

	@FXML
	private TableView<Media> mediaTable;

	@FXML
	private TableColumn<Media, Number> numberColumn;

	@FXML
	private TableColumn<Media, Media> titleColumn;

	@FXML
	private TableColumn<Media, String> creatorColumn;

	@FXML
	private TableColumn<Media, String> yearColumn;

	@FXML
	private TableColumn<Media, String> statusColumn;

	@FXML
	private TableColumn<Media, String> ratingColumn;

	@FXML
	private TableColumn<Media, String> reviewColumn;

	@FXML
	private TableColumn<Media, String> infoColumn;

	private MediaPlaylist playlist;
	private MediaPlaylistDAOImpl mediaPlaylistDAO;

	@FXML
	public void initialize() {
		initializeBase();

		makeNavigationButton(
			addButton,
			"/resources/application/images/icons/plus-svgrepo-com.png",
			"Manually Add Media",
			this::openSearch
		);
		
		makeNavigationButton(
			searchButton,
			"/resources/application/images/icons/home-icon-svgrepo-com.png",
			"Search Media",
			this::openSearch
		);

		makeNavigationButton(
			homeButton,
			"/resources/application/images/icons/home-icon-svgrepo-com.png",
			"Home",
			() -> switchScene("/resources/application/fxml/Menu.fxml")
		);

		initializeNavigationBar();

		TableBuilder.createMediaTable(this);

		handleDoubleClick(mediaTable, this::openMedia);
	}

	@Override
	public void setupView(Type mediaType) {
		super.setupView(mediaType);
		loadTableData();
	}

	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;

		if(pageLabel != null && playlist != null)
			pageLabel.setText(playlist.getTitle());

		loadTableData();
	}
	
	@Override
	protected void loadTableData() {
		if(conn != null && mediaType != null && playlist != null) {
			try {
				List<Media> medias = mediaPlaylistDAO.getMediasInPlaylist(playlist.getPlaylistId(), mediaType);
				mediaTable.getItems().setAll(medias);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void openMedia(Media media) {
		if(media != null)
			switchScene("/resources/application/fxml/MediaScene.fxml");
	}

	private void openSearch() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SearchScene.fxml"));
			Parent root = loader.load();

			SearchController controller = loader.getController();
			controller.setConnection(conn);
			controller.setupView(mediaType);

			rootPane.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public TableView<Media> getMediaTable() {
		return mediaTable;
	}

	@Override
	public TableColumn<Media, Number> getNumberColumn() {
		return numberColumn;
	}

	@Override
	public TableColumn<Media, Media> getTitleColumn() {
		return titleColumn;
	}

	@Override
	public TableColumn<Media, String> getCreatorColumn() {
		return creatorColumn;
	}

	@Override
	public TableColumn<Media, String> getYearColumn() {
		return yearColumn;
	}

	@Override
	public TableColumn<Media, String> getStatusColumn() {
		return statusColumn;
	}

	@Override
	public TableColumn<Media, String> getRatingColumn() {
		return ratingColumn;
	}

	@Override
	public TableColumn<Media, String> getReviewColumn() {
		return reviewColumn;
	}

	@Override
	public TableColumn<Media, String> getInfoColumn() {
		return infoColumn;
	}
}