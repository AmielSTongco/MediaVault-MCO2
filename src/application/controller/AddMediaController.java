package application.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Consumer;

import application.dao.impl.MediaDAOImpl;
import application.dao.impl.MediaPlaylistDAOImpl;
import application.model.Game;
import application.model.Media;
import application.model.MediaPlaylist;
import application.model.Show;
import application.model.Song;
import application.model.Status;
import application.model.Type;
import application.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ContextMenu;
import application.dao.impl.SeasonDAOImpl;

public class AddMediaController {

	@FXML
	private StackPane imagePane;
	
	@FXML
	private StackPane popupContainer;
	
	@FXML
	private ImageView mediaPicture;

	@FXML
	private VBox manualTitleBox;

	@FXML
	private VBox automaticTitleBox;

	@FXML
	private TextField titleField;

	@FXML
	private TextField creatorField;

	@FXML
	private Label automaticTitleLabel;

	@FXML
	private Label automaticCreatorLabel;

	@FXML
	private Label yearLabel;

	@FXML
	private TextField yearField;

	@FXML
	private Label detailOneLabel;

	@FXML
	private TextField detailOneField;

	@FXML
	private VBox songRuntimeBox;

	@FXML
	private TextField runtimeMinutesField;

	@FXML
	private TextField runtimeSecondsField;

	@FXML
	private VBox gamePlaytimeBox;

	@FXML
	private TextField playtimeField;

	@FXML
	private TextField seasonsField;

	@FXML
	private CheckBox airingCheckBox;

	@FXML
	private MenuButton statusMenuButton;

	private Status selectedStatus;

	@FXML
	private TextField ratingField;

	@FXML
	private TextArea reviewArea;

	@FXML
	private Label statusLabel;

	@FXML
	private Button saveButton;

	@FXML
	private Button cancelButton;
	
	@FXML
	private Pane dotGridPane;
	
	@FXML
	private VBox standardFieldsBox;

	@FXML
	private VBox showFieldsBox;

	@FXML
	private TextField showYearStartField;

	@FXML
	private TextField showYearEndField;

	@FXML
	private TextField showGenreField;

	private static final double DOT_SPACING = 55.0;
	private final ArrayList<Circle> dots = new ArrayList<>();

	private boolean automaticMode;
	private Type mediaType;
	private Media media;
	private String imagePath;
	private Runnable closeAction;
	private Consumer<Media> saveAction;
	private MediaPlaylist playlist;
	private MediaDAOImpl mediaDAO;
	private SeasonDAOImpl seasonDAO;
	private MediaPlaylistDAOImpl mediaPlaylistDAO;
	private Connection conn;

	@FXML
	public void initialize() {
		setSelectedStatus(null);

		statusLabel.setVisible(false);
		statusLabel.setManaged(false);

		automaticTitleBox.setVisible(false);
		automaticTitleBox.setManaged(false);

		standardFieldsBox.setVisible(true);
		standardFieldsBox.setManaged(true);

		songRuntimeBox.setVisible(false);
		songRuntimeBox.setManaged(false);

		gamePlaytimeBox.setVisible(false);
		gamePlaytimeBox.setManaged(false);

		showFieldsBox.setVisible(false);
		showFieldsBox.setManaged(false);

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(popupContainer.widthProperty());
		clip.heightProperty().bind(popupContainer.heightProperty());
		clip.setArcWidth(60);
		clip.setArcHeight(60);
		popupContainer.setClip(clip);

		dotGridPane.widthProperty().addListener((observable, oldValue, newValue) ->
			updateDotGrid(dotGridPane, dots)
		);

		dotGridPane.heightProperty().addListener((observable, oldValue, newValue) ->
			updateDotGrid(dotGridPane, dots)
		);
		
		statusMenuButton.setOnShowing(event -> {
			ContextMenu menu = statusMenuButton.getContextMenu();

			if(menu != null)
				menu.setPrefWidth(statusMenuButton.getWidth());
		});
	}
	
	public void setConnection(Connection conn) {
		this.conn = conn;
		mediaDAO = new MediaDAOImpl(conn, UserSession.getCurrentUserId());
		mediaPlaylistDAO = new MediaPlaylistDAOImpl(conn, UserSession.getCurrentUserId());
		seasonDAO = new SeasonDAOImpl(conn, UserSession.getCurrentUserId());
	}

	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
	}

	public void setAutomaticMode(boolean automaticMode) {
		this.automaticMode = automaticMode;

		manualTitleBox.setVisible(!automaticMode);
		manualTitleBox.setManaged(!automaticMode);

		automaticTitleBox.setVisible(automaticMode);
		automaticTitleBox.setManaged(automaticMode);

		setFieldEditable(yearField, !automaticMode);
		setFieldEditable(detailOneField, !automaticMode);
		setFieldEditable(runtimeMinutesField, !automaticMode);
		setFieldEditable(runtimeSecondsField, !automaticMode);
		setFieldEditable(playtimeField, !automaticMode);

		setFieldEditable(showYearStartField, !automaticMode);
		setFieldEditable(showYearEndField, !automaticMode);
		setFieldEditable(showGenreField, !automaticMode);
		setFieldEditable(seasonsField, !automaticMode);

		airingCheckBox.setDisable(automaticMode);
		airingCheckBox.setOpacity(1);
	}

	public void setMediaType(Type mediaType) {
		this.mediaType = mediaType;
		setupMediaFields();
		applyTheme();
		setupDotGrid();
		loadDefaultImage();
	}

	public void setMedia(Media media) {
		this.media = media;
		imagePath = media.getImagePath();

		automaticTitleLabel.setText(media.getTitle());
		automaticCreatorLabel.setText("by " + media.getCreator());

		titleField.setText(media.getTitle());
		creatorField.setText(media.getCreator());
		yearField.setText(media.getYearString());

		setSelectedStatus(media.getStatus());

		if(media.getUserRating() > 0)
			ratingField.setText(String.valueOf(media.getUserRating()));
		else
			ratingField.clear();

		if(media.getReview() != null)
			reviewArea.setText(media.getReview());
		else
			reviewArea.clear();

		if(media instanceof Song) {
			Song song = (Song)media;

			detailOneField.setText(song.getAlbum());

			int runtimeSeconds = song.getRuntimeSeconds();
			runtimeMinutesField.setText(String.format("%02d", runtimeSeconds / 60));
			runtimeSecondsField.setText(String.format("%02d", runtimeSeconds % 60));
		}

		if(media instanceof Game) {
			Game game = (Game)media;

			detailOneField.setText(game.getGenre());
			
			if(game.getAvgPlaytimeMins() > 0)
				playtimeField.setText(String.valueOf(game.getAvgPlaytimeMins()));
			else
				playtimeField.clear();
		}

		if(media instanceof Show) {
			Show show = (Show)media;

			showYearStartField.setText(String.valueOf(show.getYearStart()));

			if(show.getYearEnd() > 0)
				showYearEndField.setText(String.valueOf(show.getYearEnd()));
			else
				showYearEndField.clear();

			showGenreField.setText(show.getGenre());
			seasonsField.setText(String.valueOf(show.getNumOfSeasons()));
			airingCheckBox.setSelected(show.isAiring());
		}

		loadMediaImage(imagePath);
	}

	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	public void setSaveAction(Consumer<Media> saveAction) {
		this.saveAction = saveAction;
	}

	private void setupMediaFields() {
		boolean song = mediaType == Type.SONG;
		boolean game = mediaType == Type.GAME;
		boolean show = mediaType == Type.SHOW;

		standardFieldsBox.setVisible(!show);
		standardFieldsBox.setManaged(!show);

		songRuntimeBox.setVisible(song);
		songRuntimeBox.setManaged(song);

		gamePlaytimeBox.setVisible(game);
		gamePlaytimeBox.setManaged(game);

		showFieldsBox.setVisible(show);
		showFieldsBox.setManaged(show);

		if(song) {
			yearLabel.setText("Year Released:");
			detailOneLabel.setText("Album:");
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Artist");
		}
		else if(game) {
			yearLabel.setText("Year Released:");
			detailOneLabel.setText("Genre:");
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Developer");
		}
		else if(show) {
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Creator");
		}
	}

	private void setFieldEditable(TextField field, boolean editable) {
		field.setEditable(editable);
		field.setFocusTraversable(editable);

		if(editable)
			field.getStyleClass().remove("locked-media-field");
		else if(!field.getStyleClass().contains("locked-media-field"))
			field.getStyleClass().add("locked-media-field");
	}

	@FXML
	private void handleSaveAndAdd(ActionEvent event) {
		boolean valid = validateInputs();

		if(mediaDAO == null || mediaPlaylistDAO == null) {
			showStatus("Database connection is unavailable.", true);
			valid = false;
		}
		else if(playlist == null) {
			showStatus("No playlist was selected.", true);
			valid = false;
		}

		if(valid) {
			try {
				Status status = selectedStatus;
				double rating = 0;

				if(!ratingField.getText().trim().isEmpty())
					rating = Double.parseDouble(ratingField.getText().trim());

				String review = reviewArea.getText().trim();

				if(!automaticMode)
					media = createMediaFromFields();

				if(media != null) {
					media.setStatus(status);
					media.setUserRating(rating);
					media.setReview(review);

					saveMediaToPlaylist(media);

					if(saveAction != null)
						saveAction.accept(media);

					if(closeAction != null)
						closeAction.run();
				}
				else
					showStatus("Media information is unavailable.", true);
			}
			catch(NumberFormatException e) {
				showStatus("One or more number fields are invalid.", true);
			}
			catch(SQLException e) {
				showStatus("Failed to add media to the playlist.", true);
				e.printStackTrace();
			}
			catch(Exception e) {
				showStatus("Failed to add media.", true);
				e.printStackTrace();
			}
		}
	}
	
	private void saveMediaToPlaylist(Media newMedia) throws SQLException {
		int mediaId = mediaDAO.findMediaId(newMedia);
		Media oldMedia = null;

		if(mediaId != -1) {
			if(newMedia instanceof Song)
				oldMedia = mediaDAO.getSongOfUserById(mediaId);
			else if(newMedia instanceof Game)
				oldMedia = mediaDAO.getGameOfUserById(mediaId);
			else if(newMedia instanceof Show)
				oldMedia = mediaDAO.getShowOfUserById(mediaId);
		}
		else
			mediaId = mediaDAO.addMedia(newMedia);

		newMedia.setMediaId(mediaId);
		
		if(newMedia instanceof Show) {
			Show show = (Show)newMedia;
			System.out.println("Saving TMDB API ID: " + show.getApiId());
			seasonDAO.generateSeasons(mediaId, show.getNumOfSeasons(), show.getSeasonImagePaths());
		}

		if(oldMedia != null) {
			boolean statusChanged = oldMedia.getStatus() != newMedia.getStatus();
			boolean ratingChanged = oldMedia.getUserRating() != newMedia.getUserRating();
			boolean reviewChanged = !oldMedia.getReview().equals(newMedia.getReview());

			if(statusChanged || ratingChanged || reviewChanged)
				mediaPlaylistDAO.updateAllPlaylists(newMedia);
		}

		mediaPlaylistDAO.addMediaToPlaylist(
			playlist.getPlaylistId(),
			mediaId,
			newMedia.getStatus(),
			newMedia.getUserRating(),
			newMedia.getReview(),
			mediaType.getTitle()
		);
	}

	private Media createMediaFromFields() {
		String title = titleField.getText().trim();
		String creator = creatorField.getText().trim();
		Status status = selectedStatus;
		double rating = 0;
		String review = reviewArea.getText().trim();

		if(!ratingField.getText().trim().isEmpty())
			rating = Double.parseDouble(ratingField.getText().trim());

		if(mediaType == Type.SONG) {
			int year = Integer.parseInt(yearField.getText().trim());
			String album = detailOneField.getText().trim();
			int minutes = Integer.parseInt(runtimeMinutesField.getText().trim());
			int seconds = Integer.parseInt(runtimeSecondsField.getText().trim());
			int runtimeSeconds = minutes * 60 + seconds;

			return new Song(title, status, rating, album, creator, year, runtimeSeconds, review, imagePath);
		}

		if(mediaType == Type.GAME) {
			int year = Integer.parseInt(yearField.getText().trim());
			String genre = detailOneField.getText().trim();
			int playtime = Integer.parseInt(playtimeField.getText().trim());

			return new Game(title, creator, year, status, rating, review, genre, playtime, imagePath);
		}

		if(mediaType == Type.SHOW) {
			int yearStart = Integer.parseInt(showYearStartField.getText().trim());
			int yearEnd = 0;

			if(!showYearEndField.getText().trim().isEmpty())
				yearEnd = Integer.parseInt(showYearEndField.getText().trim());

			String genre = showGenreField.getText().trim();
			int seasons = Integer.parseInt(seasonsField.getText().trim());
			boolean airing = airingCheckBox.isSelected();

			return new Show(title, creator, yearStart, yearEnd, status, rating, review, genre, seasons, airing, imagePath);
		}

		return null;
	}

	private boolean validateInputs() {
		boolean valid = true;

		if(!automaticMode && titleField.getText().trim().isEmpty()) {
			showStatus("Title cannot be empty.", true);
			valid = false;
		}
		else if(!automaticMode && creatorField.getText().trim().isEmpty()) {
			showStatus("Creator cannot be empty.", true);
			valid = false;
		}
		else if(selectedStatus == null) {
			showStatus("Select a status.", true);
			valid = false;
		}

		if(valid && mediaType == Type.SONG) {
			try {
				int minutes = Integer.parseInt(runtimeMinutesField.getText().trim());
				int seconds = Integer.parseInt(runtimeSecondsField.getText().trim());

				if(minutes < 0 || seconds < 0 || seconds > 59) {
					showStatus("Enter a valid runtime.", true);
					valid = false;
				}
			}
			catch(NumberFormatException e) {
				showStatus("Runtime must contain valid numbers.", true);
				valid = false;
			}
		}

		if(valid && mediaType == Type.GAME) {
			try {
				int playtime = Integer.parseInt(playtimeField.getText().trim());

				if(playtime < 0) {
					showStatus("Average playtime cannot be negative.", true);
					valid = false;
				}
			}
			catch(NumberFormatException e) {
				showStatus("Average playtime must be a number.", true);
				valid = false;
			}
		}

		if(valid && mediaType == Type.SHOW) {
			if(showYearStartField.getText().trim().isEmpty()) {
				showStatus("Year started cannot be empty.", true);
				valid = false;
			}
			else if(showGenreField.getText().trim().isEmpty()) {
				showStatus("Genre cannot be empty.", true);
				valid = false;
			}
			else if(seasonsField.getText().trim().isEmpty()) {
				showStatus("Number of seasons cannot be empty.", true);
				valid = false;
			}
			else {
				try {
					int yearStart = Integer.parseInt(showYearStartField.getText().trim());
					int yearEnd = 0;
					int seasons = Integer.parseInt(seasonsField.getText().trim());

					if(!showYearEndField.getText().trim().isEmpty())
						yearEnd = Integer.parseInt(showYearEndField.getText().trim());

					if(yearStart <= 0) {
						showStatus("Year started must be greater than zero.", true);
						valid = false;
					}
					else if(yearEnd > 0 && yearEnd < yearStart) {
						showStatus("Year ended cannot be earlier than year started.", true);
						valid = false;
					}
					else if(seasons <= 0) {
						showStatus("Number of seasons must be greater than zero.", true);
						valid = false;
					}
					else if(!airingCheckBox.isSelected() && yearEnd == 0) {
						showStatus("Enter the ending year for a show that is no longer airing.", true);
						valid = false;
					}
				}
				catch(NumberFormatException e) {
					showStatus("Show year and season fields must contain valid numbers.", true);
					valid = false;
				}
			}
		}
		
		if(valid && selectedStatus != Status.COMPLETED) {
			if(!ratingField.getText().trim().isEmpty()) {
				showStatus("You can only add a rating when the media is completed.", true);
				valid = false;
			}
			else if(!reviewArea.getText().trim().isEmpty()) {
				showStatus("You can only add a review when the media is completed.", true);
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
				showStatus("Rating must be a number.", true);
				valid = false;
			}
		}

		return valid;
	}

	private void loadDefaultImage() {
		loadMediaImage(getDefaultImagePath());
	}

	private String getDefaultImagePath() {
		if(mediaType == Type.SONG)
			return "/resources/application/images/icons/default-song-playlist-icon.png";

		if(mediaType == Type.GAME)
			return "/resources/application/images/icons/default-game-playlist-icon.png";

		if(mediaType == Type.SHOW)
			return "/resources/application/images/icons/default-show-playlist-icon.png";

		return "";
	}

	private void loadMediaImage(String path) {
		String finalPath = path;

		if(finalPath == null || finalPath.isBlank())
			finalPath = getDefaultImagePath();

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
			mediaPicture.setImage(image);

			image.progressProperty().addListener((observable, oldValue, newValue) -> {
				if(newValue.doubleValue() >= 1)
					setCenterCroppedImage(loadedImage);
			});
		}
		else
			setCenterCroppedImage(image);
	}

	private void setCenterCroppedImage(Image image) {
		mediaPicture.setImage(image);
		mediaPicture.setFitWidth(235);
		mediaPicture.setFitHeight(235);
		mediaPicture.setPreserveRatio(true);

		if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
			double cropSize = Math.min(image.getWidth(), image.getHeight());
			double cropX = (image.getWidth() - cropSize) / 2;
			double cropY = (image.getHeight() - cropSize) / 2;

			mediaPicture.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		}
		else
			mediaPicture.setViewport(null);
	}

	@FXML
	private void handleCancel(ActionEvent event) {
		if(closeAction != null)
			closeAction.run();
	}

	private void showStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setTextFill(error ? javafx.scene.paint.Color.web("#FF8F9B") : javafx.scene.paint.Color.web("#9BE7B0"));
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
	}
	
	private void createDotGrid(Pane pane, ArrayList<Circle> dots, Color color) {
		int columns = 25;
		int rows = 20;

		pane.setMouseTransparent(true);

		for(int row = 0; row < rows; row++) {
			for(int column = 0; column < columns; column++) {
				Circle dot = new Circle();
				dot.setFill(color);
				dots.add(dot);
				pane.getChildren().add(dot);
			}
		}

		updateDotGrid(pane, dots);
	}

	private void updateDotGrid(Pane pane, ArrayList<Circle> dots) {
		double width = pane.getWidth();
		double height = pane.getHeight();

		if(width > 0 && height > 0) {
			int visibleColumns = (int)Math.ceil(width / DOT_SPACING) + 4;
			int visibleRows = (int)Math.ceil(height / DOT_SPACING) + 4;
			double centerX = width / 2.0;
			double centerY = height / 2.0;
			double startX = centerX - ((visibleColumns - 1) * DOT_SPACING) / 2.0;
			double startY = centerY - ((visibleRows - 1) * DOT_SPACING) / 2.0;
			int dotIndex = 0;

			for(int row = 0; row < visibleRows; row++) {
				for(int column = 0; column < visibleColumns; column++) {
					if(dotIndex < dots.size()) {
						Circle dot = dots.get(dotIndex);
						double x = startX + column * DOT_SPACING;
						double y = startY + row * DOT_SPACING;
						double distance = Math.hypot(x - centerX, y - centerY);
						double radius = Math.max(2.0, 10.0 - distance / 55.0);

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
	
	private void applyTheme() {
		popupContainer.getStyleClass().removeAll("add-media-popup-songs", "add-media-popup-games", "add-media-popup-shows");
		imagePane.getStyleClass().removeAll("add-media-image-songs", "add-media-image-games", "add-media-image-shows");
		automaticTitleBox.getStyleClass().removeAll("automatic-media-header-songs", "automatic-media-header-games", "automatic-media-header-shows");
		saveButton.getStyleClass().removeAll("add-media-save-songs", "add-media-save-games", "add-media-save-shows");
		statusMenuButton.getStyleClass().removeAll("add-media-status-menu-songs", "add-media-status-menu-games", "add-media-status-menu-shows");

		removeControlThemes(titleField);
		removeControlThemes(creatorField);
		removeControlThemes(yearField);
		removeControlThemes(detailOneField);
		removeControlThemes(runtimeMinutesField);
		removeControlThemes(runtimeSecondsField);
		removeControlThemes(playtimeField);
		removeControlThemes(seasonsField);
		removeControlThemes(ratingField);
		removeControlThemes(reviewArea);
		removeControlThemes(showYearStartField);
		removeControlThemes(showYearEndField);
		removeControlThemes(showGenreField);
		removeControlThemes(seasonsField);

		String popupTheme = "";
		String controlTheme = "";
		String imageTheme = "";
		String titleTheme = "";
		String saveTheme = "";
		String statusMenuTheme = "";

		switch(mediaType) {
			case SONG:
				popupTheme = "add-media-popup-songs";
				controlTheme = "add-media-control-songs";
				imageTheme = "add-media-image-songs";
				titleTheme = "automatic-media-header-songs";
				saveTheme = "add-media-save-songs";
				statusMenuTheme = "add-media-status-menu-songs";
				break;

			case GAME:
				popupTheme = "add-media-popup-games";
				controlTheme = "add-media-control-games";
				imageTheme = "add-media-image-games";
				titleTheme = "automatic-media-header-games";
				saveTheme = "add-media-save-games";
				statusMenuTheme = "add-media-status-menu-games";
				break;

			case SHOW:
				popupTheme = "add-media-popup-shows";
				controlTheme = "add-media-control-shows";
				imageTheme = "add-media-image-shows";
				titleTheme = "automatic-media-header-shows";
				saveTheme = "add-media-save-shows";
				statusMenuTheme = "add-media-status-menu-shows";
				break;
		}

		popupContainer.getStyleClass().add(popupTheme);
		imagePane.getStyleClass().add(imageTheme);
		automaticTitleBox.getStyleClass().add(titleTheme);
		saveButton.getStyleClass().add(saveTheme);
		statusMenuButton.getStyleClass().add(statusMenuTheme);

		addControlTheme(titleField, controlTheme);
		addControlTheme(creatorField, controlTheme);
		addControlTheme(yearField, controlTheme);
		addControlTheme(detailOneField, controlTheme);
		addControlTheme(runtimeMinutesField, controlTheme);
		addControlTheme(runtimeSecondsField, controlTheme);
		addControlTheme(playtimeField, controlTheme);
		addControlTheme(seasonsField, controlTheme);
		addControlTheme(ratingField, controlTheme);
		addControlTheme(reviewArea, controlTheme);
		addControlTheme(showYearStartField, controlTheme);
		addControlTheme(showYearEndField, controlTheme);
		addControlTheme(showGenreField, controlTheme);
		addControlTheme(seasonsField, controlTheme);
	}
	
	private void removeControlThemes(Control control) {
		if(control != null)
			control.getStyleClass().removeAll("add-media-control-songs", "add-media-control-games", "add-media-control-shows");
	}

	private void addControlTheme(Control control, String theme) {
		if(control != null)
			control.getStyleClass().add(theme);
	}
	
	private void setupDotGrid() {
		Color dotColor = Color.TRANSPARENT;

		switch(mediaType) {
			case SONG:
				dotColor = Color.web("#2e5068", 0.15);
				break;

			case GAME:
				dotColor = Color.web("#212d5f", 0.43);
				break;

			case SHOW:
				dotColor = Color.web("#413466", 0.33);
				break;
		}

		if(dots.isEmpty())
			createDotGrid(dotGridPane, dots, dotColor);
		else {
			for(Circle dot : dots)
				dot.setFill(dotColor);

			updateDotGrid(dotGridPane, dots);
		}
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
}