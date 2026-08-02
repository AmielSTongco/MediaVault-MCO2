package application.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.dao.impl.SeasonDAOImpl;
import application.dao.impl.EpisodeDAOImpl;
import application.model.Season;
import application.model.Show;
import application.model.Episode;
import application.model.Status;
import application.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

public class AddSeasonController {

	@FXML
	private StackPane popupContainer;

	@FXML
	private StackPane imagePane;

	@FXML
	private ImageView seasonPicture;

	@FXML
	private TextField titleField;

	@FXML
	private TextField seasonNumberField;

	@FXML
	private TextField totalEpisodesField;

	@FXML
	private MenuButton statusMenuButton;

	@FXML
	private TextField ratingField;

	@FXML
	private TextArea reviewArea;

	@FXML
	private Label statusLabel;

	@FXML
	private Button pictureButton;

	@FXML
	private Button saveButton;

	@FXML
	private Button cancelButton;

	@FXML
	private Pane dotGridPane;

	private static final double DOT_SPACING = 55.0;
	private final ArrayList<Circle> dots = new ArrayList<>();

	private Connection conn;
	private SeasonDAOImpl seasonDAO;
	private Show show;
	private Status selectedStatus;
	private String imagePath;
	private Runnable closeAction;
	private Runnable refreshAction;
	private EpisodeDAOImpl episodeDAO;

	@FXML
	public void initialize() {
		setSelectedStatus(null);

		statusLabel.setVisible(false);
		statusLabel.setManaged(false);

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(popupContainer.widthProperty());
		clip.heightProperty().bind(popupContainer.heightProperty());
		clip.setArcWidth(60);
		clip.setArcHeight(60);
		popupContainer.setClip(clip);

		dotGridPane.widthProperty().addListener((observable, oldValue, newValue) ->
			updateDotGrid()
		);

		dotGridPane.heightProperty().addListener((observable, oldValue, newValue) ->
			updateDotGrid()
		);

		statusMenuButton.setOnShowing(event -> {
			ContextMenu menu = statusMenuButton.getContextMenu();

			if(menu != null)
				menu.setPrefWidth(statusMenuButton.getWidth());
		});

		setupDotGrid();
		loadDefaultImage();
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
		seasonDAO = new SeasonDAOImpl(conn, UserSession.getCurrentUserId());
		episodeDAO = new EpisodeDAOImpl(conn, UserSession.getCurrentUserId());
	}

	public void setShow(Show show) {
		this.show = show;
		setSuggestedSeasonNumber();
	}

	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	public void setRefreshAction(Runnable refreshAction) {
		this.refreshAction = refreshAction;
	}

	private void setSuggestedSeasonNumber() {
		if(show != null && seasonDAO != null) {
			try {
				int nextSeasonNumber = seasonDAO.getSeasonsByShowId(show.getMediaId()).size() + 1;
				seasonNumberField.setText(String.valueOf(nextSeasonNumber));
				titleField.setText("Season " + nextSeasonNumber);
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void handleChoosePicture(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Season Picture");
		chooser.getExtensionFilters().add(
			new FileChooser.ExtensionFilter(
				"Image Files",
				"*.png",
				"*.jpg",
				"*.jpeg",
				"*.gif",
				"*.webp"
			)
		);

		File selectedFile = chooser.showOpenDialog(seasonPicture.getScene().getWindow());

		if(selectedFile != null) {
			imagePath = selectedFile.getAbsolutePath();
			loadSeasonImage(imagePath);
		}
	}

	private void handleSaveAndAdd(ActionEvent event) {
		if(validateInputs()) {
			try {
				String title = titleField.getText().trim();
				int seasonNumber = Integer.parseInt(seasonNumberField.getText().trim());
				int totalEpisodes = Integer.parseInt(totalEpisodesField.getText().trim());
				double rating = 0;

				if(!ratingField.getText().trim().isEmpty())
					rating = Double.parseDouble(ratingField.getText().trim());

				if(seasonDAO.seasonExists(show.getMediaId(), seasonNumber)) {
					showStatus("That season number already exists for this show.", true);
				}
				else {
					int completedCount = 0;
					int inProgressCount = 0;
					int plannedCount = 0;
					double avgRating = 0;

					if(selectedStatus == Status.COMPLETED) {
						completedCount = totalEpisodes;
						avgRating = rating;
					}
					else if(selectedStatus == Status.IN_PROGRESS)
						inProgressCount = totalEpisodes;
					else if(selectedStatus == Status.PLANNED)
						plannedCount = totalEpisodes;

					Season season = new Season(0, show.getMediaId(), seasonNumber, title, imagePath, totalEpisodes, completedCount, inProgressCount, plannedCount, avgRating);

					int seasonId = seasonDAO.addSeason(show.getMediaId(), season);

					if(seasonId > 0) {
						List<Episode> episodes = new ArrayList<>();

						for(int episodeNumber=1; episodeNumber<=totalEpisodes; episodeNumber++) {
							Episode episode = new Episode(episodeNumber, "Episode " + episodeNumber, imagePath);
							episode.setSeasonNumber(seasonNumber);
							episode.setStatus(selectedStatus);
							episode.setUserRating(rating);
							episode.setReview("");

							episodes.add(episode);
						}

						episodeDAO.addEpisodes(seasonId, episodes);

						if(refreshAction != null)
							refreshAction.run();

						if(closeAction != null)
							closeAction.run();
					}
					else
						showStatus("The season could not be added.", true);
				}
			}
			catch(NumberFormatException e) {
				showStatus("Season number, episode count, and rating must be valid numbers.", true);
			}
			catch(SQLException e) {
				showStatus("Failed to add the season.", true);
				e.printStackTrace();
			}
		}
	}
	
	

	private boolean validateInputs() {
		boolean valid = true;

		if(show == null) {
			showStatus("The parent show could not be found.", true);
			valid = false;
		}
		else if(seasonDAO == null || conn == null) {
			showStatus("Database connection is unavailable.", true);
			valid = false;
		}
		else if(titleField.getText().trim().isEmpty()) {
			showStatus("Season title cannot be empty.", true);
			valid = false;
		}
		else if(seasonNumberField.getText().trim().isEmpty()) {
			showStatus("Season number cannot be empty.", true);
			valid = false;
		}
		else if(totalEpisodesField.getText().trim().isEmpty()) {
			showStatus("Number of episodes cannot be empty.", true);
			valid = false;
		}
		else if(selectedStatus == null) {
			showStatus("Select a status.", true);
			valid = false;
		}

		if(valid) {
			try {
				int seasonNumber = Integer.parseInt(seasonNumberField.getText().trim());
				int totalEpisodes = Integer.parseInt(totalEpisodesField.getText().trim());

				if(seasonNumber <= 0) {
					showStatus("Season number must be greater than zero.", true);
					valid = false;
				}
				else if(totalEpisodes < 0) {
					showStatus("Number of episodes cannot be negative.", true);
					valid = false;
				}
			}
			catch(NumberFormatException e) {
				showStatus("Season and episode numbers must be valid numbers.", true);
				valid = false;
			}
		}

		if(valid && selectedStatus != Status.COMPLETED) {
			if(!ratingField.getText().trim().isEmpty()) {
				showStatus("Only completed seasons can have a rating.", true);
				valid = false;
			}
			else if(!reviewArea.getText().trim().isEmpty()) {
				showStatus("Only completed seasons can have a review.", true);
				valid = false;
			}
		}

		if(valid && !ratingField.getText().trim().isEmpty()) {
			try {
				double rating = Double.parseDouble(ratingField.getText().trim());

				if(rating < 1 || rating > 10) {
					showStatus("Rating must be between 1 and 10.", true);
					valid = false;
				}
			}
			catch(NumberFormatException e) {
				showStatus("Rating must be a valid number.", true);
				valid = false;
			}
		}

		return valid;
	}

	@FXML
	private void handleCancel(ActionEvent event) {
		if(closeAction != null)
			closeAction.run();
	}

	@FXML
	private void handlePlannedStatus() {
		setSelectedStatus(Status.PLANNED);
	}

	@FXML
	private void handleInProgressStatus() {
		setSelectedStatus(Status.IN_PROGRESS);
	}

	@FXML
	private void handleCompletedStatus() {
		setSelectedStatus(Status.COMPLETED);
	}

	private void setSelectedStatus(Status status) {
		selectedStatus = status;

		if(status == null)
			statusMenuButton.setText("STATUS");
		else if(status == Status.PLANNED)
			statusMenuButton.setText("PLANNED");
		else if(status == Status.IN_PROGRESS)
			statusMenuButton.setText("IN PROGRESS");
		else
			statusMenuButton.setText("COMPLETED");
	}

	private void loadDefaultImage() {
		loadSeasonImage("/resources/application/images/icons/default-show-playlist-icon.png");
	}

	private void loadSeasonImage(String path) {
		String finalPath = path;

		if(finalPath == null || finalPath.isBlank())
			finalPath = "/resources/application/images/icons/default-show-playlist-icon.png";

		Image image = null;

		if(finalPath.startsWith("http://") || finalPath.startsWith("https://"))
			image = new Image(finalPath, true);
		else {
			File file = new File(finalPath);

			if(file.exists())
				image = new Image(file.toURI().toString());
			else if(getClass().getResource(finalPath) != null)
				image = new Image(getClass().getResource(finalPath).toExternalForm());
		}

		if(image != null && image.isBackgroundLoading()) {
			Image loadedImage = image;
			seasonPicture.setImage(image);

			image.progressProperty().addListener((observable, oldValue, newValue) -> {
				if(newValue.doubleValue() >= 1)
					setCenterCroppedImage(loadedImage);
			});
		}
		else
			setCenterCroppedImage(image);
	}

	private void setCenterCroppedImage(Image image) {
		seasonPicture.setImage(image);
		seasonPicture.setFitWidth(235);
		seasonPicture.setFitHeight(235);
		seasonPicture.setPreserveRatio(false);

		if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
			double cropSize = Math.min(image.getWidth(), image.getHeight());
			double cropX = (image.getWidth() - cropSize)/2;
			double cropY = (image.getHeight() - cropSize)/2;

			seasonPicture.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		}
		else
			seasonPicture.setViewport(null);
	}

	private void showStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setTextFill(
			error
				? Color.web("#FF8F9B")
				: Color.web("#9BE7B0")
		);
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
	}

	private void setupDotGrid() {
		Color dotColor = Color.web("#413466", 0.33);

		if(dots.isEmpty()) {
			int columns = 25;
			int rows = 20;

			dotGridPane.setMouseTransparent(true);

			for(int row = 0; row < rows; row++) {
				for(int column = 0; column < columns; column++) {
					Circle dot = new Circle();
					dot.setFill(dotColor);
					dot.setMouseTransparent(true);

					dots.add(dot);
					dotGridPane.getChildren().add(dot);
				}
			}
		}

		updateDotGrid();
	}

	private void updateDotGrid() {
		double width = dotGridPane.getWidth();
		double height = dotGridPane.getHeight();

		if(width > 0 && height > 0) {
			int visibleColumns = (int)Math.ceil(width/DOT_SPACING) + 4;
			int visibleRows = (int)Math.ceil(height/DOT_SPACING) + 4;
			double centerX = width/2.0;
			double centerY = height/2.0;
			double startX = centerX - ((visibleColumns - 1)*DOT_SPACING)/2.0;
			double startY = centerY - ((visibleRows - 1)*DOT_SPACING)/2.0;
			int dotIndex = 0;

			for(int row = 0; row < visibleRows; row++) {
				for(int column = 0; column < visibleColumns; column++) {
					if(dotIndex < dots.size()) {
						Circle dot = dots.get(dotIndex);
						double x = startX + column*DOT_SPACING;
						double y = startY + row*DOT_SPACING;
						double distance = Math.hypot(x - centerX, y - centerY);
						double radius = Math.max(2.0, 10.0 - distance/55.0);

						dot.setCenterX(x);
						dot.setCenterY(y);
						dot.setRadius(radius);
						dot.setVisible(true);
						dotIndex++;
					}
				}
			}

			for(int i = dotIndex; i < dots.size(); i++)
				dots.get(i).setVisible(false);
		}
	}
}