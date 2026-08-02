package application.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import application.dao.impl.MediaDAOImpl;
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
import application.dao.impl.MediaPlaylistDAOImpl;
import application.model.MediaPlaylist;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import application.dao.impl.EpisodeDAOImpl;

import java.util.ArrayList;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;


public class MediaController extends BaseMediaPageController {

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
	private MediaDAOImpl mediaDAO;
	private MediaPlaylist playlist;
	private MediaPlaylistDAOImpl mediaPlaylistDAO;
	private boolean changesSaved;
	private final Rectangle dotGridClip = new Rectangle();
	private EpisodeDAOImpl episodeDAO;
	
	private final ArrayList<Circle> dots = new ArrayList<>();
	private static final double DOT_SPACING = 65.0;	
	
	private boolean returnToSeasons;
	private Show returnShow;
	private boolean openedFromSeasons;
	
	private Show parentShow;
	private Season parentSeason;
	private boolean openedFromEpisodes;

	@FXML
	public void initialize() {
		initializeBase();

		detailType = mediaLabel == null || mediaLabel.getText() == null
			? "SONGS"
			: mediaLabel.getText().toUpperCase();

		applyDetailTheme();
		initializeDotGrid();
		initializeFields();
		initializeListeners();
		initializeButtons();
		initializeNavigationBar();
	}
	
	public void setMedia(Media media) {
		this.media = media;
		loadMediaData();
	}
	
	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		mediaDAO = new MediaDAOImpl(conn, UserSession.getCurrentUserId());
		mediaPlaylistDAO = new MediaPlaylistDAOImpl(conn, UserSession.getCurrentUserId());
		episodeDAO = new EpisodeDAOImpl(conn, UserSession.getCurrentUserId());
	}
	
	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
		updateRemoveButtonVisibility();
	}
	
	public void setReturnToSeasons(Show show) {
		returnToSeasons = true;
		returnShow = show;
	}
	
	public void setOpenedFromSeasons(boolean openedFromSeasons) {
		this.openedFromSeasons = openedFromSeasons;
		updateButtonVisibility();
	}
	
	private void updateButtonVisibility() {
		if(deleteButton != null) {
			deleteButton.setVisible(!openedFromSeasons);
			deleteButton.setManaged(!openedFromSeasons);
		}

		if(removeButton != null) {
			removeButton.setVisible(!openedFromSeasons);
			removeButton.setManaged(!openedFromSeasons);
		}

		initializeNavigationBar();
	}
	
	private void loadMediaData() {
		if(media != null) {
			loadMediaPicture();

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
	
	private void setStatus(Status status) {
		if(statusLabel != null && statusField != null) {
			statusField.setValue(status);

			if(status != null)
				statusLabel.setText("Status: " + statusField.getConverter().toString(status));
			else
				statusLabel.setText("Status: ");
		}
	}

	private void loadMediaPicture() {
		if(mediaArt != null && media != null) {
			Image image = null;
			boolean defaultImage = false;
			String imagePath = media.getImagePath();

			if(imagePath != null && !imagePath.isBlank())
				image = loadImage(imagePath);

			if(image == null) {
				defaultImage = true;

				if(media instanceof Song)
					image = loadImage("/resources/application/images/icons/default-song-icon.png");
				else if(media instanceof Game)
					image = loadImage("/resources/application/images/icons/default-game-icon.png");
				else if(media instanceof Show || media instanceof Episode)
					image = loadImage("/resources/application/images/icons/default-show-icon.png");
			}

			if(image != null) {
				StackPane container = (StackPane)mediaArt.getParent();

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
	
	private void updateRemoveButtonVisibility() {
		if(removeButton != null && playlist != null && mediaType != null) {
			boolean defaultPlaylist =
				playlist.getTitle().equals("all_songs") && mediaType == Type.SONG ||
				playlist.getTitle().equals("all_games") && mediaType == Type.GAME ||
				playlist.getTitle().equals("all_shows") && mediaType == Type.SHOW;

			removeButton.setVisible(!defaultPlaylist);
			removeButton.setManaged(!defaultPlaylist);

			initializeNavigationBar();
		}
	}

	private void setTextWithPrefix(Label label, TextInputControl field, String prefix, String value) {
		String text = value;

		if(text == null)
			text = "";

		if(label != null)
			label.setText(prefix + text);

		if(field != null)
			field.setText(text);
	}

	private String formatNumber(int value) {
		String text = "";

		if(value > 0)
			text = String.valueOf(value);

		return text;
	}

	private String getRatingText() {
		String rating = "";

		if(media.getUserRating() > 0)
			rating = String.format("%.2f", media.getUserRating());

		return rating;
	}

	private String getReviewText() {
		String review = media.getReview();

		if(review == null)
			review = "";

		return review;
	}
	
	private void setText(Label label, TextField field, String value) {
		String text = value;

		if(text == null)
			text = "";

		if(label != null)
			label.setText(text);

		if(field != null)
			field.setText(text);
	}

	private void applyDetailTheme() {
		rootStackPane.getStyleClass().removeAll(
			"songs-details-theme",
			"games-details-theme",
			"shows-details-theme",
			"seasons-details-theme",
			"episodes-details-theme"
		);

		switch(detailType) {
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

	private void initializeFields() {
		hideStatusField();
		hideField(ratingLabel, ratingField);
		hideField(reviewLabel, reviewField);

		if(mediaArt != null) {
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
	
	private void fillImage(ImageView imageView) {
		Image image = imageView.getImage();

		if(image != null) {
			double imgRatio = image.getWidth() / image.getHeight();
			double viewRatio = imageView.getFitWidth() / imageView.getFitHeight();

			if(imgRatio > viewRatio) {
				double width = viewRatio / imgRatio;

				imageView.setViewport(new Rectangle2D(
					(image.getWidth() - image.getWidth() * width) / 2,
					0,
					image.getWidth() * width,
					image.getHeight()
				));
			}
			else {
				double height = imgRatio / viewRatio;

				imageView.setViewport(new Rectangle2D(
					0,
					(image.getHeight() - image.getHeight() * height) / 2,
					image.getWidth(),
					image.getHeight() * height
				));
			}
		}
	}

	private void initializeListeners() {
		bindField(ratingField, ratingLabel, "Rating: ");
		bindField(reviewField, reviewLabel, "Review: ");

		if(statusField != null) {
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

			statusField.setItems(FXCollections.observableArrayList(
				Status.PLANNED,
				Status.IN_PROGRESS,
				Status.COMPLETED
			));

			statusField.setPromptText("STATUS");

			statusField.valueProperty().addListener((observable, oldStatus, newStatus) -> {
				if(newStatus != null)
					statusLabel.setText("Status: " + statusField.getConverter().toString(newStatus));
				else
					statusLabel.setText("Status: ");
			});
		}
	}

	private void bindField(TextInputControl field, Label label, String prefix) {
		if(field != null && label != null) {
			field.textProperty().addListener((observable, oldText, newText) ->
				label.setText(prefix + newText)
			);
		}
	}

	private void initializeButtons() {
		makeNavigationButton(
			editButton,
			"/resources/application/images/icons/pencil-svgrepo-com.png",
			"Update Details",
			this::toggleEdit
		);

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
			removeButton,
			"/resources/application/images/icons/remove-icon.png",
			"Remove From Playlist",
			this::removeMedia
		);

		makeNavigationButton(
			deleteButton,
			"/resources/application/images/icons/trash-can-svgrepo-com.png",
			"Delete Media",
			this::deleteMedia
		);
		
		editButton.setTranslateY(-15);
		removeButton.setTranslateY(-15);
		deleteButton.setTranslateY(-15);
		backButton.setTranslateY(-15);
		homeButton.setTranslateY(-15);
		
		updateRemoveButtonVisibility();
	}

	@FXML
	private void toggleEdit() {
		editing = !editing;

		if(editing) {
			hideError();

			setButtonIcon(
				editButton,
				"/resources/application/images/icons/check-svgrepo-com.png"
			);
		}
		else {
			saveChanges();
			showAllLabels();

			setButtonIcon(
				editButton,
				"/resources/application/images/icons/pencil-svgrepo-com.png"
			);
		}
	}

	private void saveChanges() {
		changesSaved = false;
		hideError();

		if(media != null && mediaDAO != null) {
			try {
				Status status = statusField.getValue();
				String ratingText = ratingField.getText().trim();
				String review = reviewField.getText().trim();
				double rating = 0.0;
				boolean validRating = true;
				boolean validCompletion = true;

				if(!ratingText.isBlank()) {
					try {
						rating = Double.parseDouble(ratingText);
					}
					catch(NumberFormatException e) {
						validRating = false;
					}
				}

				if(status == null) {
					revertChanges();
					showError("Please select a status.");
				}
				else if(!validRating) {
					revertChanges();
					showError("Rating must be a valid number.");
				}
				else if(status == Status.COMPLETED && ratingText.isBlank()) {
					revertChanges();
					showError("A rating is required when media is marked as COMPLETED.");
				}
				else if(status == Status.COMPLETED && (rating <= 0 || rating > 10)) {
					revertChanges();
					showError("Rating must be between 0.01 and 10.00.");
				}
				else if(status != Status.COMPLETED && !ratingText.isBlank()) {
					revertChanges();
					showError("You can only rate media that is marked as COMPLETED.");
				}
				else if(status != Status.COMPLETED && !review.isBlank()) {
					revertChanges();
					showError("You can only review media that is marked as COMPLETED.");
				}
				else {
					if(media instanceof Show && status == Status.COMPLETED) {
						Show show = (Show)media;

						if(episodeDAO == null || !episodeDAO.canCompleteShow(show.getMediaId())) {
							showError("You must complete every episode before completing the show.");
							validCompletion = false;
						}
					}

					if(validCompletion) {
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
	
	private void revertChanges() {
		if(media != null) {
			Status previousStatus = media.getStatus();
			double previousRating = media.getUserRating();
			String previousReview = media.getReview();

			if(previousReview == null || previousReview.equals("/--/"))
				previousReview = "";

			statusField.setValue(previousStatus);

			if(previousRating > 0)
				ratingField.setText(String.format("%.2f", previousRating));
			else
				ratingField.setText("");

			reviewField.setText(previousReview);
			updateEditableDisplay();
		}
	}
	
	private void updateEditableDisplay() {
		if(media != null) {
			Status status = media.getStatus();
			double rating = media.getUserRating();
			String review = media.getReview();

			if(status != null)
				statusLabel.setText("Status: " + statusField.getConverter().toString(status));
			else
				statusLabel.setText("Status: ");

			if(rating > 0)
				ratingLabel.setText("Rating: " + String.format("%.2f", rating));
			else
				ratingLabel.setText("Rating: ");

			if(review == null || review.isBlank() || review.equals("/--/"))
				reviewLabel.setText("Review: ");
			else
				reviewLabel.setText("Review: " + review);
		}
	}
	
	public void setEpisodeContext(Show show, Season season) {
		this.parentShow = show;
		this.parentSeason = season;
		openedFromEpisodes = true;
		updateButtonVisibility();
	}
	
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

	private void showAllLabels() {
		showStatusLabel();
		showLabel(ratingLabel, ratingField);
		showLabel(reviewLabel, reviewField);
	}
	
	private void showStatusLabel() {
		if(statusLabel != null && statusField != null) {
			statusLabel.setVisible(true);
			statusLabel.setManaged(true);
			statusField.setVisible(false);
			statusField.setManaged(false);
		}
	}
	
	private void hideStatusField() {
		if(statusLabel != null && statusField != null) {
			statusLabel.setVisible(true);
			statusLabel.setManaged(true);
			statusField.setVisible(false);
			statusField.setManaged(false);
		}
	}

	@FXML
	private void editTitle() {
		editField(titleLabel, titleField);
	}

	@FXML
	private void editCreator() {
		editField(creatorLabel, creatorField);
	}

	@FXML
	private void editYear() {
		editField(yearLabel, yearField);
	}

	@FXML
	private void editGenre() {
		editField(genreLabel, genreField);
	}

	@FXML
	private void editPlaytime() {
		editField(playtimeLabel, playtimeField);
	}

	@FXML
	private void editAvgPlaytime() {
		editField(avgPlaytimeLabel, avgPlaytimeField);
	}

	@FXML
	private void editStatus() {
		if(editing && statusLabel != null && statusField != null) {
			statusLabel.setVisible(false);
			statusLabel.setManaged(false);
			statusField.setVisible(true);
			statusField.setManaged(true);
			statusField.requestFocus();
		}
	}

	@FXML
	private void editRating() {
		editField(ratingLabel, ratingField);
	}

	@FXML
	private void editReview() {
		editField(reviewLabel, reviewField);
	}

	@FXML
	private void editYearFirstAired() {
		editField(yearFirstAiredLabel, yearFirstAiredField);
	}

	@FXML
	private void editYearLastAired() {
		editField(yearLastAiredLabel, yearLastAiredField);
	}

	@FXML
	private void editNumOfSeasons() {
		editField(numOfSeasonsLabel, numOfSeasonsField);
	}

	@FXML
	private void editNumOfEpisodes() {
		editField(numOfEpisodesLabel, numOfEpisodesField);
	}

	@FXML
	private void editAiring() {
		editField(airingLabel, airingField);
	}

	@FXML
	private void editSeasonNumber() {
		editField(seasonNumberLabel, seasonNumberField);
	}

	@FXML
	private void editEpisodeNumber() {
		editField(episodeNumberLabel, episodeNumberField);
	}

	private void editField(Label label, TextInputControl field) {
		if(editing && label != null && field != null) {
			label.setVisible(false);
			label.setManaged(false);

			field.setVisible(true);
			field.setManaged(true);
			field.requestFocus();
			field.selectAll();
		}
	}

	private void showLabel(Label label, TextInputControl field) {
		if(label != null && field != null) {
			label.setVisible(true);
			label.setManaged(true);

			field.setVisible(false);
			field.setManaged(false);
		}
	}

	private void hideField(Label label, TextInputControl field) {
		if(label != null && field != null) {
			field.setVisible(false);
			field.setManaged(false);

			label.setVisible(true);
			label.setManaged(true);
		}
	}

	@FXML
	private void choosePicture() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Media Picture");
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

		File selectedFile = chooser.showOpenDialog(
			mediaArt.getScene().getWindow()
		);

		if(selectedFile != null) {
			selectedPicturePath = selectedFile.getAbsolutePath();

			Image image = new Image(selectedFile.toURI().toString());
			mediaArt.setImage(image);
			fillImage(mediaArt);
		}
	}
	
	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setVisible(true);
	}

	private void hideError() {
		errorLabel.setText("");
		errorLabel.setVisible(false);
	}

	private void removeMedia() {
		hideError();

		if(media != null && playlist != null && mediaPlaylistDAO != null) {
			try {
				mediaPlaylistDAO.removeMediaFromPlaylist(
					playlist.getPlaylistId(),
					media.getMediaId(),
					mediaType
				);

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

	private void deleteMedia() {
		hideError();

		if(media != null && mediaDAO != null) {
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
	
	private void goBack() {
		if(openedFromEpisodes)
			goBackToEpisodes();
		else if(returnToSeasons)
			goBackToSeasons();
		else
			goBackToPlaylist();
	}

	private void goBackToEpisodes() {
		if(parentShow != null && parentSeason != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/EpisodesTableScene.fxml"));
				Parent root = loader.load();

				EpisodesTableController controller = loader.getController();
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

	private void goBackToSeasons() {
		if(returnShow != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SeasonsTableScene.fxml"));
				Parent root = loader.load();

				SeasonsTableController controller = loader.getController();
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

	private void goBackToPlaylist() {
		if(playlist != null && mediaType != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsItemsScene.fxml"));
				Parent root = loader.load();

				MediaPlaylistsItemsController controller = loader.getController();
				controller.setConnection(conn);
				controller.setupView(mediaType);
				controller.setPlaylist(playlist);

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
	
	private void initializeDotGrid() {
		if(dotGridPane != null && contentCard != null) {
			dotGridPane.prefWidthProperty().bind(contentCard.widthProperty());
			dotGridPane.prefHeightProperty().bind(contentCard.heightProperty());

			dotGridClip.widthProperty().bind(dotGridPane.widthProperty());
			dotGridClip.heightProperty().bind(dotGridPane.heightProperty());
			dotGridClip.setArcWidth(48.0);
			dotGridClip.setArcHeight(48.0);
			dotGridPane.setClip(dotGridClip);

			dotGridPane.widthProperty().addListener((observable, oldValue, newValue) ->
				updateDotGrid()
			);

			dotGridPane.heightProperty().addListener((observable, oldValue, newValue) ->
				updateDotGrid()
			);

			setupDotGrid();
		}
	}

	private void setupDotGrid() {
		if(dotGridPane != null && mediaType != null) {
			Color dotColor = getDotColor();

			for(Circle dot : dots)
				dot.setFill(dotColor);

			updateDotGrid();
		}
	}

	private void createDots(int requiredDots, Color color) {
		while(dots.size() < requiredDots) {
			Circle dot = new Circle();
			dot.setFill(color);
			dot.setMouseTransparent(true);

			dots.add(dot);
			dotGridPane.getChildren().add(dot);
		}
	}

	private void updateDotGrid() {
		if(dotGridPane != null && mediaType != null) {
			double width = dotGridPane.getWidth();
			double height = dotGridPane.getHeight();

			if(width > 0 && height > 0) {
				int visibleColumns = (int)Math.ceil(width / DOT_SPACING) + 4;
				int visibleRows = (int)Math.ceil(height / DOT_SPACING) + 4;
				int requiredDots = visibleColumns * visibleRows;
				Color dotColor = getDotColor();

				createDots(requiredDots, dotColor);

				double centerX = width / 2.0;
				double centerY = height / 2.0;
				double startX = centerX - ((visibleColumns - 1) * DOT_SPACING) / 2.0;
				double startY = centerY - ((visibleRows - 1) * DOT_SPACING) / 2.0;
				int dotIndex = 0;

				for(int row = 0; row < visibleRows; row++) {
					for(int column = 0; column < visibleColumns; column++) {
						Circle dot = dots.get(dotIndex);
						double x = startX + column * DOT_SPACING;
						double y = startY + row * DOT_SPACING;
						double distance = Math.hypot(x - centerX, y - centerY);
						double radius = Math.max(3.5, 13.0 - distance / 90.0);

						dot.setCenterX(x);
						dot.setCenterY(y);
						dot.setRadius(radius);
						dot.setFill(dotColor);
						dot.setVisible(true);
						dotIndex++;
					}
				}

				for(int i = dotIndex; i < dots.size(); i++)
					dots.get(i).setVisible(false);
			}
		}
	}

	private Color getDotColor() {
		Color dotColor = Color.TRANSPARENT;

		if(mediaType != null) {
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
		}

		return dotColor;
	}

	@Override
	protected void loadTableData() {
	}
}