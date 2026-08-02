package application.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import application.dao.MediaDAO;
import application.dao.SeasonDAO;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SeasonsTableController extends BaseMediaPageController implements PlaylistTableOwner<Season> {
	
	/*
	 * Controls the scene which displays and manages
	 * seasons belonging to a selected show
	 */
	
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

	private SeasonDAO seasonDAO;
	private MediaDAO mediaDAO;
	private Show show;
	private MediaPlaylist playlist;
	
	/**
	 * Initializes shared page elements, navigation buttons, season table, and listeners.
	 */
	@FXML
	public void initialize() {
		initializeBase();
		
		// Creates navigation buttons
		makeNavigationButton(backButton, "/resources/application/images/icons/back-reply-svgrepo-com.png", "Back", this::goBack);
		makeNavigationButton(viewShowButton, "/resources/application/images/icons/view-icon.png", "View Show Details", this::viewShowDetails);
		makeNavigationButton(deleteShowButton, "/resources/application/images/icons/delete-icon.png", "Delete Show", this::deleteShow);
		makeNavigationButton(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png", "Home", () -> switchScene("/resources/application/fxml/Menu.fxml"));
		
		initializeNavigationBar();
		
		TableBuilder.createPlaylistTable(this);
		mediaPlaylistTable.setItems(seasons);
		handleDoubleClick(mediaPlaylistTable, this::openSeason);
	}
	
	/**
	 * Sets database connection and initializes required data access objects.
	 *
	 * @param conn active database connection
	 */
	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		
		// Initializes data access objects
		seasonDAO = new SeasonDAO(conn, UserSession.getCurrentUserId());
		mediaDAO = new MediaDAO(conn, UserSession.getCurrentUserId());
		
		loadTableData();
	}
	
	/**
	 * Sets playlist containing the current show.
	 *
	 * @param playlist current media playlist
	 */
	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
	}
	
	/**
	 * Sets current show and updates season controls.
	 *
	 * @param show selected show
	 */
	public void setShow(Show show) {
		System.out.println("SeasonsTableController.setShow called");

		this.show = show;

		if(show != null)
			System.out.println("SHOW TITLE: " + show.getTitle());
		
		// Enables manual season creation for manually added shows
		boolean manualShow = show != null && show.getApiId() <= 0;

		addButton.setVisible(manualShow);
		addButton.setManaged(manualShow);

		updateHeader();
		loadTableData();
	}
	
	/**
	 * Applies show theme and updates page header.
	 *
	 * @param mediaType selected media type
	 */
	@Override
	protected void setupView(Type mediaType) {
		super.setupView(mediaType);
		updateHeader();
	}
	
	/**
	 * Updates show title and header image.
	 */
	private void updateHeader() {
		if(show != null)
		{
			if(pageLabel != null)
				pageLabel.setText(show.getTitle());

			if(mediaLogo != null)
			{
				String imagePath = show.getImagePath();
				
				// Uses default show image when needed
				if(imagePath == null || imagePath.isBlank())
					imagePath = "/resources/application/images/icons/default-show-playlist-icon.png";

				Image loadedImage = loadImage(imagePath);

				if(loadedImage == null)
					loadedImage = loadImage("/resources/application/images/icons/default-show-playlist-icon.png");

				Image finalImage = loadedImage;
				
				// Waits for online image loading
				if(finalImage != null)
				{
					if(finalImage.getProgress() >= 1.0)
						setCenterCroppedImage(mediaLogo, finalImage, 140);
					else
					{
						finalImage.progressProperty().addListener((observable, oldValue, newValue) -> {
							if(newValue.doubleValue() >= 1.0)
								setCenterCroppedImage(mediaLogo, finalImage, 140);
						});
					}
				}
			}
		}
	}
	
	/**
	 * Opens detail scene for the current show.
	 */
	private void viewShowDetails() {
		if(show != null)
		{
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
	
	/**
	 * Permanently deletes the current show.
	 */
	private void deleteShow() {
		if(show != null && mediaDAO != null)
		{
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
	
	/**
	 * Opens episode table for the selected season.
	 *
	 * @param season selected season
	 */
	private void openSeason(Season season) {
		if(season != null)
		{
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
	
	/**
	 * Returns to the previous playlist or show playlist page.
	 */
	private void goBack() {
		if(playlist != null)
		{
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
		else
		{
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
	
	/**
	 * Displays an image using a centered square crop and rounded corners.
	 *
	 * @param imageView target image view
	 * @param image image to display
	 * @param size image display size
	 */
	private static void setCenterCroppedImage(ImageView imageView, Image image, double size) {
		imageView.setImage(image);
		imageView.setFitWidth(size);
		imageView.setFitHeight(size);
		imageView.setPreserveRatio(false);
		
		// Calculates centered square viewport
		if(image != null && image.getWidth() > 0 && image.getHeight() > 0)
		{
			double imageWidth = image.getWidth();
			double imageHeight = image.getHeight();
			double cropSize = Math.min(imageWidth, imageHeight);
			double cropX = (imageWidth - cropSize)/2;
			double cropY = (imageHeight - cropSize)/2;

			imageView.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		}
		else
			imageView.setViewport(null);
		
		// Clips rounded corners
		Rectangle clip = new Rectangle(size, size);
		clip.setArcWidth(20);
		clip.setArcHeight(20);
		imageView.setClip(clip);
	}
	
	/**
	 * Loads seasons belonging to the current show.
	 */
	@Override
	protected void loadTableData() {
		if(show != null && seasonDAO != null)
		{
			try {
				seasons.setAll(seasonDAO.getSeasonsByShowId(show.getMediaId()));
			}
			catch(SQLException e) {
				seasons.clear();
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns season table.
	 *
	 * @return season table
	 */
	@Override
	public TableView<Season> getMediaPlaylistTable() {
		return mediaPlaylistTable;
	}
	
	/**
	 * Returns season number column.
	 *
	 * @return season number column
	 */
	@Override
	public TableColumn<Season, Number> getNumberColumn() {
		return numberColumn;
	}
	
	/**
	 * Returns season title column.
	 *
	 * @return season title column
	 */
	@Override
	public TableColumn<Season, Season> getTitleColumn() {
		return titleColumn;
	}
	
	/**
	 * Returns total episode column.
	 *
	 * @return total episode column
	 */
	@Override
	public TableColumn<Season, String> getTotalColumn() {
		return totalColumn;
	}
	
	/**
	 * Returns completed episode column.
	 *
	 * @return completed episode column
	 */
	@Override
	public TableColumn<Season, String> getCompletedColumn() {
		return completedColumn;
	}
	
	/**
	 * Returns in-progress episode column.
	 *
	 * @return in-progress episode column
	 */
	@Override
	public TableColumn<Season, String> getInProgressColumn() {
		return inProgressColumn;
	}
	
	/**
	 * Returns planned episode column.
	 *
	 * @return planned episode column
	 */
	@Override
	public TableColumn<Season, String> getPlannedColumn() {
		return plannedColumn;
	}
	
	/**
	 * Returns average rating column.
	 *
	 * @return average rating column
	 */
	@Override
	public TableColumn<Season, String> getAvgRatingColumn() {
		return avgRatingColumn;
	}
}