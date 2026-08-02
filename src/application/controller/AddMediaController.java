package application.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Consumer;

import application.dao.MediaDAO;
import application.dao.MediaPlaylistDAO;
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
import application.dao.SeasonDAO;

public class AddMediaController {
	
	/*
	 * The class that controls the pop-up whenever you add a
	 * manual or automatic medium
	 */
	
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
	
	// Used for creation of the dots background in the pop-up
	private static final double DOT_SPACING = 55.0;
	private final ArrayList<Circle> dots = new ArrayList<>();
	
	// Used to determine if the media added was from search or just manual
	private boolean automaticMode;
	
	private Type mediaType;
	private Media media;
	private String imagePath;
	private Runnable closeAction;
	private Consumer<Media> saveAction;
	private MediaPlaylist playlist;
	private MediaDAO mediaDAO;
	private SeasonDAO seasonDAO;
	private MediaPlaylistDAO mediaPlaylistDAO;
	private Connection conn;
	
	/**
	 * Initializes popup fields, visual elements, listeners, and status menu styling.
	 */
	@FXML
	public void initialize() {
		
		// Sets initial status
		setSelectedStatus(null);
		
		// Hides status message
		statusLabel.setVisible(false);
		statusLabel.setManaged(false);
		
		// Sets initial field visibility
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
		
		// Clips corners of the pop-up
		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(popupContainer.widthProperty());
		clip.heightProperty().bind(popupContainer.heightProperty());
		clip.setArcWidth(60);
		clip.setArcHeight(60);
		popupContainer.setClip(clip);
		
		/* Uses shorthand for anonymous listener class */
		// Updates dot grid with pop-up size
		dotGridPane.widthProperty().addListener((observable, oldValue, newValue) -> updateDotGrid(dotGridPane, dots));
		dotGridPane.heightProperty().addListener((observable, oldValue, newValue) -> updateDotGrid(dotGridPane, dots));
		
		/* Inspiration for use of lambda is from (https://medium.com/@nagarjun_nagesh/lambdas-in-event-driven-programming-fd448541991e) */
		// Applies status menu theme
		statusMenuButton.setOnShowing(event -> {
			ContextMenu menu = statusMenuButton.getContextMenu();

			if(menu != null) {
				menu.setPrefWidth(statusMenuButton.getWidth());

				menu.getStyleClass().removeAll(
					"add-media-status-menu-songs",
					"add-media-status-menu-games",
					"add-media-status-menu-shows"
				);

				if(mediaType == Type.SONG)
					menu.getStyleClass().add("add-media-status-menu-songs");
				else if(mediaType == Type.GAME)
					menu.getStyleClass().add("add-media-status-menu-games");
				else if(mediaType == Type.SHOW)
					menu.getStyleClass().add("add-media-status-menu-shows");
			}
		});
	}
	
	/**
	 * Sets database connection and initializes required data access objects.
	 *
	 * @param conn active database connection
	 */
	public void setConnection(Connection conn) {
		
		// Initialize connections and data access objects
		this.conn = conn;
		mediaDAO = new MediaDAO(conn, UserSession.getCurrentUserId());
		mediaPlaylistDAO = new MediaPlaylistDAO(conn, UserSession.getCurrentUserId());
		seasonDAO = new SeasonDAO(conn, UserSession.getCurrentUserId());
	}
	
	/**
	 * Sets playlist where selected media will be added.
	 *
	 * @param playlist selected media playlist
	 */
	public void setPlaylist(MediaPlaylist playlist) {
		
		// Used to determine which playlist the media came from
		this.playlist = playlist;
	}
	
	/**
	 * Sets whether media information came from API search or manual input.
	 *
	 * @param automaticMode true for API results, false for manual entries
	 */
	public void setAutomaticMode(boolean automaticMode) {
		
		// Stores current mode
		this.automaticMode = automaticMode;
		
		// Switches title section
		manualTitleBox.setVisible(!automaticMode);
		manualTitleBox.setManaged(!automaticMode);

		automaticTitleBox.setVisible(automaticMode);
		automaticTitleBox.setManaged(automaticMode);
		
		// Locks automatic fields (stuff given by search)
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
	
	/**
	 * Sets current media type and configures corresponding popup layout.
	 *
	 * @param mediaType selected media type
	 */
	public void setMediaType(Type mediaType) {
		this.mediaType = mediaType;
		
		// Sets media-specific layout (each media has its own color theme)
		setupMediaFields();
		applyTheme();
		setupDotGrid();
		loadDefaultImage();
	}
	
	/**
	 * Sets the media to be displayed and populates all corresponding fields.
	 *
	 * @param media selected media
	 */
	public void setMedia(Media media) {
		this.media = media;
		imagePath = media.getImagePath();
		
		// Loads common fields
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
		
		// Loads song fields
		if(media instanceof Song)
		{
			Song song = (Song)media;
			
			// Album field
			detailOneField.setText(song.getAlbum());

			int runtimeSeconds = song.getRuntimeSeconds();
			
			// MM:SS format
			runtimeMinutesField.setText(String.format("%02d", runtimeSeconds / 60));
			runtimeSecondsField.setText(String.format("%02d", runtimeSeconds % 60));
		}
		
		// Loads game fields
		if(media instanceof Game) {
			Game game = (Game)media;
			
			// Genre field
			detailOneField.setText(game.getGenre());
			
			if(game.getAvgPlaytimeMins() > 0)
				playtimeField.setText(String.valueOf(game.getAvgPlaytimeMins()));
			else
				playtimeField.clear();
		}
		
		// Loads show fields
		if(media instanceof Show) {
			Show show = (Show)media;
			
			// Year first aired
			showYearStartField.setText(String.valueOf(show.getYearStart()));
			
			// Year last aired
			if(show.getYearEnd() > 0)
				showYearEndField.setText(String.valueOf(show.getYearEnd()));
			else
				showYearEndField.clear();
			
			//Genre Field
			showGenreField.setText(show.getGenre());
			
			seasonsField.setText(String.valueOf(show.getNumOfSeasons()));
			airingCheckBox.setSelected(show.isAiring());
		}

		loadMediaImage(imagePath);
	}
	
	/**
	 * Sets action executed when the popup closes.
	 *
	 * @param closeAction callback to execute
	 */
	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}
	
	/**
	 * Sets action executed after media is successfully saved.
	 *
	 * @param saveAction callback to execute
	 */
	public void setSaveAction(Consumer<Media> saveAction) {
		this.saveAction = saveAction;
	}
	
	/**
	 * Configures visible fields based on the selected media type.
	 */
	private void setupMediaFields() {
		boolean song = mediaType == Type.SONG;
		boolean game = mediaType == Type.GAME;
		boolean show = mediaType == Type.SHOW;
		
		// Toggles media-specific sections
		standardFieldsBox.setVisible(!show);
		standardFieldsBox.setManaged(!show);

		songRuntimeBox.setVisible(song);
		songRuntimeBox.setManaged(song);

		gamePlaytimeBox.setVisible(game);
		gamePlaytimeBox.setManaged(game);

		showFieldsBox.setVisible(show);
		showFieldsBox.setManaged(show);
		
		// Sets up the text seen in each field of the pop-up
		if(song)
		{
			yearLabel.setText("Year Released:");
			detailOneLabel.setText("Album:");
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Artist");
		}
		else if(game)
		{
			yearLabel.setText("Year Released:");
			detailOneLabel.setText("Genre:");
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Developer");
		}
		else if(show)
		{
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Creator");
		}
	}
	
	/**
	 * Enables or disables editing for a text field.
	 *
	 * @param field target text field
	 * @param editable true if editable, otherwise false
	 */
	private void setFieldEditable(TextField field, boolean editable) {
		field.setEditable(editable);
		field.setFocusTraversable(editable);

		if(editable)
			field.getStyleClass().remove("locked-media-field");
		else if(!field.getStyleClass().contains("locked-media-field"))
			field.getStyleClass().add("locked-media-field");
	}
	
	/**
	 * Validates input fields and saves the selected media.
	 *
	 * @param event button click event
	 */
	@FXML
	private void handleSaveAndAdd(ActionEvent event) {
		boolean valid = validateInputs();
		
		// Safety checks
		if(mediaDAO == null || mediaPlaylistDAO == null)
		{
			showStatus("Database connection is unavailable.", true);
			valid = false;
		}
		else if(playlist == null)
		{
			showStatus("No playlist was selected.", true);
			valid = false;
		}

		if(valid)
		{
			try {
				Status status = selectedStatus;
				double rating = 0;

				if(!ratingField.getText().trim().isEmpty())
					rating = Double.parseDouble(ratingField.getText().trim());

				String review = reviewArea.getText().trim();

				if(!automaticMode)
					media = createMediaFromFields();

				if(media != null)
				{	
					if(media instanceof Show && status == Status.COMPLETED)
						showStatus("A show cannot be completed until all of its episodes are completed.", true);
					else {
						media.setStatus(status);
						media.setUserRating(rating);
						media.setReview(review);

						saveMediaToPlaylist(media);

						if(saveAction != null)
							saveAction.accept(media);

						if(closeAction != null)
							closeAction.run();
					}
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
	
	/**
	 * Saves media and its user information to the selected playlist.
	 *
	 * @param newMedia media to save
	 * @throws SQLException if a database error occurs
	 */
	private void saveMediaToPlaylist(Media newMedia) throws SQLException {
		
		/* Code logic adapted from MCO1 */
		
		int mediaId = mediaDAO.findMediaId(newMedia);
		Media oldMedia = null;
		
		// Checks if media already exist
		if(mediaId != -1)
		{
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

		if(oldMedia == null)
			mediaDAO.addMediaReview(newMedia);
		
		// Generates seasons for new shows
		if(newMedia instanceof Show) {
			Show show = (Show)newMedia;
			seasonDAO.generateSeasons(mediaId, show.getNumOfSeasons(), show.getSeasonImagePaths());
		}
		
		// Updates existing playlist entries
		if(oldMedia != null)
		{
			boolean statusChanged = oldMedia.getStatus() != newMedia.getStatus();
			boolean ratingChanged = oldMedia.getUserRating() != newMedia.getUserRating();
			boolean reviewChanged = !oldMedia.getReview().equals(newMedia.getReview());

			if(statusChanged || ratingChanged || reviewChanged)
				mediaPlaylistDAO.updateAllPlaylists(newMedia);
		}
		
		// Adds media to current playlist
		mediaPlaylistDAO.addMediaToPlaylist(playlist.getPlaylistId(), mediaId, newMedia.getStatus(), newMedia.getUserRating(), newMedia.getReview(), mediaType.getTitle());
	}
	
	/**
	 * Creates a media object from the entered field values.
	 *
	 * @return created media object
	 */
	private Media createMediaFromFields() {
		String title = titleField.getText().trim();
		String creator = creatorField.getText().trim();
		Status status = selectedStatus;
		double rating = 0;
		String review = reviewArea.getText().trim();

		if(!ratingField.getText().trim().isEmpty())
			rating = Double.parseDouble(ratingField.getText().trim());

		if(mediaType == Type.SONG)
		{
			int year = Integer.parseInt(yearField.getText().trim());
			String album = detailOneField.getText().trim();
			int minutes = Integer.parseInt(runtimeMinutesField.getText().trim());
			int seconds = Integer.parseInt(runtimeSecondsField.getText().trim());
			int runtimeSeconds = minutes * 60 + seconds;

			return new Song(title, status, rating, album, creator, year, runtimeSeconds, review, imagePath);
		}

		if(mediaType == Type.GAME)
		{
			int year = Integer.parseInt(yearField.getText().trim());
			String genre = detailOneField.getText().trim();
			int playtime = Integer.parseInt(playtimeField.getText().trim());

			return new Game(title, creator, year, status, rating, review, genre, playtime, imagePath);
		}

		if(mediaType == Type.SHOW)
		{
			int yearStart = Integer.parseInt(showYearStartField.getText().trim());
			int yearEnd = 0;

			if(!showYearEndField.getText().trim().isEmpty())
				yearEnd = Integer.parseInt(showYearEndField.getText().trim());

			String genre = showGenreField.getText().trim();
			int seasons = Integer.parseInt(seasonsField.getText().trim());
			boolean airing = airingCheckBox.isSelected();
			
			String finalImagePath = imagePath;

			if(finalImagePath == null || finalImagePath.isBlank())
				finalImagePath = "/resources/application/images/icons/default-show-playlist-icon.png";
			
			Show show = new Show(title, creator, yearStart, yearEnd, status, rating, review, genre, seasons, airing, finalImagePath);
			show.setApiId(0);

			return show;
		}

		return null;
	}
	
	/**
	 * Validates all entered media information.
	 *
	 * @return true if all inputs are valid, otherwise false
	 */
	private boolean validateInputs() {
		boolean valid = true;

		if(!automaticMode && titleField.getText().trim().isEmpty())
		{
			showStatus("Title cannot be empty.", true);
			valid = false;
		}
		else if(!automaticMode && creatorField.getText().trim().isEmpty())
		{
			showStatus("Creator cannot be empty.", true);
			valid = false;
		}
		else if(selectedStatus == null)
		{
			showStatus("Select a status.", true);
			valid = false;
		}

		if(valid && mediaType == Type.SONG)
		{
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

		if(valid && mediaType == Type.GAME)
		{
			try {
				int playtime = Integer.parseInt(playtimeField.getText().trim());

				if(playtime < 0)
				{
					showStatus("Average playtime cannot be negative.", true);
					valid = false;
				}
			}
			catch(NumberFormatException e) {
				showStatus("Average playtime must be a number.", true);
				valid = false;
			}
		}

		if(valid && mediaType == Type.SHOW)
		{
			if(showYearStartField.getText().trim().isEmpty())
			{
				showStatus("Year started cannot be empty.", true);
				valid = false;
			}
			else if(showGenreField.getText().trim().isEmpty())
			{
				showStatus("Genre cannot be empty.", true);
				valid = false;
			}
			else if(seasonsField.getText().trim().isEmpty())
			{
				showStatus("Number of seasons cannot be empty.", true);
				valid = false;
			}
			else
			{
				try {
					int yearStart = Integer.parseInt(showYearStartField.getText().trim());
					int yearEnd = 0;
					int seasons = Integer.parseInt(seasonsField.getText().trim());

					if(!showYearEndField.getText().trim().isEmpty())
						yearEnd = Integer.parseInt(showYearEndField.getText().trim());

					if(yearStart <= 0)
					{
						showStatus("Year started must be greater than zero.", true);
						valid = false;
					}
					else if(yearEnd > 0 && yearEnd < yearStart)
					{
						showStatus("Year ended cannot be earlier than year started.", true);
						valid = false;
					}
					else if(seasons <= 0)
					{
						showStatus("Number of seasons must be greater than zero.", true);
						valid = false;
					}
					else if(!airingCheckBox.isSelected() && yearEnd == 0)
					{
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
		
		if(valid && selectedStatus != Status.COMPLETED)
		{
			if(!ratingField.getText().trim().isEmpty())
			{
				showStatus("You can only add a rating when the media is completed.", true);
				valid = false;
			}
			else if(!reviewArea.getText().trim().isEmpty())
			{
				showStatus("You can only add a review when the media is completed.", true);
				valid = false;
			}
		}

		if(valid && !ratingField.getText().trim().isEmpty())
		{
			try {
				double rating = Double.parseDouble(ratingField.getText().trim());

				if(rating < 1 || rating > 10)
				{
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
	
	/**
	 * Loads the default image for the current media type.
	 */
	private void loadDefaultImage() {
		loadMediaImage(getDefaultImagePath());
	}
	
	/**
	 * Retrieves the default image path for the current media type.
	 *
	 * @return default image path
	 */
	private String getDefaultImagePath() {
		if(mediaType == Type.SONG)
			return "/resources/application/images/icons/default-song-playlist-icon.png";

		if(mediaType == Type.GAME)
			return "/resources/application/images/icons/default-game-playlist-icon.png";

		if(mediaType == Type.SHOW)
			return "/resources/application/images/icons/default-show-playlist-icon.png";

		return "";
	}
	
	/**
	 * Loads a media image from an online URL, local file, or application resource.
	 *
	 * @param path image path to load
	 */
	private void loadMediaImage(String path) {
		String finalPath = path;
		
		// Uses default image when needed
		if(finalPath == null || finalPath.isBlank())
			finalPath = getDefaultImagePath();

		Image image = null;
		
		// Loads online image
		if(finalPath.startsWith("http://") || finalPath.startsWith("https://"))
			image = new Image(finalPath, true);
		else
		{
			File file = new File(finalPath);

			if(file.exists())
				image = new Image(file.toURI().toString());
			else if(getClass().getResource(finalPath) != null)
				image = new Image(getClass().getResource(finalPath).toExternalForm());
		}
		
		// Waits for background image loading
		if(image != null && image.isBackgroundLoading())
		{
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
	
	/**
	 * Displays an image using a centered square crop.
	 *
	 * @param image image to display
	 */
	private void setCenterCroppedImage(Image image) {
		mediaPicture.setImage(image);
		mediaPicture.setFitWidth(235);
		mediaPicture.setFitHeight(235);
		mediaPicture.setPreserveRatio(true);
		
		// Calculates centered square viewport
		if(image != null && image.getWidth() > 0 && image.getHeight() > 0)
		{
			double cropSize = Math.min(image.getWidth(), image.getHeight());
			double cropX = (image.getWidth() - cropSize) / 2;
			double cropY = (image.getHeight() - cropSize) / 2;

			mediaPicture.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		}
		else
			mediaPicture.setViewport(null);
	}
	
	/**
	 * Closes the add media popup.
	 *
	 * @param event button click event
	 */
	@FXML
	private void handleCancel(ActionEvent event) {
		if(closeAction != null)
			closeAction.run();
	}
	
	/**
	 * Displays a success or error message.
	 *
	 * @param message message to display
	 * @param error true for error styling, otherwise false
	 */
	private void showStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setTextFill(error ? javafx.scene.paint.Color.web("#FF8F9B") : javafx.scene.paint.Color.web("#9BE7B0"));
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
	}
	
	/**
	 * Creates the background dot grid.
	 *
	 * @param pane pane containing the dots
	 * @param dots list of dot nodes
	 * @param color dot color
	 */
	private void createDotGrid(Pane pane, ArrayList<Circle> dots, Color color) {
		int columns = 25;
		int rows = 20;

		pane.setMouseTransparent(true);
		
		// Creates grid of dots
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
	
	/**
	 * Updates the dot grid layout.
	 *
	 * @param pane pane containing the dots
	 * @param dots list of dot nodes
	 */
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
			
			// Positions visible dots
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
			
			// Hides unused dots
			for(int i = dotIndex; i < dots.size(); i++)
				dots.get(i).setVisible(false);
		}
	}
	
	/**
	 * Applies the selected media theme to the popup.
	 */
	private void applyTheme() {
		
		// Removes previous theme
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
		
		// Selects media theme
		switch(mediaType)
		{
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
				
				popupContainer.setStyle("-fx-border-color: #0b112c;");
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
		
		ContextMenu menu = statusMenuButton.getContextMenu();

		if(menu != null)
		{
			menu.getStyleClass().removeAll("add-media-status-menu-songs", "add-media-status-menu-games", "add-media-status-menu-shows");
			menu.getStyleClass().add(statusMenuTheme);
		}

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
	
	/**
	 * Removes media themes from a control.
	 *
	 * @param control target control
	 */
	private void removeControlThemes(Control control) {
		if(control != null)
			control.getStyleClass().removeAll("add-media-control-songs", "add-media-control-games", "add-media-control-shows");
	}
	
	/**
	 * Applies a media theme to a control.
	 *
	 * @param control target control
	 * @param theme theme class to apply
	 */
	private void addControlTheme(Control control, String theme) {
		if(control != null)
			control.getStyleClass().add(theme);
	}
	
	/**
	 * Configures the background dot grid for the selected media type.
	 */
	private void setupDotGrid() {
		Color dotColor = Color.TRANSPARENT;
		
		
		/* Each color was specifically and intentionally chosen. Designed in Canva! */
		// Selects dot color
		switch(mediaType)
		{
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
		else
		{
			for(Circle dot : dots)
				dot.setFill(dotColor);

			updateDotGrid(dotGridPane, dots);
		}
	}
	
	/* Methods used by the buttons */
	
	/**
	 * Sets media status to Planned.
	 */
	@FXML
	private void handlePlannedStatus() {
		setSelectedStatus(Status.PLANNED);
	}
	
	/**
	 * Sets media status to In Progress.
	 */
	@FXML
	private void handleInProgressStatus() {
		setSelectedStatus(Status.IN_PROGRESS);
	}
	
	/**
	 * Sets media status to Completed.
	 */
	@FXML
	private void handleCompletedStatus() {
		setSelectedStatus(Status.COMPLETED);
	}
	
	/**
	 * Updates the selected status and button label.
	 *
	 * @param status selected media status
	 */
	private void setSelectedStatus(Status status) {
		selectedStatus = status;
		
		// Updates button text
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