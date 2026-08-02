package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import application.api.ShowAPIClient;
import application.dao.impl.EpisodeDAOImpl;
import application.model.Episode;
import application.model.Media;
import application.model.Season;
import application.model.Show;
import application.model.Type;
import application.model.UserSession;
import application.view.MediaTableOwner;
import application.view.TableBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class EpisodesController extends BaseMediaPageController implements MediaTableOwner {

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

	private final ObservableList<Media> episodes = FXCollections.observableArrayList();

	private final ShowAPIClient showAPIClient = new ShowAPIClient(
		"eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyNGZkMWUwNDlhMzUyOWU1MmM5YjM2ZTg3OGJjYmM1YiIsIm5iZiI6MTc4NTU4NTMxNS45MTkwMDAxLCJzdWIiOiI2YTZkZGVhMzc3ZDRkNjQ5OGQyNDY5NjYiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.vBb_6eSGprrZE9MIpEicSDqih4HRVbttWFN37KKca88"
	);

	private EpisodeDAOImpl episodeDAO;
	private Show show;
	private Season season;
	private boolean loadingEpisodes;

	@FXML
	public void initialize() {
		initializeBase();
		setupView(Type.SHOW);

		TableBuilder.createMediaTable(this);
		mediaTable.setItems(episodes);

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
		tryLoadEpisodes();
	}

	public void setShow(Show show) {
		this.show = show;
		updateHeader();
		tryLoadEpisodes();
	}

	public void setSeason(Season season) {
		this.season = season;
		updateHeader();
		tryLoadEpisodes();
	}

	private void tryLoadEpisodes() {
		if(show != null && season != null && episodeDAO != null && !loadingEpisodes)
			loadEpisodes();
	}

	private void updateHeader() {
		if(pageLabel != null && show != null && season != null) {
			String showTitle = show.getTitle();
			int maxLength = 12;

			if(showTitle.length() > maxLength)
				showTitle = showTitle.substring(0, maxLength).trim() + "...";

			pageLabel.setText(showTitle + " - " + season.getTitle());
		}

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

	private void loadEpisodes() {
		loadingEpisodes = true;
		setLoading(true);

		Task<List<Episode>> task = new Task<List<Episode>>() {
			@Override
			protected List<Episode> call() throws Exception {
				List<Episode> savedEpisodes = episodeDAO.getEpisodesBySeasonId(season.getPlaylistId());

				if(savedEpisodes.isEmpty() && show.getApiId() > 0 && season.getSeasonNumber() > 0) {
					List<Episode> downloadedEpisodes = showAPIClient.getEpisodes(show.getApiId(), season.getSeasonNumber());
					episodeDAO.addEpisodes(season.getPlaylistId(), downloadedEpisodes);
					savedEpisodes = episodeDAO.getEpisodesBySeasonId(season.getPlaylistId());
				}

				return savedEpisodes;
			}
		};

		task.setOnSucceeded(event -> {
			episodes.setAll(task.getValue());
			mediaTable.refresh();
			loadingEpisodes = false;
			setLoading(false);
		});

		task.setOnFailed(event -> {
			episodes.clear();
			loadingEpisodes = false;
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

	private void setLoading(boolean loading) {
		mediaTable.setDisable(loading);

		if(loadingLabel != null) {
			loadingLabel.setText("Loading episodes...");
			loadingLabel.setVisible(loading);
			loadingLabel.setManaged(loading);
		}
	}

	private void goBack() {
		if(show != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SeasonsScene.fxml"));
				Parent root = loader.load();

				SeasonsController controller = loader.getController();
				controller.setConnection(conn);
				controller.setShow(show);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void loadTableData() {
		tryLoadEpisodes();
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