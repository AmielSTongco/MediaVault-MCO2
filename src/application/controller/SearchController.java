package application.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.api.SpotifyClient;
import application.model.Media;
import application.model.Song;
import application.model.Type;
import application.view.MediaTableOwner;
import application.view.TableBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class SearchController extends BaseMediaPageController implements MediaTableOwner {

	@FXML
	private Button searchButton;

	@FXML
	private Button backButton;

	@FXML
	private Button playlistsButton;

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

	private final SpotifyClient spotifyClient = new SpotifyClient(
		"266e17b3bb8e432d82b803598192fc5f",
		"f38ada98c91f4bf9bf6ed4f4490d7b12"
	);

	@FXML
	public void initialize() {
		initializeBase();

		makeNavigationButton(
			backButton,
			"/resources/application/images/icons/back-reply-svgrepo-com.png",
			"Back",
			this::goBack
		);

		makeNavigationButton(
			playlistsButton,
			"/resources/application/images/icons/stack-overflow-svgrepo-com.png",
			"Playlists",
			this::openPlaylists
		);

		initializeNavigationBar();

		TableBuilder.createMediaTable(this);

		handleDoubleClick(mediaTable, this::openMedia);

		searchButton.setOnAction(event -> searchMedia());
		songName.setOnAction(event -> searchMedia());

		updateTablePage();
	}

	@Override
	public void setupView(Type mediaType) {
		super.setupView(mediaType);
		
		songName.getStyleClass().removeAll("song-search-bar", "game-search-bar", "show-search-bar");
		searchButton.getStyleClass().removeAll("song-search-button", "game-search-button", "show-search-button");

		switch(mediaType) {
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

	@Override
	protected void loadTableData() {
		
	}

	private void searchMedia() {
		String query = songName.getText().trim();

		if(query.isEmpty()) {
			masterData.clear();
			currentPage = 0;
			updateTablePage();
		}
		else {
			try {
				List<Song> results = spotifyClient.searchTracks(query);

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

	private void updateTablePage() {
		int fromIndex = currentPage*ROWS_PER_PAGE;
		int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, masterData.size());

		if(fromIndex > masterData.size()) {
			currentPage = 0;
			fromIndex = 0;
			toIndex = Math.min(ROWS_PER_PAGE, masterData.size());
		}

		mediaTable.getItems().setAll(masterData.subList(fromIndex, toIndex));

		if(pageLabel != null)
			pageLabel.setText("Showing " + (toIndex - fromIndex) + " of " + masterData.size());
	}

	@FXML
	private void handlePreviousPage() {
		if(currentPage > 0) {
			currentPage--;
			updateTablePage();
		}
	}

	@FXML
	private void handleNextPage() {
		if((currentPage + 1)*ROWS_PER_PAGE < masterData.size()) {
			currentPage++;
			updateTablePage();
		}
	}

	private void openMedia(Media media) {
		if(media != null)
			switchScene("/resources/application/fxml/MediaScene.fxml");
	}

	private void goBack() {
		switchScene("/resources/application/fxml/MediaScene.fxml");
	}

	private void openPlaylists() {
		switchScene("/resources/application/fxml/MediaPlaylistsScene.fxml");
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