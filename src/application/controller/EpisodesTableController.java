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
import application.model.MediaPlaylist;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import java.io.File;
import java.sql.SQLException;

import application.dao.impl.SeasonDAOImpl;
import application.model.Status;
import javafx.stage.FileChooser;

public class EpisodesTableController extends BaseMediaPageController implements MediaTableOwner {

	@FXML
	private Button backButton;

	@FXML
	private Button homeButton;
	
	@FXML
	private Button addEpisodeButton;

	@FXML
	private Button deleteLastEpisodeButton;

	@FXML
	private Button changeSeasonPictureButton;

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
	private MediaPlaylist playlist;
	private SeasonDAOImpl seasonDAO;

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
		
		makeNavigationButton(
			addEpisodeButton,
			"/resources/application/images/icons/plus-svgrepo-com.png",
			"Add Episode",
			this::addEpisode
		);

		makeNavigationButton(
			deleteLastEpisodeButton,
			"/resources/application/images/icons/trash-can-svgrepo-com.png",
			"Delete Last Episode",
			this::deleteLastEpisode
		);

		makeNavigationButton(
			changeSeasonPictureButton,
			"/resources/application/images/icons/pencil-svgrepo-com.png",
			"Change Season Picture",
			this::changeSeasonPicture
		);

		setManualButtonsVisible(false);

		initializeNavigationBar();

		if(loadingLabel != null) {
			loadingLabel.setVisible(false);
			loadingLabel.setManaged(false);
		}
		
		handleDoubleClick(mediaTable, this::openEpisode);
	}
	
	private void openEpisode(Media media) {
		if(media instanceof Episode) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/EpisodeDetailsScene.fxml"));
				Parent root = loader.load();

				EpisodeDetailsController controller = loader.getController();
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setShow(show);
				controller.setSeason(season);
				controller.setEpisode((Episode)media);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
	}

	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		episodeDAO = new EpisodeDAOImpl(conn, UserSession.getCurrentUserId());
		seasonDAO = new SeasonDAOImpl(conn, UserSession.getCurrentUserId());
		tryLoadEpisodes();
	}

	public void setShow(Show show) {
		this.show = show;
		updateManualButtons();
		updateHeader();
		loadTableData();
	}

	public void setSeason(Season season) {
		this.season = season;
		updateManualButtons();
		updateHeader();
		loadTableData();
	}
	
	private void updateManualButtons() {
		boolean visible = show != null && season != null && show.getApiId() <= 0;
		setManualButtonsVisible(visible);
	}

	private void setManualButtonsVisible(boolean visible) {
		if(addEpisodeButton != null) {
			addEpisodeButton.setVisible(visible);
			addEpisodeButton.setManaged(visible);
		}

		if(deleteLastEpisodeButton != null) {
			deleteLastEpisodeButton.setVisible(visible);
			deleteLastEpisodeButton.setManaged(visible);
		}

		if(changeSeasonPictureButton != null) {
			changeSeasonPictureButton.setVisible(visible);
			changeSeasonPictureButton.setManaged(visible);
		}
	}
	
	private void addEpisode() {
		if(show != null && season != null && show.getApiId() <= 0 && episodeDAO != null) {
			try {
				int nextEpisodeNumber = 1;

				for(Media media : episodes) {
					if(media instanceof Episode) {
						Episode currentEpisode = (Episode)media;

						if(currentEpisode.getEpisodeNumber() >= nextEpisodeNumber)
							nextEpisodeNumber = currentEpisode.getEpisodeNumber() + 1;
					}
				}

				String episodeImagePath = season.getImagePath();

				if(episodeImagePath == null || episodeImagePath.isBlank())
					episodeImagePath = "/resources/application/images/icons/default-show-icon.png";

				Episode episode = new Episode(nextEpisodeNumber, "Episode " + nextEpisodeNumber, episodeImagePath);
				episode.setSeasonNumber(season.getSeasonNumber());
				episode.setStatus(Status.PLANNED);

				episodeDAO.addEpisodes(season.getPlaylistId(), List.of(episode));
				loadEpisodes();
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void deleteLastEpisode() {
		if(show != null && season != null && show.getApiId() <= 0 && episodeDAO != null) {
			Episode lastEpisode = null;

			for(Media media : episodes) {
				if(media instanceof Episode) {
					Episode currentEpisode = (Episode)media;

					if(lastEpisode == null || currentEpisode.getEpisodeNumber() > lastEpisode.getEpisodeNumber())
						lastEpisode = currentEpisode;
				}
			}

			if(lastEpisode != null) {
				try {
					episodeDAO.deleteEpisode(lastEpisode.getEpisodeId());
					loadEpisodes();
				}
				catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void changeSeasonPicture() {
		if(show != null && season != null && show.getApiId() <= 0 && seasonDAO != null) {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select Season Picture");
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

			File selectedFile = chooser.showOpenDialog(rootPane.getScene().getWindow());

			if(selectedFile != null) {
				try {
					String imagePath = selectedFile.getAbsolutePath();
					seasonDAO.updateSeasonImagePath(season.getPlaylistId(), imagePath);
					season.setImagePath(imagePath);
					loadHeaderImage();
					mediaTable.refresh();
				}
				catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void tryLoadEpisodes() {
		if(show != null && season != null && episodeDAO != null && !loadingEpisodes)
			loadEpisodes();
	}

	private void updateHeader() {
		if(season != null) {
			if(pageLabel != null)
				pageLabel.setText(season.getTitle());

			loadHeaderImage();
		}
	}
	
	private void loadHeaderImage() {
		if(mediaLogo != null && season != null) {
			String imagePath = season.getImagePath();

			if(imagePath == null || imagePath.isBlank())
				imagePath = "/resources/application/images/icons/default-show-icon.png";

			Image loadedImage = loadImage(imagePath);

			if(loadedImage == null)
				loadedImage = loadImage("/resources/application/images/icons/default-show-icon.png");

			Image finalImage = loadedImage;

			if(finalImage != null) {
				if(finalImage.getProgress() >= 1.0)
					setCenterCroppedImage(mediaLogo, finalImage, 120);
				else {
					finalImage.progressProperty().addListener((observable, oldValue, newValue) -> {
						if(newValue.doubleValue() >= 1.0)
							setCenterCroppedImage(mediaLogo, finalImage, 120);
					});
				}
			}
		}
	}

	private void loadEpisodes() {
		loadingEpisodes = true;
		setLoading(true);

		Task<List<Episode>> task = new Task<List<Episode>>() {
			@Override
			protected List<Episode> call() throws Exception {
				if(show.getApiId() > 0 && season.getSeasonNumber() > 0) {
					List<Episode> downloadedEpisodes = showAPIClient.getEpisodes(show.getApiId(), season.getSeasonNumber());

					if(downloadedEpisodes != null && !downloadedEpisodes.isEmpty())
						episodeDAO.addEpisodes(season.getPlaylistId(), downloadedEpisodes);
				}

				List<Episode> savedEpisodes = episodeDAO.getEpisodesBySeasonId(season.getPlaylistId());

				for(Episode episode : savedEpisodes)
					episode.setSeasonNumber(season.getSeasonNumber());

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
	}
	
	private void setCenterCroppedImage(ImageView imageView, Image image, double size) {
		imageView.setImage(image);
		imageView.setFitWidth(size);
		imageView.setFitHeight(size);
		imageView.setPreserveRatio(false);

		if(image.getWidth() > 0 && image.getHeight() > 0) {
			double imageWidth = image.getWidth();
			double imageHeight = image.getHeight();
			double cropSize = Math.min(imageWidth, imageHeight);
			double cropX = (imageWidth - cropSize)/2;
			double cropY = (imageHeight - cropSize)/2;

			imageView.setViewport(new javafx.geometry.Rectangle2D(cropX, cropY, cropSize, cropSize));
		}

		Rectangle clip = new javafx.scene.shape.Rectangle(size, size);
		clip.setArcWidth(20);
		clip.setArcHeight(20);
		imageView.setClip(clip);
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