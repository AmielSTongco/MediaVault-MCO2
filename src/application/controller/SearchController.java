package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import application.api.GameAPIClient;
import application.api.ShowAPIClient;
import application.api.SpotifyClient;
import application.dao.MediaPlaylistDAO;
import application.model.Media;
import application.model.MediaPlaylist;
import application.model.Type;
import application.model.UserSession;
import application.view.MediaTableOwner;
import application.view.TableBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SearchController extends BaseMediaPageController implements MediaTableOwner {
	
	/*
	 * Controls the scene which searches for songs, games, and shows
	 * through their corresponding APIs
	 */
	
	@FXML
	private ImageView searchIcon;
	
	@FXML
	private Button searchButton;

	@FXML
	private Button backButton;
	
	@FXML
	private Button homeButton;

	@FXML
	private TextField songName;

	@FXML
	private Label pageLabel;

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

	private static final int ROWS_PER_PAGE = 10;

	private final ObservableList<Media> masterData = FXCollections.observableArrayList();

	private int currentPage;
	private MediaPlaylist playlist;
	private MediaPlaylistDAO mediaPlaylistDAO;

	private final SpotifyClient spotifyClient = new SpotifyClient("266e17b3bb8e432d82b803598192fc5f", "f38ada98c91f4bf9bf6ed4f4490d7b12");
	private final GameAPIClient gameAPIClient = new GameAPIClient("gxsv5i3dj34nlo79kwnhpi2w4bbvup", "4varf8c4a02pp8cofeeqm7ohau7qih");
	private final ShowAPIClient showAPIClient = new ShowAPIClient("eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyNGZkMWUwNDlhMzUyOWU1MmM5YjM2ZTg3OGJjYmM1YiIsIm5iZiI6MTc4NTU4NTMxNS45MTkwMDAxLCJzdWIiOiI2YTZkZGVhMzc3ZDRkNjQ5OGQyNDY5NjYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.vBb_6eSGprrZE9MIpEicSDqih4HRVbttWFN37KKca88");
	
	/**
	 * Initializes shared page elements, navigation buttons, media table, and search listeners.
	 */
	@FXML
	public void initialize() {
		initializeBase();

		// Creates navigation buttons
		makeNavigationButton(backButton, "/resources/application/images/icons/back-reply-svgrepo-com.png", "Back", () -> goBack(playlist));
		makeNavigationButton(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png", "Home", () -> switchScene("/resources/application/fxml/Menu.fxml"));

		initializeNavigationBar();

		TableBuilder.createMediaTable(this);
		handleDoubleClick(mediaTable, this::openAutomaticAdd);

		// Searches through button or enter key
		searchButton.setOnAction(event -> searchMedia());
		songName.setOnAction(event -> searchMedia());

		updateTablePage();
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
	 * Applies media-specific search bar and button themes.
	 *
	 * @param mediaType selected media type
	 */
	@Override
	public void setupView(Type mediaType) {
		super.setupView(mediaType);
		
		searchIcon.setImage(loadImage("/resources/application/images/icons/nav-search-icon.png"));
		
		// Removes previous search themes
		songName.getStyleClass().removeAll("song-search-bar", "game-search-bar", "show-search-bar");
		searchButton.getStyleClass().removeAll("song-search-button", "game-search-button", "show-search-button");

		switch(mediaType)
		{
			case SONG:
				songName.getStyleClass().add("song-search-bar");
				searchButton.getStyleClass().add("song-search-button");
				break;

			case GAME:
				songName.getStyleClass().add("game-search-bar");
				searchButton.getStyleClass().add("game-search-button");
				break;

			case SHOW:
				songName.getStyleClass().add("show-search-bar");
				searchButton.getStyleClass().add("show-search-button");
				break;
		}
	}
	
	/**
	 * Sets playlist where selected media will be added.
	 *
	 * @param playlist selected media playlist
	 */
	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
	}

	/**
	 * Loads table data required by the base controller.
	 */
	@Override
	protected void loadTableData() {
	}

	/**
	 * Searches the corresponding API using entered text.
	 */
	private void searchMedia() {
		String query = songName.getText().trim();

		if(query.isEmpty())
		{
			masterData.clear();
			currentPage = 0;
			updateTablePage();
		}
		else
		{
			try {
				List<? extends Media> results;

				// Searches selected media API
				switch(mediaType)
				{
					case SONG:
						results = spotifyClient.searchTracks(query);
						break;

					case GAME:
						results = gameAPIClient.searchGames(query);
						break;

					case SHOW:
						results = showAPIClient.searchShows(query);
						break;

					default:
						results = new ArrayList<>();
						break;
				}

				if(results == null)
					results = new ArrayList<>();

				masterData.setAll(results);
				currentPage = 0;
				updateTablePage();
			}
			catch(IOException | InterruptedException e) {
				masterData.clear();
				currentPage = 0;
				updateTablePage();
				e.printStackTrace();

				if(e instanceof InterruptedException)
					Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Displays search results belonging to the current page.
	 */
	private void updateTablePage() {
		int fromIndex = currentPage*ROWS_PER_PAGE;
		int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, masterData.size());

		// Resets invalid page index
		if(fromIndex > masterData.size())
		{
			currentPage = 0;
			fromIndex = 0;
			toIndex = Math.min(ROWS_PER_PAGE, masterData.size());
		}

		mediaTable.getItems().setAll(masterData.subList(fromIndex, toIndex));

		if(pageLabel != null)
			pageLabel.setText("Showing " + (toIndex - fromIndex) + " of " + masterData.size());
	}

	/**
	 * Displays previous page of search results.
	 */
	@FXML
	private void handlePreviousPage() {
		if(currentPage > 0)
		{
			currentPage--;
			updateTablePage();
		}
	}

	/**
	 * Displays next page of search results.
	 */
	@FXML
	private void handleNextPage() {
		if((currentPage + 1)*ROWS_PER_PAGE < masterData.size())
		{
			currentPage++;
			updateTablePage();
		}
	}

	/**
	 * Returns to selected playlist.
	 *
	 * @param playlist playlist to reopen
	 */
	private void goBack(MediaPlaylist playlist) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsItemsScene.fxml"));
			Parent root = loader.load();

			MediaPlaylistsItemsController controller = loader.getController();
			controller.setConnection(conn);
			controller.setPlaylist(playlist);
			controller.setupView(mediaType);

			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Opens popup for adding selected API result to the playlist.
	 *
	 * @param media selected search result
	 */
	private void openAutomaticAdd(Media media) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/AddMedia.fxml"));
			StackPane popup = loader.load();

			AddMediaController controller = loader.getController();
			controller.setConnection(conn);
			controller.setPlaylist(playlist);
			controller.setMediaType(mediaType);
			controller.setAutomaticMode(true);
			controller.setMedia(media);
			controller.setCloseAction(() -> rootStackPane.getChildren().remove(popup));
			controller.setSaveAction(savedMedia -> goBack(playlist));

			rootStackPane.getChildren().add(popup);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns media search table.
	 *
	 * @return media search table
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
	 * Returns media release year column.
	 *
	 * @return media release year column
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