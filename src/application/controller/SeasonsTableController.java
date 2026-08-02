package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import application.dao.impl.MediaDAOImpl;
import application.dao.impl.SeasonDAOImpl;
import application.model.MediaPlaylist;
import application.model.Season;
import application.model.Show;
import application.model.Type;
import application.model.UserSession;
import application.view.PlaylistTableOwner;
import application.view.TableBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class SeasonsTableController extends BaseMediaPageController implements PlaylistTableOwner<Season> {
	
	@FXML
	private Button backButton;

	@FXML
	private Button addButton;

	@FXML
	private Button viewShowButton;
	
	@FXML
	private Button deleteShowButton;

	@FXML
	private Button homeButton;
	
	@FXML
	private Text pageLabel;
	
	@FXML
	private TableView<Season> mediaPlaylistTable;

	@FXML
	private TableColumn<Season, Number> numberColumn;

	@FXML
	private TableColumn<Season, Season> titleColumn;

	@FXML
	private TableColumn<Season, String> totalColumn;

	@FXML
	private TableColumn<Season, String> completedColumn;

	@FXML
	private TableColumn<Season, String> inProgressColumn;

	@FXML
	private TableColumn<Season, String> plannedColumn;

	@FXML
	private TableColumn<Season, String> avgRatingColumn;

	private final ObservableList<Season> seasons = FXCollections.observableArrayList();

	private SeasonDAOImpl seasonDAO;
	private MediaDAOImpl mediaDAO;
	private Show show;
	private MediaPlaylist playlist;

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
			viewShowButton,
			"/resources/application/images/icons/view-icon.png",
			"View Show Details",
			this::viewShowDetails
		);
		
		makeNavigationButton(
			deleteShowButton,
			"/resources/application/images/icons/delete-icon.png",
			"Delete Show",
			this::deleteShow
		);

		makeNavigationButton(
			homeButton,
			"/resources/application/images/icons/home-icon-svgrepo-com.png",
			"Home",
			() -> switchScene("/resources/application/fxml/Menu.fxml")
		);
		
		makeNavigationButton(
			addButton,
			"/resources/application/images/icons/plus-svgrepo-com.png",
			"Add Season Manually",
			this::addSeasonManually
		);
		
		initializeNavigationBar();
		
		TableBuilder.createPlaylistTable(this);
		mediaPlaylistTable.setItems(seasons);
		handleDoubleClick(mediaPlaylistTable, this::openSeason);
	}
	
	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		seasonDAO = new SeasonDAOImpl(conn, UserSession.getCurrentUserId());
		mediaDAO = new MediaDAOImpl(conn, UserSession.getCurrentUserId());
		loadTableData();
	}
	
	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
	}
	
	public void setShow(Show show) {
		System.out.println("SeasonsTableController.setShow called");

		this.show = show;

		if(show != null)
			System.out.println("SHOW TITLE: " + show.getTitle());

		boolean manualShow = show != null && show.getApiId() <= 0;

		addButton.setVisible(manualShow);
		addButton.setManaged(manualShow);

		updateHeader();
		loadTableData();
	}
	
	@Override
	protected void setupView(Type mediaType) {
		super.setupView(mediaType);
		updateHeader();
	}

	private void updateHeader() {
		if(show != null) {
			if(pageLabel != null)
				pageLabel.setText(show.getTitle());

			if(mediaLogo != null) {
				String imagePath = show.getImagePath();

				if(imagePath == null || imagePath.isBlank())
					imagePath = "/resources/application/images/icons/default-show-playlist-icon.png";

				Image loadedImage = loadImage(imagePath);

				if(loadedImage == null)
					loadedImage = loadImage("/resources/application/images/icons/default-show-playlist-icon.png");

				Image finalImage = loadedImage;

				if(finalImage != null) {
					if(finalImage.getProgress() >= 1.0)
						setCenterCroppedImage(mediaLogo, finalImage, 140);
					else {
						finalImage.progressProperty().addListener((observable, oldValue, newValue) -> {
							if(newValue.doubleValue() >= 1.0)
								setCenterCroppedImage(mediaLogo, finalImage, 140);
						});
					}
				}
			}
		}
	}

	private void addSeasonManually() {
		if(show != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/AddSeason.fxml"));
				Parent popup = loader.load();

				AddSeasonController controller = loader.getController();
				controller.setConnection(conn);
				controller.setShow(show);
				controller.setCloseAction(() -> rootStackPane.getChildren().remove(popup));
				controller.setRefreshAction(this::loadTableData);

				rootStackPane.getChildren().add(popup);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void viewShowDetails() {
		if(show != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/ShowsScene.fxml"));
				Parent root = loader.load();

				MediaController controller = loader.getController();
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setMedia(show);
				controller.setReturnToSeasons(show);
				controller.setOpenedFromSeasons(true);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void deleteShow() {
		if(show != null && mediaDAO != null) {
			try {
				int deleted = mediaDAO.deleteMedia(show);

				if(deleted == 1)
					goBack();
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void openSeason(Season season) {
		if(season != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/EpisodesTableScene.fxml"));
				Parent root = loader.load();

				EpisodesTableController controller = loader.getController();
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setupView(Type.SHOW);
				controller.setShow(show);
				controller.setSeason(season);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void goBack() {
		if(playlist != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsItemsScene.fxml"));
				Parent root = loader.load();

				MediaPlaylistsItemsController controller = loader.getController();
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsScene.fxml"));
				Parent root = loader.load();

				MediaPlaylistsController controller = loader.getController();
				controller.setConnection(conn);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void setCenterCroppedImage(ImageView imageView, Image image, double size) {
		imageView.setImage(image);
		imageView.setFitWidth(size);
		imageView.setFitHeight(size);
		imageView.setPreserveRatio(false);

		if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
			double imageWidth = image.getWidth();
			double imageHeight = image.getHeight();
			double cropSize = Math.min(imageWidth, imageHeight);
			double cropX = (imageWidth - cropSize) / 2;
			double cropY = (imageHeight - cropSize) / 2;

			imageView.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		}
		else
			imageView.setViewport(null);
		
		Rectangle clip = new javafx.scene.shape.Rectangle(size, size);
		clip.setArcWidth(20);
		clip.setArcHeight(20);
		imageView.setClip(clip);
	}

	@Override
	protected void loadTableData() {
		if(show != null && seasonDAO != null) {
			try {
				seasons.setAll(seasonDAO.getSeasonsByShowId(show.getMediaId()));
			}
			catch(SQLException e) {
				seasons.clear();
				e.printStackTrace();
			}
		}
	}

	@Override
	public TableView<Season> getMediaPlaylistTable() {
		return mediaPlaylistTable;
	}

	@Override
	public TableColumn<Season, Number> getNumberColumn() {
		return numberColumn;
	}

	@Override
	public TableColumn<Season, Season> getTitleColumn() {
		return titleColumn;
	}

	@Override
	public TableColumn<Season, String> getTotalColumn() {
		return totalColumn;
	}

	@Override
	public TableColumn<Season, String> getCompletedColumn() {
		return completedColumn;
	}

	@Override
	public TableColumn<Season, String> getInProgressColumn() {
		return inProgressColumn;
	}

	@Override
	public TableColumn<Season, String> getPlannedColumn() {
		return plannedColumn;
	}

	@Override
	public TableColumn<Season, String> getAvgRatingColumn() {
		return avgRatingColumn;
	}
}