package application.controller;

import java.util.List;

import application.model.Media;
import application.model.Show;
import application.model.MediaPlaylist;
import application.dao.impl.MediaPlaylistDAOImpl;
import application.model.Type;
import application.view.MediaTableOwner;
import application.controller.MediaPlaylistsController;
import application.view.TableBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import application.model.UserSession;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MediaPlaylistsItemsController extends BaseMediaPageController implements MediaTableOwner {

	@FXML
	private Button backButton;
	
	@FXML
	private Button addButton;
	
	@FXML
	private Button searchButton;
	
	@FXML
	private Button deletePlaylistButton;

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
	private boolean reorderingEnabled;

	@FXML
	public void initialize() {
		initializeBase();

		makeNavigationButton(
			backButton,
			"/resources/application/images/icons/back-reply-svgrepo-com.png",
			"Back",
			() -> goBack()
		);
		
		makeNavigationButton(
			addButton,
			"/resources/application/images/icons/plus-svgrepo-com.png",
			"Manually Add Media",
			this::manuallyAddMedia
		);
		
		makeNavigationButton(
			searchButton,
			"/resources/application/images/icons/nav-search-icon.png",
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
		TableBuilder.enableRowReordering(
				mediaTable,
				media -> true,
				this::saveMediaOrder
			);

		handleDoubleClick(mediaTable, this::openMedia);
	}
	
	private void manuallyAddMedia() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/AddMedia.fxml"));
			StackPane popup = loader.load();

			AddMediaController controller = loader.getController();
			controller.setConnection(conn);
			controller.setPlaylist(playlist);
			controller.setMediaType(mediaType);
			controller.setAutomaticMode(false);
			controller.setCloseAction(() -> rootStackPane.getChildren().remove(popup));
			controller.setSaveAction(savedMedia -> loadTableData());

			rootStackPane.getChildren().add(popup);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deletePlaylist() {
		if(playlist != null && mediaType != null) {
			boolean defaultPlaylist =
				playlist.getTitle().equals("all_songs") && mediaType == Type.SONG ||
				playlist.getTitle().equals("all_games") && mediaType == Type.GAME ||
				playlist.getTitle().equals("all_shows") && mediaType == Type.SHOW;

			if(!defaultPlaylist) {
				try {
					mediaPlaylistDAO.deletePlaylist(playlist.getPlaylistId(), mediaType.getTitle());
					goBack();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void goBack() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsScene.fxml"));
			Parent root = loader.load();

			MediaPlaylistsController controller = loader.getController();
			controller.setConnection(conn);
			controller.setupView(mediaType);

			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setupView(Type mediaType) {
		super.setupView(mediaType);

		if(playlist != null) {
			String imagePath = playlist.getImagePath();

			if(imagePath == null || imagePath.isBlank()) {
				if(mediaType == Type.SONG)
					imagePath = "/resources/application/images/icons/default-song-playlist-icon.png";
				else if(mediaType == Type.GAME)
					imagePath = "/resources/application/images/icons/default-game-playlist-icon.png";
				else if(mediaType == Type.SHOW)
					imagePath = "/resources/application/images/icons/default-show-playlist-icon.png";
			}

			mediaLogo.setImage(loadImage(imagePath));
			cropImage(mediaLogo);
		}

		setupDeleteButton();
		loadTableData();
		setupMediaReordering();
	}
	
	@Override
	public void setConnection(Connection conn) {
		mediaPlaylistDAO = new MediaPlaylistDAOImpl(conn, UserSession.getCurrentUserId());
		super.setConnection(conn);
	}

	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;

		if(pageLabel != null && playlist != null) {
			String title = playlist.getTitle();

			if(title.equals("all_songs"))
				pageLabel.setText("All Songs");
			else if(title.equals("all_games"))
				pageLabel.setText("All Games");
			else if(title.equals("all_shows"))
				pageLabel.setText("All Shows");
			else
				pageLabel.setText(playlist.getTitle());
		}

		setupDeleteButton();
		loadTableData();
		setupMediaReordering();
	}
	
	private void setupDeleteButton() {
		if(playlist != null && mediaType != null && deletePlaylistButton != null) {
			boolean defaultPlaylist =
				playlist.getTitle().equals("all_songs") && mediaType == Type.SONG ||
				playlist.getTitle().equals("all_games") && mediaType == Type.GAME ||
				playlist.getTitle().equals("all_shows") && mediaType == Type.SHOW;

			if(defaultPlaylist) {
				deletePlaylistButton.setVisible(false);
				deletePlaylistButton.setManaged(false);
				deletePlaylistButton.setDisable(true);
			}
			else {
				deletePlaylistButton.setVisible(true);
				deletePlaylistButton.setManaged(true);
				deletePlaylistButton.setDisable(false);

				makeNavigationButton(
					deletePlaylistButton,
					"/resources/application/images/icons/delete-icon.png",
					"Delete Playlist",
					this::deletePlaylist
				);
			}

			initializeNavigationBar();
		}
	}
	
	@Override
	protected void loadTableData() {
		if(mediaPlaylistDAO != null && mediaType != null && playlist != null) {
			try {
				List<Media> media = mediaPlaylistDAO.getMediasInPlaylist(playlist.getPlaylistId(), mediaType);
				mediaTable.getItems().setAll(media);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void openMedia(Media media) {
		if(media != null) {
			if(media instanceof Show)
				openSeasons((Show)media);
			else if(mediaType == Type.SONG)
				openMediaDetails(media, "/resources/application/fxml/SongsScene.fxml");
			else if(mediaType == Type.GAME)
				openMediaDetails(media, "/resources/application/fxml/GamesScene.fxml");
		}
	}
	
	private void openMediaDetails(Media media, String fxmlPath) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
			Parent root = loader.load();

			MediaController controller = loader.getController();
			controller.setConnection(conn);
			controller.setPlaylist(playlist);
			controller.setMedia(media);
			controller.setupView(mediaType);

			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void openSeasons(Show show) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SeasonsTableScene.fxml"));
			Parent root = loader.load();

			SeasonsTableController controller = loader.getController();
			controller.setConnection(conn);
			controller.setPlaylist(playlist);
			controller.setShow(show);
			controller.setupView(Type.SHOW);

			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void openSearch() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SearchScene.fxml"));
			Parent root = loader.load();

			SearchController controller = loader.getController();
			controller.setConnection(conn);
			controller.setupView(mediaType);
			controller.setPlaylist(playlist);

			rootPane.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setupMediaReordering() {
		if(!reorderingEnabled && mediaTable != null && playlist != null && mediaType == Type.SHOW) {
			TableBuilder.enableRowReordering(
				mediaTable,
				media -> media instanceof Show,
				this::saveMediaOrder
			);

			reorderingEnabled = true;
		}
	}

	private void saveMediaOrder() {
		if(mediaPlaylistDAO != null && playlist != null && mediaType == Type.SHOW) {
			try {
				mediaPlaylistDAO.updateMediaOrder(
					playlist.getPlaylistId(),
					mediaTable.getItems(),
					mediaType
				);
			}
			catch(Exception e) {
				e.printStackTrace();
				loadTableData();
			}
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