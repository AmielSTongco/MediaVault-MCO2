package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import application.dao.MediaPlaylistDAO;
import application.model.Media;
import application.model.MediaPlaylist;
import application.model.Show;
import application.model.Type;
import application.model.UserSession;
import application.view.MediaTableOwner;
import application.view.TableBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MediaPlaylistsItemsController extends BaseMediaPageController implements MediaTableOwner {
	
	/*
	 * Controls the scene which displays and manages
	 * media entries inside a selected playlist
	 */

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
	private MediaPlaylistDAO mediaPlaylistDAO;
	private boolean reorderingEnabled;
	
	/**
	 * Initializes shared page elements, navigation buttons, media table, and listeners.
	 */
	@FXML
	public void initialize() {
		initializeBase();
		
		// Creates navigation buttons
		makeNavigationButton(backButton, "/resources/application/images/icons/back-reply-svgrepo-com.png", "Back", this::goBack);
		makeNavigationButton(addButton, "/resources/application/images/icons/plus-svgrepo-com.png", "Manually Add Media", this::manuallyAddMedia);
		makeNavigationButton(searchButton, "/resources/application/images/icons/nav-search-icon.png", "Search Media", this::openSearch);
		makeNavigationButton(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png", "Home", () -> switchScene("/resources/application/fxml/Menu.fxml"));

		initializeNavigationBar();

		TableBuilder.createMediaTable(this);
		handleDoubleClick(mediaTable, this::openMedia);
	}
	
	/**
	 * Opens popup for manually adding media.
	 */
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
	
	/**
	 * Permanently deletes current custom playlist.
	 */
	public void deletePlaylist() {
		if(playlist != null && mediaType != null)
		{
			boolean defaultPlaylist = playlist.getTitle().equals("all_songs") && mediaType == Type.SONG ||
					playlist.getTitle().equals("all_games") && mediaType == Type.GAME ||
					playlist.getTitle().equals("all_shows") && mediaType == Type.SHOW;

			// Prevents deletion of default playlists
			if(!defaultPlaylist)
			{
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
	
	/**
	 * Returns to media playlist table.
	 */
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
	
	/**
	 * Applies media-specific theme, playlist image, and controls.
	 *
	 * @param mediaType selected media type
	 */
	@Override
	public void setupView(Type mediaType) {
		super.setupView(mediaType);

		if(playlist != null)
		{
			String imagePath = playlist.getImagePath();

			// Uses default playlist image when needed
			if(imagePath == null || imagePath.isBlank())
			{
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
	
	/**
	 * Sets database connection and initializes playlist data access.
	 *
	 * @param conn active database connection
	 */
	@Override
	public void setConnection(Connection conn) {
		mediaPlaylistDAO = new MediaPlaylistDAO(conn, UserSession.getCurrentUserId());
		super.setConnection(conn);
	}
	
	/**
	 * Sets current playlist and updates page details.
	 *
	 * @param playlist selected media playlist
	 */
	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;

		// Formats default playlist title
		if(pageLabel != null && playlist != null)
		{
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
	
	/**
	 * Configures playlist delete button visibility and behavior.
	 */
	private void setupDeleteButton() {
		if(playlist != null && mediaType != null && deletePlaylistButton != null)
		{
			boolean defaultPlaylist = playlist.getTitle().equals("all_songs") && mediaType == Type.SONG ||
					playlist.getTitle().equals("all_games") && mediaType == Type.GAME ||
					playlist.getTitle().equals("all_shows") && mediaType == Type.SHOW;

			// Hides delete button for default playlists
			if(defaultPlaylist)
			{
				deletePlaylistButton.setVisible(false);
				deletePlaylistButton.setManaged(false);
				deletePlaylistButton.setDisable(true);
			}
			else
			{
				deletePlaylistButton.setVisible(true);
				deletePlaylistButton.setManaged(true);
				deletePlaylistButton.setDisable(false);

				makeNavigationButton(deletePlaylistButton, "/resources/application/images/icons/delete-icon.png", "Delete Playlist", this::deletePlaylist);
			}

			initializeNavigationBar();
		}
	}
	
	/**
	 * Loads media entries from current playlist.
	 */
	@Override
	protected void loadTableData() {
		if(mediaPlaylistDAO != null && mediaType != null && playlist != null)
		{
			try {
				List<Media> media = mediaPlaylistDAO.getMediasInPlaylist(playlist.getPlaylistId(), mediaType);
				mediaTable.getItems().setAll(media);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Opens selected media based on its media type.
	 *
	 * @param media selected media
	 */
	private void openMedia(Media media) {
		if(media != null)
		{
			if(media instanceof Show)
				openSeasons((Show)media);
			else if(mediaType == Type.SONG)
				openMediaDetails(media, "/resources/application/fxml/SongsScene.fxml");
			else if(mediaType == Type.GAME)
				openMediaDetails(media, "/resources/application/fxml/GamesScene.fxml");
		}
	}
	
	/**
	 * Opens detail scene for selected song or game.
	 *
	 * @param media selected media
	 * @param fxmlPath target details scene
	 */
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
	
	/**
	 * Opens season table for selected show.
	 *
	 * @param show selected show
	 */
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
	
	/**
	 * Opens media search scene.
	 */
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
	
	/**
	 * Enables show reordering inside show playlists.
	 */
	private void setupMediaReordering() {
		if(!reorderingEnabled && mediaTable != null && playlist != null && mediaType == Type.SHOW)
		{
			TableBuilder.enableRowReordering(mediaTable, media -> media instanceof Show, this::saveMediaOrder);
			reorderingEnabled = true;
		}
	}
	
	/**
	 * Saves current show order inside selected playlist.
	 */
	private void saveMediaOrder() {
		if(mediaPlaylistDAO != null && playlist != null && mediaType == Type.SHOW)
		{
			try {
				mediaPlaylistDAO.updateMediaOrder(playlist.getPlaylistId(), mediaTable.getItems(), mediaType);
			}
			catch(Exception e) {
				e.printStackTrace();
				loadTableData();
			}
		}
	}
	
	/**
	 * Returns media table.
	 *
	 * @return media table
	 */
	@Override
	public TableView<Media> getMediaTable() {
		return mediaTable;
	}
	
	/**
	 * Returns media number column.
	 *
	 * @return media number column
	 */
	@Override
	public TableColumn<Media, Number> getNumberColumn() {
		return numberColumn;
	}
	
	/**
	 * Returns media title column.
	 *
	 * @return media title column
	 */
	@Override
	public TableColumn<Media, Media> getTitleColumn() {
		return titleColumn;
	}
	
	/**
	 * Returns media creator column.
	 *
	 * @return media creator column
	 */
	@Override
	public TableColumn<Media, String> getCreatorColumn() {
		return creatorColumn;
	}
	
	/**
	 * Returns media year column.
	 *
	 * @return media year column
	 */
	@Override
	public TableColumn<Media, String> getYearColumn() {
		return yearColumn;
	}
	
	/**
	 * Returns media status column.
	 *
	 * @return media status column
	 */
	@Override
	public TableColumn<Media, String> getStatusColumn() {
		return statusColumn;
	}
	
	/**
	 * Returns media rating column.
	 *
	 * @return media rating column
	 */
	@Override
	public TableColumn<Media, String> getRatingColumn() {
		return ratingColumn;
	}
	
	/**
	 * Returns media review column.
	 *
	 * @return media review column
	 */
	@Override
	public TableColumn<Media, String> getReviewColumn() {
		return reviewColumn;
	}
	
	/**
	 * Returns media information column.
	 *
	 * @return media information column
	 */
	@Override
	public TableColumn<Media, String> getInfoColumn() {
		return infoColumn;
	}
}