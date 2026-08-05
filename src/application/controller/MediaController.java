package application.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import application.dao.MediaDAO;
import application.model.Game;
import application.model.Media;
import application.model.Show;
import application.model.Song;
import application.model.Season;
import application.model.Episode;
import application.model.Status;
import application.model.Type;
import application.model.UserSession;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javafx.util.StringConverter;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import application.dao.MediaPlaylistDAO;
import application.model.MediaPlaylist;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import application.dao.EpisodeDAO;

import java.util.ArrayList;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class MediaController extends BaseMediaPageController {
	
	/*
	 * This controls the scenes which displays the details
	 * of a song, game, or a show. The length of this class
	 * is due to the cumulative attributes each individual
	 * media type has, as well as hiding/showing them depending
	 * on the current instance
	 */
	
	@FXML
	private StackPane contentCard;

	@FXML
	private ImageView mediaArt;

	@FXML
	private Label titleLabel;

	@FXML
	private TextField titleField;

	@FXML
	private Label creatorLabel;

	@FXML
	private TextField creatorField;

	@FXML
	private Label yearLabel;

	@FXML
	private TextField yearField;

	@FXML
	private Label genreLabel;

	@FXML
	private TextField genreField;

	@FXML
	private Label playtimeLabel;

	@FXML
	private TextField playtimeField;

	@FXML
	private Label avgPlaytimeLabel;

	@FXML
	private TextField avgPlaytimeField;

	@FXML
	private Label statusLabel;

	@FXML
	private ComboBox<Status> statusField;

	@FXML
	private Label ratingLabel;

	@FXML
	private TextField ratingField;

	@FXML
	private Label reviewLabel;

	@FXML
	private TextArea reviewField;

	@FXML
	private Label yearFirstAiredLabel;

	@FXML
	private TextField yearFirstAiredField;

	@FXML
	private Label yearLastAiredLabel;

	@FXML
	private TextField yearLastAiredField;

	@FXML
	private Label numOfSeasonsLabel;

	@FXML
	private TextField numOfSeasonsField;

	@FXML
	private Label numOfEpisodesLabel;

	@FXML
	private TextField numOfEpisodesField;

	@FXML
	private Label airingLabel;

	@FXML
	private TextField airingField;

	@FXML
	private Label seasonNumberLabel;

	@FXML
	private TextField seasonNumberField;

	@FXML
	private Label episodeNumberLabel;

	@FXML
	private TextField episodeNumberField;

	@FXML
	private Button editPictureButton;

	@FXML
	private Button editButton;

	@FXML
	private Button removeButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button backButton;

	@FXML
	private Button homeButton;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private Pane dotGridPane;

	private String detailType;
	private boolean editing;
	private String selectedPicturePath;
	private Media media;
	private MediaDAO mediaDAO;
	private MediaPlaylist playlist;
	private MediaPlaylistDAO mediaPlaylistDAO;
	private boolean changesSaved;
	private final Rectangle dotGridClip = new Rectangle();
	private EpisodeDAO episodeDAO;
	
	private final ArrayList<Circle> dots = new ArrayList<>();
	private static final double DOT_SPACING = 65.0;	
	
	private boolean returnToSeasons;
	private Show returnShow;
	private boolean openedFromSeasons;
	
	private Show parentShow;
	private Season parentSeason;
	private boolean openedFromEpisodes;

	/**
	 * Initializes shared page elements, fields, listeners, buttons, and theme.
	 */
	@FXML
	public void initialize() {
		initializeBase();

		/* Use of ternary learned from Exercism */
		detailType = mediaLabel == null || mediaLabel.getText() == null ? "SONGS" : mediaLabel.getText().toUpperCase();

		applyDetailTheme();
		initializeDotGrid();
		initializeFields();
		initializeListeners();
		initializeButtons();
		initializeNavigationBar();
	}
	
	/**
	 * Sets current media and loads its details.
	 *
	 * @param media selected media
	 */
	public void setMedia(Media media) {
		this.media = media;
		loadMediaData();
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
		mediaDAO = new MediaDAO(conn, UserSession.getCurrentUserId());
		mediaPlaylistDAO = new MediaPlaylistDAO(conn, UserSession.getCurrentUserId());
		episodeDAO = new EpisodeDAO(conn, UserSession.getCurrentUserId());
	}

	/**
	 * Sets playlist containing current media.
	 *
	 * @param playlist current media playlist
	 */
	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
		updateRemoveButtonVisibility();
	}

	/**
	 * Sets return destination to show seasons.
	 *
	 * @param show show whose seasons should reopen
	 */
	public void setReturnToSeasons(Show show) {
		returnToSeasons = true;
		returnShow = show;
	}

	/**
	 * Sets whether details were opened from season list.
	 *
	 * @param openedFromSeasons true when opened from seasons
	 */
	public void setOpenedFromSeasons(boolean openedFromSeasons) {
		this.openedFromSeasons = openedFromSeasons;
		updateButtonVisibility();
	}

	/**
	 * Updates delete and remove button visibility.
	 */
	private void updateButtonVisibility() {
		if(deleteButton != null)
		{
			deleteButton.setVisible(!openedFromSeasons);
			deleteButton.setManaged(!openedFromSeasons);
		}

		if(removeButton != null)
		{
			removeButton.setVisible(!openedFromSeasons);
			removeButton.setManaged(!openedFromSeasons);
		}

		initializeNavigationBar();
	}

	/**
	 * Loads details based on current media type.
	 */
	private void loadMediaData() {
		if(media != null)
		{
			loadMediaPicture();

			// Loads media-specific details
			if(media instanceof Song)
				loadSongData((Song)media);
			else if(media instanceof Game)
				loadGameData((Game)media);
			else if(media instanceof Show)
				loadShowData((Show)media);
			else if(media instanceof Episode)
				loadEpisodeData((Episode)media);

			revertChanges();
		}
	}

	/**
	 * Loads song information into labels and fields.
	 *
	 * @param song selected song
	 */
	private void loadSongData(Song song) {
		setText(titleLabel, titleField, song.getTitle());
		setTextWithPrefix(creatorLabel, creatorField, "Artist: ", song.getCreator());
		setTextWithPrefix(yearLabel, yearField, "Year Released: ", song.getYearString());
		setTextWithPrefix(genreLabel, genreField, "Album: ", song.getAlbum());
		setTextWithPrefix(playtimeLabel, playtimeField, "Runtime in Seconds: ", String.valueOf(song.getRuntimeSeconds()));
		setStatus(media.getStatus());
		setTextWithPrefix(ratingLabel, ratingField, "Rating: ", getRatingText());
		setTextWithPrefix(reviewLabel, reviewField, "Review: ", getReviewText());
	}

	/**
	 * Loads game information into labels and fields.
	 *
	 * @param game selected game
	 */
	private void loadGameData(Game game) {
		setText(titleLabel, titleField, game.getTitle());
		setTextWithPrefix(creatorLabel, creatorField, "Developer: ", game.getCreator());
		setTextWithPrefix(yearLabel, yearField, "Year Released: ", game.getYearString());
		setTextWithPrefix(genreLabel, genreField, "Genre: ", game.getGenre());
		setTextWithPrefix(avgPlaytimeLabel, avgPlaytimeField, "Average Playtime in Minutes: ", String.valueOf(game.getAvgPlaytimeMins()));
		setStatus(media.getStatus());
		setTextWithPrefix(ratingLabel, ratingField, "Rating: ", getRatingText());
		setTextWithPrefix(reviewLabel, reviewField, "Review: ", getReviewText());
	}

	/**
	 * Loads show information into labels and fields.
	 *
	 * @param show selected show
	 */
	private void loadShowData(Show show) {
		setText(titleLabel, titleField, show.getTitle());
		setTextWithPrefix(creatorLabel, creatorField, "Director: ", show.getCreator());
		setTextWithPrefix(yearFirstAiredLabel, yearFirstAiredField, "Year Started: ", formatNumber(show.getYearStart()));
		setTextWithPrefix(yearLastAiredLabel, yearLastAiredField, "Year Ended: ", formatNumber(show.getYearEnd()));
		setTextWithPrefix(genreLabel, genreField, "Genre: ", show.getGenre());
		setTextWithPrefix(numOfSeasonsLabel, numOfSeasonsField, "Number of Seasons: ", String.valueOf(show.getNumOfSeasons()));
		setTextWithPrefix(airingLabel, airingField, "Is Airing: ", show.isAiring() ? "Yes" : "No");
		setStatus(media.getStatus());
		setTextWithPrefix(ratingLabel, ratingField, "Rating: ", getRatingText());
		setTextWithPrefix(reviewLabel, reviewField, "Review: ", getReviewText());
	}

	/**
	 * Updates status field and label.
	 *
	 * @param status current media status
	 */
	private void setStatus(Status status) {
		if(statusLabel != null && statusField != null)
		{
			statusField.setValue(status);

			if(status != null)
				statusLabel.setText("Status: " + statusField.getConverter().toString(status));
			else
				statusLabel.setText("Status: ");
		}
	}

	/**
	 * Loads current media artwork or its default icon.
	 */
	private void loadMediaPicture() {
		if(mediaArt != null && media != null)
		{
			Image image = null;
			boolean defaultImage = false;
			String imagePath = media.getImagePath();

			if(imagePath != null && !imagePath.isBlank())
				image = loadImage(imagePath);

			// Loads default icon when no image is available
			if(image == null)
			{
				defaultImage = true;

				if(media instanceof Song)
					image = loadImage("/resources/application/images/icons/default-song-icon.png");
				else if(media instanceof Game)
					image = loadImage("/resources/application/images/icons/default-game-icon.png");
				else if(media instanceof Show || media instanceof Episode)
					image = loadImage("/resources/application/images/icons/default-show-icon.png");
			}

			if(image != null)
			{
				StackPane container = (StackPane)mediaArt.getParent();

				// Removes border from default icons
				if(defaultImage)
					container.getStyleClass().remove("media-art-border");
				else if(!container.getStyleClass().contains("media-art-border"))
					container.getStyleClass().add("media-art-border");

				mediaArt.setViewport(null);
				mediaArt.setImage(image);
				fillImage(mediaArt);
			}
		}
	}

	/**
	 * Updates remove button visibility based on current playlist.
	 */
	private void updateRemoveButtonVisibility() {
		if(removeButton != null && playlist != null && mediaType != null)
		{
			boolean defaultPlaylist = playlist.getTitle().equals("all_songs") && mediaType == Type.SONG ||
					playlist.getTitle().equals("all_games") && mediaType == Type.GAME ||
					playlist.getTitle().equals("all_shows") && mediaType == Type.SHOW;

			removeButton.setVisible(!defaultPlaylist);
			removeButton.setManaged(!defaultPlaylist);

			initializeNavigationBar();
		}
	}

	/**
	 * Sets matching text for a label and input field using a prefix.
	 *
	 * @param label target label
	 * @param field target input field
	 * @param prefix label prefix
	 * @param value field value
	 */
	private void setTextWithPrefix(Label label, TextInputControl field, String prefix, String value) {
		String text = value;

		if(text == null)
			text = "";

		if(label != null)
			label.setText(prefix + text);

		if(field != null)
			field.setText(text);
	}

	/**
	 * Formats a positive number for display.
	 *
	 * @param value number to format
	 * @return formatted number or empty text
	 */
	private String formatNumber(int value) {
		String text = "";

		if(value > 0)
			text = String.valueOf(value);

		return text;
	}

	/**
	 * Retrieves formatted media rating.
	 *
	 * @return formatted rating or empty text
	 */
	private String getRatingText() {
		String rating = "";

		if(media.getUserRating() > 0)
			rating = String.format("%.1f", media.getUserRating());

		return rating;
	}

	/**
	 * Retrieves media review text.
	 *
	 * @return review text
	 */
	private String getReviewText() {
		String review = media.getReview();

		if(review == null)
			review = "";

		return review;
	}

	/**
	 * Sets matching text for a label and text field.
	 *
	 * @param label target label
	 * @param field target text field
	 * @param value text value
	 */
	private void setText(Label label, TextField field, String value) {
		String text = value;

		if(text == null)
			text = "";

		if(label != null)
			label.setText(text);

		if(field != null)
			field.setText(text);
	}

	/**
	 * Applies detail theme based on current media type.
	 */
	private void applyDetailTheme() {
		// Removes previous detail themes
		rootStackPane.getStyleClass().removeAll("songs-details-theme", "games-details-theme", "shows-details-theme", "seasons-details-theme", "episodes-details-theme");

		switch(detailType)
		{
			case "GAMES":
				rootStackPane.getStyleClass().add("games-details-theme");
				setupView(Type.GAME);
				break;

			case "SHOWS":
				rootStackPane.getStyleClass().add("shows-details-theme");
				setupView(Type.SHOW);
				break;

			case "SEASONS":
				rootStackPane.getStyleClass().add("seasons-details-theme");
				setupView(Type.SHOW);
				break;

			case "EPISODES":
				rootStackPane.getStyleClass().add("episodes-details-theme");
				setupView(Type.SHOW);
				break;

			default:
				rootStackPane.getStyleClass().add("songs-details-theme");
				setupView(Type.SONG);
				break;
		}
	}

	/**
	 * Initializes visible fields and media artwork.
	 */
	private void initializeFields() {
		hideStatusField();
		hideField(ratingLabel, ratingField);
		hideField(reviewLabel, reviewField);

		// Configures media artwork
		if(mediaArt != null)
		{
			Rectangle clip = new Rectangle();
			clip.setWidth(390);
			clip.setHeight(390);
			clip.setArcWidth(60);
			clip.setArcHeight(60);

			mediaArt.setClip(clip);
			mediaArt.setFitWidth(390);
			mediaArt.setFitHeight(390);
			mediaArt.setPreserveRatio(false);
			mediaArt.setSmooth(true);
		}
	}

	/**
	 * Crops an image to fill its image view.
	 *
	 * @param imageView target image view
	 */
	private void fillImage(ImageView imageView) {
		Image image = imageView.getImage();

		if(image != null)
		{
			double imgRatio = image.getWidth()/image.getHeight();
			double viewRatio = imageView.getFitWidth()/imageView.getFitHeight();

			// Crops image width
			if(imgRatio > viewRatio)
			{
				double width = viewRatio/imgRatio;

				imageView.setViewport(new Rectangle2D((image.getWidth() - image.getWidth()*width)/2, 0, image.getWidth()*width, image.getHeight()));
			}
			// Crops image height
			else
			{
				double height = imgRatio/viewRatio;

				imageView.setViewport(new Rectangle2D(0, (image.getHeight() - image.getHeight()*height)/2, image.getWidth(), image.getHeight()*height));
			}
		}
	}

	/**
	 * Initializes field bindings and status options.
	 */
	private void initializeListeners() {
		bindField(ratingField, ratingLabel, "Rating: ");
		bindField(reviewField, reviewLabel, "Review: ");

		if(statusField != null)
		{
			// Converts status values for display
			statusField.setConverter(new StringConverter<Status>() {
				@Override
				public String toString(Status status) {
					if(status == null)
						return "STATUS";

					return status.toDbString().replace('_', ' ').toUpperCase();
				}

				@Override
				public Status fromString(String string) {
					if(string == null || string.equals("STATUS"))
						return null;

					return Status.fromDbString(string.replace(' ', '_').toLowerCase());
				}
			});

			statusField.setItems(FXCollections.observableArrayList(Status.PLANNED, Status.IN_PROGRESS, Status.COMPLETED));
			statusField.setPromptText("STATUS");

			// Updates status label
			statusField.valueProperty().addListener((observable, oldStatus, newStatus) -> {
				if(newStatus != null)
					statusLabel.setText("Status: " + statusField.getConverter().toString(newStatus));
				else
					statusLabel.setText("Status: ");
			});
		}
	}

	/**
	 * Updates a label whenever its corresponding field changes.
	 *
	 * @param field source input field
	 * @param label target label
	 * @param prefix label prefix
	 */
	private void bindField(TextInputControl field, Label label, String prefix) {
		if(field != null && label != null)
			field.textProperty().addListener((observable, oldText, newText) -> label.setText(prefix + newText));
	}

	/**
	 * Creates navigation buttons and adjusts their positions.
	 */
	private void initializeButtons() {
		makeNavigationButton(editButton, "/resources/application/images/icons/pencil-svgrepo-com.png", "Update Details", this::toggleEdit);
		makeNavigationButton(backButton, "/resources/application/images/icons/back-reply-svgrepo-com.png", "Back", this::goBack);
		makeNavigationButton(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png", "Home", () -> switchScene("/resources/application/fxml/Menu.fxml"));
		makeNavigationButton(removeButton, "/resources/application/images/icons/remove-icon.png", "Remove From Playlist", this::removeMedia);
		makeNavigationButton(deleteButton, "/resources/application/images/icons/trash-can-svgrepo-com.png", "Delete Media", this::deleteMedia);

		// Adjusts button positions
		editButton.setTranslateY(-15);
		removeButton.setTranslateY(-15);
		deleteButton.setTranslateY(-15);
		backButton.setTranslateY(-15);
		homeButton.setTranslateY(-15);

		updateRemoveButtonVisibility();
	}

	/**
	 * Enables editing or saves current changes.
	 */
	@FXML
	private void toggleEdit() {
		editing = !editing;

		if(editing)
		{
			hideError();
			setButtonIcon(editButton, "/resources/application/images/icons/check-svgrepo-com.png");
		}
		else
		{
			saveChanges();
			showAllLabels();
			setButtonIcon(editButton, "/resources/application/images/icons/pencil-svgrepo-com.png");
		}
	}

	/**
	 * Validates and saves status, rating, and review changes.
	 */
	private void saveChanges() {
		changesSaved = false;
		hideError();

		if(media != null && mediaDAO != null)
		{
			try {
				Status status = statusField.getValue();
				String ratingText = ratingField.getText().trim();
				String review = reviewField.getText().trim();
				double rating = 0.0;
				boolean validRating = true;
				boolean validCompletion = true;

				// Parses rating
				if(!ratingText.isBlank())
				{
					try {
						rating = Double.parseDouble(ratingText);
					}
					catch(NumberFormatException e) {
						validRating = false;
					}
				}

				// Validates user values
				if(status == null)
				{
					revertChanges();
					showError("Please select a status.");
				}
				else if(!validRating)
				{
					revertChanges();
					showError("Rating must be a valid number.");
				}
				else if(status == Status.COMPLETED && ratingText.isBlank())
				{
					revertChanges();
					showError("A rating is required when media is marked as COMPLETED.");
				}
				else if(status == Status.COMPLETED && (rating <= 0 || rating > 10))
				{
					revertChanges();
					showError("Rating must be between 0.01 and 10.00.");
				}
				else if(status != Status.COMPLETED && !ratingText.isBlank())
				{
					revertChanges();
					showError("You can only rate media that is marked as COMPLETED.");
				}
				else if(status != Status.COMPLETED && !review.isBlank())
				{
					revertChanges();
					showError("You can only review media that is marked as COMPLETED.");
				}
				else
				{
					// Checks show completion requirement
					if(media instanceof Show && status == Status.COMPLETED)
					{
						Show show = (Show)media;

						if(episodeDAO == null || !episodeDAO.canCompleteShow(show.getMediaId()))
						{
							showError("You must complete every episode before completing the show.");
							validCompletion = false;
						}
					}

					if(validCompletion)
					{
						mediaDAO.updateMediaStatus(media, status);
						mediaDAO.updateMediaRating(media, rating);
						mediaDAO.updateMediaReview(media, review);

						media.setStatus(status);
						media.setUserRating(rating);
						media.setReview(review);

						updateEditableDisplay();
						changesSaved = true;
					}
				}
			}
			catch(SQLException e) {
				revertChanges();
				showError("Failed to save changes.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Restores fields to current saved media values.
	 */
	private void revertChanges() {
		if(media != null)
		{
			Status previousStatus = media.getStatus();
			double previousRating = media.getUserRating();
			String previousReview = media.getReview();

			if(previousReview == null || previousReview.equals("/--/"))
				previousReview = "";

			statusField.setValue(previousStatus);

			if(previousRating > 0)
				ratingField.setText(String.format("%.1f", previousRating));
			else
				ratingField.setText("");

			reviewField.setText(previousReview);
			updateEditableDisplay();
		}
	}

	/**
	 * Updates displayed status, rating, and review values.
	 */
	private void updateEditableDisplay() {
		if(media != null)
		{
			Status status = media.getStatus();
			double rating = media.getUserRating();
			String review = media.getReview();

			if(status != null)
				statusLabel.setText("Status: " + statusField.getConverter().toString(status));
			else
				statusLabel.setText("Status: ");

			if(rating > 0)
				ratingLabel.setText("Rating: " + String.format("%.1f", rating));
			else
				ratingLabel.setText("Rating: ");

			if(review == null || review.isBlank() || review.equals("/--/"))
				reviewLabel.setText("Review: ");
			else
				reviewLabel.setText("Review: " + review);
		}
	}

	/**
	 * Sets episode return context.
	 *
	 * @param show parent show
	 * @param season parent season
	 */
	public void setEpisodeContext(Show show, Season season) {
		this.parentShow = show;
		this.parentSeason = season;
		openedFromEpisodes = true;

		updateButtonVisibility();
	}
	
	/**
	 * Loads episode information into labels and fields.
	 *
	 * @param episode selected episode
	 */
	private void loadEpisodeData(Episode episode) {
		setText(titleLabel, titleField, episode.getTitle());
		setTextWithPrefix(creatorLabel, creatorField, "Writer: ", episode.getCreator());
		setTextWithPrefix(yearLabel, yearField, "Year Released: ", episode.getYearString());
		setTextWithPrefix(seasonNumberLabel, seasonNumberField, "Season Number: ", String.valueOf(episode.getSeasonNumber()));
		setTextWithPrefix(episodeNumberLabel, episodeNumberField, "Episode Number: ", String.valueOf(episode.getEpisodeNumber()));
		setStatus(episode.getStatus());
		setTextWithPrefix(ratingLabel, ratingField, "Rating: ", getRatingText());
		setTextWithPrefix(reviewLabel, reviewField, "Review: ", getReviewText());
	}

	/**
	 * Displays all labels and hides editable fields.
	 */
	private void showAllLabels() {
		showStatusLabel();
		showLabel(ratingLabel, ratingField);
		showLabel(reviewLabel, reviewField);
	}

	/**
	 * Displays status label and hides status field.
	 */
	private void showStatusLabel() {
		if(statusLabel != null && statusField != null)
		{
			statusLabel.setVisible(true);
			statusLabel.setManaged(true);
			statusField.setVisible(false);
			statusField.setManaged(false);
		}
	}

	/**
	 * Hides status field.
	 */
	private void hideStatusField() {
		if(statusLabel != null && statusField != null)
		{
			statusLabel.setVisible(true);
			statusLabel.setManaged(true);
			statusField.setVisible(false);
			statusField.setManaged(false);
		}
	}

	/**
	 * Enables title editing.
	 */
	@FXML
	private void editTitle() {
		editField(titleLabel, titleField);
	}

	/**
	 * Enables creator editing.
	 */
	@FXML
	private void editCreator() {
		editField(creatorLabel, creatorField);
	}

	/**
	 * Enables release year editing.
	 */
	@FXML
	private void editYear() {
		editField(yearLabel, yearField);
	}

	/**
	 * Enables genre editing.
	 */
	@FXML
	private void editGenre() {
		editField(genreLabel, genreField);
	}

	/**
	 * Enables playtime editing.
	 */
	@FXML
	private void editPlaytime() {
		editField(playtimeLabel, playtimeField);
	}

	/**
	 * Enables average playtime editing.
	 */
	@FXML
	private void editAvgPlaytime() {
		editField(avgPlaytimeLabel, avgPlaytimeField);
	}

	/**
	 * Enables status editing.
	 */
	@FXML
	private void editStatus() {
		if(editing && statusLabel != null && statusField != null)
		{
			statusLabel.setVisible(false);
			statusLabel.setManaged(false);
			statusField.setVisible(true);
			statusField.setManaged(true);
			statusField.requestFocus();
		}
	}

	/**
	 * Enables rating editing.
	 */
	@FXML
	private void editRating() {
		editField(ratingLabel, ratingField);
	}

	/**
	 * Enables review editing.
	 */
	@FXML
	private void editReview() {
		editField(reviewLabel, reviewField);
	}

	/**
	 * Enables starting year editing.
	 */
	@FXML
	private void editYearFirstAired() {
		editField(yearFirstAiredLabel, yearFirstAiredField);
	}

	/**
	 * Enables ending year editing.
	 */
	@FXML
	private void editYearLastAired() {
		editField(yearLastAiredLabel, yearLastAiredField);
	}

	/**
	 * Enables season count editing.
	 */
	@FXML
	private void editNumOfSeasons() {
		editField(numOfSeasonsLabel, numOfSeasonsField);
	}

	/**
	 * Enables episode count editing.
	 */
	@FXML
	private void editNumOfEpisodes() {
		editField(numOfEpisodesLabel, numOfEpisodesField);
	}

	/**
	 * Enables airing status editing.
	 */
	@FXML
	private void editAiring() {
		editField(airingLabel, airingField);
	}

	/**
	 * Enables season number editing.
	 */
	@FXML
	private void editSeasonNumber() {
		editField(seasonNumberLabel, seasonNumberField);
	}

	/**
	 * Enables episode number editing.
	 */
	@FXML
	private void editEpisodeNumber() {
		editField(episodeNumberLabel, episodeNumberField);
	}

	/**
	 * Displays an input field while editing and hides its label.
	 *
	 * @param label target label
	 * @param field corresponding input field
	 */
	private void editField(Label label, TextInputControl field) {
		if(editing && label != null && field != null)
		{
			label.setVisible(false);
			label.setManaged(false);

			field.setVisible(true);
			field.setManaged(true);
			field.requestFocus();
			field.selectAll();
		}
	}

	/**
	 * Displays a label and hides its corresponding input field.
	 *
	 * @param label target label
	 * @param field corresponding input field
	 */
	private void showLabel(Label label, TextInputControl field) {
		if(label != null && field != null)
		{
			label.setVisible(true);
			label.setManaged(true);

			field.setVisible(false);
			field.setManaged(false);
		}
	}

	/**
	 * Hides an input field and displays its corresponding label.
	 *
	 * @param label target label
	 * @param field corresponding input field
	 */
	private void hideField(Label label, TextInputControl field) {
		if(label != null && field != null)
		{
			field.setVisible(false);
			field.setManaged(false);

			label.setVisible(true);
			label.setManaged(true);
		}
	}

	/**
	 * Opens a file chooser and loads selected media picture.
	 */
	@FXML
	private void choosePicture() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Media Picture");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

		File selectedFile = chooser.showOpenDialog(mediaArt.getScene().getWindow());

		if(selectedFile != null)
		{
			selectedPicturePath = selectedFile.getAbsolutePath();

			// Loads selected picture
			Image image = new Image(selectedFile.toURI().toString());
			mediaArt.setImage(image);
			fillImage(mediaArt);
		}
	}

	/**
	 * Displays an error message.
	 *
	 * @param message error message
	 */
	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setVisible(true);
	}

	/**
	 * Hides current error message.
	 */
	private void hideError() {
		errorLabel.setText("");
		errorLabel.setVisible(false);
	}

	/**
	 * Removes current media from selected playlist.
	 */
	private void removeMedia() {
		hideError();

		if(media != null && playlist != null && mediaPlaylistDAO != null)
		{
			try {
				mediaPlaylistDAO.removeMediaFromPlaylist(playlist.getPlaylistId(), media.getMediaId(), mediaType);
				goBack();
			}
			catch(SQLException e) {
				showError("Failed to remove this media from the playlist.");
				e.printStackTrace();
			}
		}
		else
			showError("This media or playlist could not be found.");
	}

	/**
	 * Permanently deletes current media.
	 */
	private void deleteMedia() {
		hideError();

		if(media != null && mediaDAO != null)
		{
			try {
				mediaDAO.deleteMedia(media);
				goBack();
			}
			catch(SQLException e) {
				showError("Failed to permanently delete this media.");
				e.printStackTrace();
			}
		}
		else
			showError("The media could not be found.");
	}

	/**
	 * Returns to previous page based on opening context.
	 */
	private void goBack() {
		if(openedFromEpisodes)
			goBackToEpisodes();
		else if(returnToSeasons)
			goBackToSeasons();
		else
			goBackToPlaylist();
	}

	/**
	 * Returns to the episode table.
	 */
	private void goBackToEpisodes() {
		if(parentShow != null && parentSeason != null)
		{
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/EpisodesTableScene.fxml"));
				Parent root = loader.load();

				EpisodesTableController controller = loader.getController();

				// Restores previous episode page
				controller.setConnection(conn);
				controller.setShow(parentShow);
				controller.setSeason(parentSeason);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				showError("Failed to return to the episodes.");
				e.printStackTrace();
			}
		}
		else
			showError("The original episode list could not be found.");
	}

	/**
	 * Returns to the season table.
	 */
	private void goBackToSeasons() {
		if(returnShow != null)
		{
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SeasonsTableScene.fxml"));
				Parent root = loader.load();

				SeasonsTableController controller = loader.getController();

				// Restores previous season page
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setShow(returnShow);
				controller.setupView(Type.SHOW);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				showError("Failed to return to the seasons.");
				e.printStackTrace();
			}
		}
		else
			showError("The original seasons page could not be found.");
	}

	/**
	 * Returns to the playlist page.
	 */
	private void goBackToPlaylist() {
		if(playlist != null && mediaType != null)
		{
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsItemsScene.fxml"));
				Parent root = loader.load();

				MediaPlaylistsItemsController controller = loader.getController();

				// Restores previous playlist page
				controller.setConnection(conn);
				controller.setPlaylist(playlist);
				controller.setupView(mediaType);

				Stage stage = (Stage)rootPane.getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				showError("Failed to return to the playlist.");
				e.printStackTrace();
			}
		}
		else
			showError("The original playlist could not be found.");
	}

	/**
	 * Initializes animated background dot grid.
	 */
	private void initializeDotGrid() {
		if(dotGridPane != null && contentCard != null)
		{
			// Matches dot grid size to content card
			dotGridPane.prefWidthProperty().bind(contentCard.widthProperty());
			dotGridPane.prefHeightProperty().bind(contentCard.heightProperty());

			// Clips dots within rounded card edges
			dotGridClip.widthProperty().bind(dotGridPane.widthProperty());
			dotGridClip.heightProperty().bind(dotGridPane.heightProperty());
			dotGridClip.setArcWidth(48.0);
			dotGridClip.setArcHeight(48.0);
			dotGridPane.setClip(dotGridClip);

			// Refreshes dot layout when size changes
			dotGridPane.widthProperty().addListener((observable, oldValue, newValue) -> updateDotGrid());
			dotGridPane.heightProperty().addListener((observable, oldValue, newValue) -> updateDotGrid());

			setupDotGrid();
		}
	}

	/**
	 * Updates dot grid color and layout.
	 */
	private void setupDotGrid() {
		if(dotGridPane != null && mediaType != null)
		{
			Color dotColor = getDotColor();

			// Updates existing dot colors
			for(Circle dot : dots)
				dot.setFill(dotColor);

			updateDotGrid();
		}
	}

	/**
	 * Creates additional background dots when needed.
	 *
	 * @param requiredDots required number of dots
	 * @param color dot color
	 */
	private void createDots(int requiredDots, Color color) {
		// Adds missing dots
		while(dots.size() < requiredDots)
		{
			Circle dot = new Circle();
			dot.setFill(color);
			dot.setMouseTransparent(true);

			dots.add(dot);
			dotGridPane.getChildren().add(dot);
		}
	}

	/**
	 * Updates background dot positions, sizes, colors, and visibility.
	 */
	private void updateDotGrid() {
		if(dotGridPane != null && mediaType != null)
		{
			double width = dotGridPane.getWidth();
			double height = dotGridPane.getHeight();

			if(width > 0 && height > 0)
			{
				int visibleColumns = (int)Math.ceil(width/DOT_SPACING) + 4;
				int visibleRows = (int)Math.ceil(height/DOT_SPACING) + 4;
				int requiredDots = visibleColumns*visibleRows;
				Color dotColor = getDotColor();

				// Ensures enough dots exist
				createDots(requiredDots, dotColor);

				double centerX = width/2.0;
				double centerY = height/2.0;
				double startX = centerX - ((visibleColumns - 1)*DOT_SPACING)/2.0;
				double startY = centerY - ((visibleRows - 1)*DOT_SPACING)/2.0;
				int dotIndex = 0;

				// Positions visible dots
				for(int row=0; row<visibleRows; row++)
				{
					for(int column=0; column<visibleColumns; column++)
					{
						Circle dot = dots.get(dotIndex);
						double x = startX + column*DOT_SPACING;
						double y = startY + row*DOT_SPACING;
						double distance = Math.hypot(x - centerX, y - centerY);
						double radius = Math.max(3.5, 13.0 - distance/90.0);

						dot.setCenterX(x);
						dot.setCenterY(y);
						dot.setRadius(radius);
						dot.setFill(dotColor);
						dot.setVisible(true);
						dotIndex++;
					}
				}

				// Hides unused dots
				for(int i=dotIndex; i<dots.size(); i++)
					dots.get(i).setVisible(false);
			}
		}
	}

	/**
	 * Retrieves dot color for current media type.
	 *
	 * @return matching dot color
	 */
	private Color getDotColor() {
		Color dotColor = Color.TRANSPARENT;

		if(mediaType != null)
		{
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
		}

		return dotColor;
	}

	@Override
	protected void loadTableData() {
	}
}