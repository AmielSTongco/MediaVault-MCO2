package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.api.ShowAPIClient;
import application.dao.impl.EpisodeDAOImpl;
import application.model.Episode;
import application.model.Season;
import application.model.Show;
import application.model.UserSession;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

public class EpisodesController extends BaseMediaPageController {

	@FXML
	private Button backButton;

	@FXML
	private Button homeButton;

	@FXML
	private Text pageLabel;

	@FXML
	private ImageView mediaLogo;

	@FXML
	private Label loadingLabel;

	@FXML
	private TableView<Episode> episodesTable;

	@FXML
	private TableColumn<Episode, Number> numberColumn;

	@FXML
	private TableColumn<Episode, String> titleColumn;

	@FXML
	private TableColumn<Episode, String> statusColumn;

	@FXML
	private TableColumn<Episode, String> ratingColumn;

	@FXML
	private TableColumn<Episode, String> reviewColumn;

	private final ObservableList<Episode> episodes = FXCollections.observableArrayList();

	private final ShowAPIClient showAPIClient = new ShowAPIClient(
		"PUT_YOUR_TMDB_ACCESS_TOKEN_HERE"
	);

	private EpisodeDAOImpl episodeDAO;
	private Show show;
	private Season season;

	@FXML
	public void initialize() {
		initializeBase();
		setupEpisodeTable();
		episodesTable.setItems(episodes);

		makeNavigationButton(
			backButton,
			"/resources/application/images/icons/back-reply-svgrepo-com.png",
			"Back",
			this::goBack
		);

		makeNavigationButton(
			homeButton,
			"/resources/application/images/icons/home-icon-svgrepo-com.png",
			"Home",
			() -> switchScene("/resources/application/fxml/Menu.fxml")
		);

		initializeNavigationBar();

		if(loadingLabel != null) {
			loadingLabel.setVisible(false);
			loadingLabel.setManaged(false);
		}
	}

	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		episodeDAO = new EpisodeDAOImpl(conn, UserSession.getCurrentUserId());

		if(show != null && season != null)
			loadEpisodes();
	}

	public void setShow(Show show) {
		this.show = show;
		updateHeader();

		if(episodeDAO != null && season != null)
			loadEpisodes();
	}

	public void setSeason(Season season) {
		this.season = season;
		updateHeader();

		if(episodeDAO != null && show != null)
			loadEpisodes();
	}

	private void updateHeader() {
		if(pageLabel != null && show != null && season != null)
			pageLabel.setText(show.getTitle() + " - " + season.getTitle());

		if(mediaLogo != null && season != null) {
			Image image = loadImage(season.getImagePath());

			if(image == null && show != null)
				image = loadImage(show.getImagePath());

			if(image != null) {
				mediaLogo.setImage(image);
				cropImage(mediaLogo);
			}
		}
	}

	private void setupEpisodeTable() {
		numberColumn.setCellValueFactory(cellData ->
			new ReadOnlyIntegerWrapper(cellData.getValue().getEpisodeNumber())
		);

		titleColumn.setCellValueFactory(cellData ->
			new SimpleStringProperty(cellData.getValue().getTitle())
		);

		statusColumn.setCellValueFactory(cellData ->
			new SimpleStringProperty(cellData.getValue().getStatusString())
		);

		ratingColumn.setCellValueFactory(cellData ->
			new SimpleStringProperty(cellData.getValue().getRatingString())
		);

		reviewColumn.setCellValueFactory(cellData ->
			new SimpleStringProperty(cellData.getValue().getReview())
		);
	}

	private void loadEpisodes() {
		System.out.println("SHOW: " + show);
		System.out.println("SEASON: " + season);
		
		if(show != null && season != null && episodeDAO != null) {
			setLoading(true);

			Task<List<Episode>> task = new Task<List<Episode>>() {
				@Override
				protected List<Episode> call() throws Exception {
					List<Episode> savedEpisodes = episodeDAO.getEpisodesBySeasonId(season.getPlaylistId());

					if(savedEpisodes.isEmpty() && show.getApiId() > 0) {
						List<Episode> downloadedEpisodes = showAPIClient.getEpisodes(show.getApiId(), season.getSeasonNumber());
						episodeDAO.addEpisodes(season.getPlaylistId(), downloadedEpisodes);
						savedEpisodes = episodeDAO.getEpisodesBySeasonId(season.getPlaylistId());
					}

					return savedEpisodes;
				}
			};

			task.setOnSucceeded(event -> {
				episodes.setAll(task.getValue());
				setLoading(false);
			});

			task.setOnFailed(event -> {
				episodes.clear();
				setLoading(false);

				Throwable error = task.getException();

				if(error instanceof InterruptedException)
					Thread.currentThread().interrupt();

				if(error != null)
					error.printStackTrace();
			});

			Thread thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();
		}
	}

	private void setLoading(boolean loading) {
		episodesTable.setDisable(loading);

		if(loadingLabel != null) {
			loadingLabel.setText("Loading episodes...");
			loadingLabel.setVisible(loading);
			loadingLabel.setManaged(loading);
		}
	}

	private void goBack() {
		if(show != null) {
			try {
				javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/application/fxml/SeasonsScene.fxml"));
				javafx.scene.Parent root = loader.load();

				SeasonsController controller = loader.getController();
				controller.setConnection(conn);
				controller.setShow(show);
				controller.setupView(application.model.Type.SHOW);

				javafx.stage.Stage stage = (javafx.stage.Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void loadTableData() {
		loadEpisodes();
	}
}