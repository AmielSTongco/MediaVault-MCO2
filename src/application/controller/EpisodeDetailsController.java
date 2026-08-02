package application.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import application.dao.impl.EpisodeDAOImpl;
import application.model.Episode;
import application.model.MediaPlaylist;
import application.model.Season;
import application.model.Show;
import application.model.Status;
import application.model.Type;
import application.model.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class EpisodeDetailsController extends BaseMediaPageController {

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
	private Label seasonNumberLabel;

	@FXML
	private TextField seasonNumberField;

	@FXML
	private Label episodeNumberLabel;

	@FXML
	private TextField episodeNumberField;

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
	private Button editPictureButton;

	@FXML
	private Button editButton;

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

	private final Rectangle dotGridClip = new Rectangle();
	private final ArrayList<Circle> dots = new ArrayList<>();
	private static final double DOT_SPACING = 65.0;

	private EpisodeDAOImpl episodeDAO;
	private MediaPlaylist playlist;
	private Show show;
	private Season season;
	private Episode episode;
	private String selectedPicturePath;
	private boolean editing;

	@FXML
	public void initialize() {
		initializeBase();
		setupView(Type.SHOW);

		initializeDotGrid();
		initializeFields();
		initializeListeners();
		initializeButtons();
		initializeNavigationBar();
		hideError();
	}

	@Override
	public void setConnection(Connection conn) {
		super.setConnection(conn);
		episodeDAO = new EpisodeDAOImpl(conn, UserSession.getCurrentUserId());
	}

	public void setPlaylist(MediaPlaylist playlist) {
		this.playlist = playlist;
	}

	public void setShow(Show show) {
		this.show = show;

		boolean automaticShow = isAutomaticShow();

		deleteButton.setVisible(!automaticShow);
		deleteButton.setManaged(!automaticShow);

		if(episode != null)
			loadEpisodeDetails();

		initializeNavigationBar();
	}

	public void setSeason(Season season) {
		this.season = season;

		if(episode != null)
			loadEpisodeDetails();
	}

	public void setEpisode(Episode episode) {
		this.episode = episode;
		selectedPicturePath = episode == null ? null : episode.getImagePath();

		loadEpisodeDetails();
		loadEpisodePicture();
	}

	private void initializeFields() {
		showAllLabels();

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
		
		seasonNumberField.setEditable(false);
		seasonNumberField.setFocusTraversable(false);
		seasonNumberField.setMouseTransparent(true);

		episodeNumberField.setEditable(false);
		episodeNumberField.setFocusTraversable(false);
		episodeNumberField.setMouseTransparent(true);
	}

	private void initializeListeners() {
		bindField(titleField, titleLabel, "");
		bindField(creatorField, creatorLabel, "Writer: ");
		bindField(yearField, yearLabel, "Year Released: ");
		bindField(genreField, genreLabel, "Genre: ");
		bindField(seasonNumberField, seasonNumberLabel, "Season Number: ");
		bindField(episodeNumberField, episodeNumberLabel, "Episode Number: ");
		bindField(ratingField, ratingLabel, "Rating: ");
		bindField(reviewField, reviewLabel, "Review: ");

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

	private void initializeButtons() {
		makeNavigationButton(
			editButton,
			"/resources/application/images/icons/pencil-svgrepo-com.png",
			"Update Episode",
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
			deleteButton,
			"/resources/application/images/icons/trash-can-svgrepo-com.png",
			"Delete Episode",
			this::deleteEpisode
		);

		editButton.setTranslateY(-15);
		deleteButton.setTranslateY(-15);
		backButton.setTranslateY(-15);
		homeButton.setTranslateY(-15);
	}

	private void loadEpisodeDetails() {
		if(episode != null) {
			String yearReleased = "/--/";
			String genre = "";
			String seasonNumber = "";

			if(episode.getYearReleased() > 0)
				yearReleased = String.valueOf(episode.getYearReleased());

			if(show != null)
				genre = show.getGenre();

			if(season != null)
				seasonNumber = String.valueOf(season.getSeasonNumber());

			setText(titleLabel, titleField, episode.getTitle());
			setTextWithPrefix(creatorLabel, creatorField, "Writer: ", episode.getCreator());
			setTextWithPrefix(yearLabel, yearField, "Year Released: ", yearReleased);
			setTextWithPrefix(genreLabel, genreField, "Genre: ", genre);
			setTextWithPrefix(seasonNumberLabel, seasonNumberField, "Season Number: ", seasonNumber);
			setTextWithPrefix(episodeNumberLabel, episodeNumberField, "Episode Number: ", String.valueOf(episode.getEpisodeNumber()));

			statusField.setValue(episode.getStatus());

			if(episode.getStatus() != null)
				statusLabel.setText("Status: " + statusField.getConverter().toString(episode.getStatus()));
			else
				statusLabel.setText("Status: ");

			if(episode.getUserRating() > 0)
				setTextWithPrefix(ratingLabel, ratingField, "Rating: ", String.format("%.2f", episode.getUserRating()));
			else
				setTextWithPrefix(ratingLabel, ratingField, "Rating: ", "");

			String review = episode.getReview();

			if(review == null || review.equals("/--/"))
				review = "";

			setTextWithPrefix(reviewLabel, reviewField, "Review: ", review);
			showAllLabels();
		}
	}

	private void loadEpisodePicture() {
		if(mediaArt != null && episode != null) {
			String imagePath = episode.getImagePath();

			if(imagePath == null || imagePath.isBlank()) {
				if(season != null)
					imagePath = season.getImagePath();
			}

			if(imagePath == null || imagePath.isBlank()) {
				if(show != null)
					imagePath = show.getImagePath();
			}

			if(imagePath == null || imagePath.isBlank())
				imagePath = "/resources/application/images/icons/default-show-icon.png";

			boolean defaultIcon = isDefaultIcon(imagePath);
			StackPane imageContainer = (StackPane)mediaArt.getParent();

			if(defaultIcon)
				imageContainer.getStyleClass().remove("media-art-border");
			else if(!imageContainer.getStyleClass().contains("media-art-border"))
				imageContainer.getStyleClass().add("media-art-border");

			Image loadedImage = loadImage(imagePath);

			if(loadedImage == null) {
				imagePath = "/resources/application/images/icons/default-show-icon.png";
				defaultIcon = true;
				imageContainer.getStyleClass().remove("media-art-border");
				loadedImage = loadImage(imagePath);
			}

			Image finalImage = loadedImage;
			boolean finalDefaultIcon = defaultIcon;

			if(finalImage != null) {
				if(finalImage.getProgress() >= 1.0)
					setCenterCroppedImage(mediaArt, finalImage, 390, !finalDefaultIcon);
				else {
					finalImage.progressProperty().addListener((observable, oldValue, newValue) -> {
						if(newValue.doubleValue() >= 1.0)
							setCenterCroppedImage(mediaArt, finalImage, 390, !finalDefaultIcon);
					});
				}
			}
		}
	}
	
	private boolean isDefaultIcon(String imagePath) {
		if(imagePath == null)
			return true;

		return imagePath.endsWith("default-show-icon.png")
			|| imagePath.endsWith("default-show-playlist-icon.png")
			|| imagePath.endsWith("shows-icon.png");
	}
	
	private static void setCenterCroppedImage(ImageView imageView, Image image, double size, boolean rounded) {
		imageView.setFitWidth(size);
		imageView.setFitHeight(size);
		imageView.setPreserveRatio(false);
		imageView.setViewport(null);
		imageView.setImage(image);
		imageView.setSmooth(true);

		if(image != null && image.getWidth() > 0 && image.getHeight() > 0) {
			double imageWidth = image.getWidth();
			double imageHeight = image.getHeight();
			double cropSize = Math.min(imageWidth, imageHeight);
			double cropX = (imageWidth - cropSize)/2;
			double cropY = (imageHeight - cropSize)/2;

			imageView.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		}

		if(rounded) {
			Rectangle clip = new Rectangle(size, size);
			clip.setArcWidth(45);
			clip.setArcHeight(45);
			imageView.setClip(clip);
		}
		else
			imageView.setClip(null);
	}

	@FXML
	private void toggleEdit() {
		if(!editing) {
			editing = true;
			hideError();

			setButtonIcon(
				editButton,
				"/resources/application/images/icons/check-svgrepo-com.png"
			);
		}
		else
			saveEpisode();
	}

	private void saveEpisode() {
		hideError();

		if(episode == null || episodeDAO == null) {
			showError("Episode information is unavailable.");
			return;
		}

		Status status = statusField.getValue();
		String ratingText = ratingField.getText().trim();
		String review = reviewField.getText().trim();
		double rating = 0;
		boolean valid = true;

		if(status == null) {
			showError("Please select a status.");
			valid = false;
		}
		else if(!ratingText.isBlank()) {
			try {
				rating = Double.parseDouble(ratingText);
			}
			catch(NumberFormatException e) {
				showError("Rating must be a valid number.");
				valid = false;
			}
		}

		if(valid && status == Status.COMPLETED && ratingText.isBlank()) {
			showError("A rating is required when the episode is marked as COMPLETED.");
			valid = false;
		}
		else if(valid && status == Status.COMPLETED && (rating <= 0 || rating > 10)) {
			showError("Rating must be between 0.01 and 10.00.");
			valid = false;
		}
		else if(valid && status != Status.COMPLETED && !ratingText.isBlank()) {
			showError("You can only rate an episode marked as COMPLETED.");
			valid = false;
		}
		else if(valid && status != Status.COMPLETED && !review.isBlank()) {
			showError("You can only review an episode marked as COMPLETED.");
			valid = false;
		}

		String title = episode.getTitle();
		String writer = episode.getWriter();
		int yearReleased = episode.getYearReleased();

		if(valid && !isAutomaticShow()) {
			title = titleField.getText().trim();
			writer = creatorField.getText().trim();

			try {
				yearReleased = Integer.parseInt(yearField.getText().trim());
			}
			catch(NumberFormatException e) {
				showError("Year released must be a valid number.");
				valid = false;
			}

			if(valid && title.isBlank()) {
				showError("Episode title cannot be empty.");
				valid = false;
			}
			else if(valid && yearReleased <= 0) {
				showError("Year released must be greater than zero.");
				valid = false;
			}
		}

		if(valid) {
			try {
				episode.setStatus(status);
				episode.setUserRating(rating);
				episode.setReview(review);

				if(!isAutomaticShow()) {
					episode.setTitle(title);
					episode.setWriter(writer);
					episode.setYearReleased(yearReleased);
					episode.setImagePath(selectedPicturePath);
				}

				saveEpisodeChanges();
			}
			catch(SQLException e) {
				showError("The episode could not be saved.");
				e.printStackTrace();
			}
		}

		if(!valid) {
			editing = true;

			setButtonIcon(
				editButton,
				"/resources/application/images/icons/check-svgrepo-com.png"
			);
		}
	}

	private void saveEpisodeChanges() throws SQLException {
		int reviewUpdated = episodeDAO.updateEpisodeReview(episode);
		int episodeUpdated = 1;

		if(!isAutomaticShow()) {
			if(season != null)
				episodeUpdated = episodeDAO.updateEpisode(episode, season.getPlaylistId());
			else {
				showError("The episode's season could not be found.");
				episodeUpdated = 0;
			}
		}

		if(reviewUpdated > 0 && episodeUpdated > 0) {
			editing = false;

			hideError();
			updateEpisodeDisplay();
			showAllLabels();

			setButtonIcon(
				editButton,
				"/resources/application/images/icons/pencil-svgrepo-com.png"
			);
		}
		else {
			editing = true;
			showError("The episode could not be saved.");

			setButtonIcon(
				editButton,
				"/resources/application/images/icons/check-svgrepo-com.png"
			);
		}
	}
	
	private void updateEpisodeDisplay() {
		if(episode != null) {
			String yearReleased = "/--/";
			String genre = "";
			String seasonNumber = "";

			if(episode.getYearReleased() > 0)
				yearReleased = String.valueOf(episode.getYearReleased());

			if(show != null)
				genre = show.getGenre();

			if(season != null)
				seasonNumber = String.valueOf(season.getSeasonNumber());

			setText(titleLabel, titleField, episode.getTitle());
			setTextWithPrefix(creatorLabel, creatorField, "Writer: ", episode.getWriter());
			setTextWithPrefix(yearLabel, yearField, "Year Released: ", yearReleased);
			setTextWithPrefix(genreLabel, genreField, "Genre: ", genre);
			setTextWithPrefix(seasonNumberLabel, seasonNumberField, "Season Number: ", seasonNumber);
			setTextWithPrefix(episodeNumberLabel, episodeNumberField, "Episode Number: ", String.valueOf(episode.getEpisodeNumber()));

			statusField.setValue(episode.getStatus());

			if(episode.getStatus() != null)
				statusLabel.setText("Status: " + statusField.getConverter().toString(episode.getStatus()));
			else
				statusLabel.setText("Status: ");

			if(episode.getUserRating() > 0)
				setTextWithPrefix(ratingLabel, ratingField, "Rating: ", String.format("%.2f", episode.getUserRating()));
			else
				setTextWithPrefix(ratingLabel, ratingField, "Rating: ", "");

			String review = episode.getReview();

			if(review == null || review.equals("/--/"))
				review = "";

			setTextWithPrefix(reviewLabel, reviewField, "Review: ", review);
		}
	}

	private void setText(Label label, TextField field, String value) {
		String text = value;

		if(text == null)
			text = "";

		label.setText(text);
		field.setText(text);
	}

	private void setTextWithPrefix(Label label, TextInputControl field, String prefix, String value) {
		String text = value;

		if(text == null)
			text = "";

		label.setText(prefix + text);
		field.setText(text);
	}

	private void bindField(TextInputControl field, Label label, String prefix) {
		if(field != null && label != null) {
			field.textProperty().addListener((observable, oldText, newText) ->
				label.setText(prefix + newText)
			);
		}
	}

	private void showAllLabels() {
		showStatusLabel();
		showLabel(titleLabel, titleField);
		showLabel(creatorLabel, creatorField);
		showLabel(yearLabel, yearField);
		showLabel(genreLabel, genreField);
		showLabel(seasonNumberLabel, seasonNumberField);
		showLabel(episodeNumberLabel, episodeNumberField);
		showLabel(ratingLabel, ratingField);
		showLabel(reviewLabel, reviewField);
	}

	private void showStatusLabel() {
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
		statusField.setVisible(false);
		statusField.setManaged(false);
	}

	private void showLabel(Label label, TextInputControl field) {
		if(label != null && field != null) {
			label.setVisible(true);
			label.setManaged(true);

			field.setVisible(false);
			field.setManaged(false);
		}
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

	@FXML
	private void editTitle() {
		if(!isAutomaticShow())
			editField(titleLabel, titleField);
	}

	@FXML
	private void editCreator() {
		if(!isAutomaticShow())
			editField(creatorLabel, creatorField);
	}

	@FXML
	private void editYear() {
		if(!isAutomaticShow())
			editField(yearLabel, yearField);
	}

	@FXML
	private void editEpisodeNumber() {
		if(!isAutomaticShow())
			editField(episodeNumberLabel, episodeNumberField);
	}

	@FXML
	private void editStatus() {
		if(editing) {
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
	private void choosePicture() {
		if(episode != null && episodeDAO != null && season != null) {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select Episode Picture");
			chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

			File selectedFile = chooser.showOpenDialog(mediaArt.getScene().getWindow());

			if(selectedFile != null) {
				selectedPicturePath = selectedFile.getAbsolutePath();

				Image image = new Image(selectedFile.toURI().toString());
				StackPane imageContainer = (StackPane)mediaArt.getParent();

				if(!imageContainer.getStyleClass().contains("media-art-border"))
					imageContainer.getStyleClass().add("media-art-border");

				setCenterCroppedImage(mediaArt, image, 390, true);

				episode.setImagePath(selectedPicturePath);

				try {
					int updated = episodeDAO.updateEpisodeImage(episode.getEpisodeId(), selectedPicturePath);

					if(updated > 0)
						hideError();
					else
						showError("The episode picture could not be saved.");
				}
				catch(SQLException e) {
					showError("The episode picture could not be saved.");
					e.printStackTrace();
				}
			}
		}
		else
			showError("Episode information is unavailable.");
	}

	private void fillImage(ImageView imageView) {
		Image image = imageView.getImage();

		if(image != null) {
			double imgRatio = image.getWidth()/image.getHeight();
			double viewRatio = imageView.getFitWidth()/imageView.getFitHeight();

			if(imgRatio > viewRatio) {
				double width = viewRatio/imgRatio;

				imageView.setViewport(new Rectangle2D(
					(image.getWidth() - image.getWidth()*width)/2,
					0,
					image.getWidth()*width,
					image.getHeight()
				));
			}
			else {
				double height = imgRatio/viewRatio;

				imageView.setViewport(new Rectangle2D(
					0,
					(image.getHeight() - image.getHeight()*height)/2,
					image.getWidth(),
					image.getHeight()*height
				));
			}
		}
	}
	
	private boolean isAutomaticShow() {
		return show != null && show.getApiId() > 0;
	}

	private void deleteEpisode() {
		if(isAutomaticShow())
			showError("Episodes from automatically created shows cannot be deleted.");
		else if(episode != null && episodeDAO != null) {
			try {
				int deleted = episodeDAO.deleteEpisode(episode.getMediaId());

				if(deleted == 1)
					goBack();
				else
					showError("The episode could not be deleted.");
			}
			catch(SQLException e) {
				showError("The episode could not be deleted.");
				e.printStackTrace();
			}
		}
	}

	private void goBack() {
		if(show != null && season != null) {
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
				showError("Failed to return to the episode list.");
				e.printStackTrace();
			}
		}
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
		if(dotGridPane != null) {
			double width = dotGridPane.getWidth();
			double height = dotGridPane.getHeight();

			if(width > 0 && height > 0) {
				int visibleColumns = (int)Math.ceil(width/DOT_SPACING) + 4;
				int visibleRows = (int)Math.ceil(height/DOT_SPACING) + 4;
				int requiredDots = visibleColumns*visibleRows;
				Color dotColor = Color.web("#413466", 0.33);

				createDots(requiredDots, dotColor);

				double centerX = width/2.0;
				double centerY = height/2.0;
				double startX = centerX - ((visibleColumns - 1)*DOT_SPACING)/2.0;
				double startY = centerY - ((visibleRows - 1)*DOT_SPACING)/2.0;
				int dotIndex = 0;

				for(int row = 0; row < visibleRows; row++) {
					for(int column = 0; column < visibleColumns; column++) {
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

				for(int i=dotIndex; i < dots.size(); i++)
					dots.get(i).setVisible(false);
			}
		}
	}

	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setVisible(true);
		errorLabel.setManaged(true);
	}

	private void hideError() {
		errorLabel.setText("");
		errorLabel.setVisible(false);
		errorLabel.setManaged(true);
	}
	
	@FXML
	private void editGenre() {
		if(!isAutomaticShow())
			editField(genreLabel, genreField);
	}

	@Override
	protected void loadTableData() {
	}
}