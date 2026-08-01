package application.controller;

import java.io.File;
import java.util.function.Consumer;

import application.model.Game;
import application.model.Media;
import application.model.Show;
import application.model.Song;
import application.model.Status;
import application.model.Type;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.StackPane;

public class AddMediaController {

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
	private HBox yearEndBox;

	@FXML
	private TextField yearEndField;

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
	private VBox showDetailsBox;

	@FXML
	private TextField seasonsField;

	@FXML
	private CheckBox airingCheckBox;

	@FXML
	private ComboBox<Status> statusComboBox;

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

	private static final double DOT_SPACING = 55.0;
	private final ArrayList<Circle> dots = new ArrayList<>();

	private boolean automaticMode;
	private Type mediaType;
	private Media media;
	private String imagePath;
	private Runnable closeAction;
	private Consumer<Media> saveAction;

	@FXML
	public void initialize() {
		statusComboBox.getItems().setAll(Status.values());
		statusComboBox.setValue(Status.PLANNED);

		statusLabel.setVisible(false);
		statusLabel.setManaged(false);

		automaticTitleBox.setVisible(false);
		automaticTitleBox.setManaged(false);

		yearEndBox.setVisible(false);
		yearEndBox.setManaged(false);

		gamePlaytimeBox.setVisible(false);
		gamePlaytimeBox.setManaged(false);

		showDetailsBox.setVisible(false);
		showDetailsBox.setManaged(false);
		
		createDotGrid(dotGridPane, dots, Color.web("#2f3c7f"));

		dotGridPane.widthProperty().addListener((observable, oldValue, newValue) ->
			updateDotGrid(dotGridPane, dots)
		);

		dotGridPane.heightProperty().addListener((observable, oldValue, newValue) ->
			updateDotGrid(dotGridPane, dots)
		);
		
		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(popupContainer.widthProperty());
		clip.heightProperty().bind(popupContainer.heightProperty());
		clip.setArcWidth(80);
		clip.setArcHeight(80);
		popupContainer.setClip(clip);
	}

	public void setAutomaticMode(boolean automaticMode) {
		this.automaticMode = automaticMode;

		manualTitleBox.setVisible(!automaticMode);
		manualTitleBox.setManaged(!automaticMode);

		automaticTitleBox.setVisible(automaticMode);
		automaticTitleBox.setManaged(automaticMode);

		setFieldEditable(yearField, !automaticMode);
		setFieldEditable(yearEndField, !automaticMode);
		setFieldEditable(detailOneField, !automaticMode);
		setFieldEditable(runtimeMinutesField, !automaticMode);
		setFieldEditable(runtimeSecondsField, !automaticMode);
		setFieldEditable(playtimeField, !automaticMode);
		setFieldEditable(seasonsField, !automaticMode);

		airingCheckBox.setDisable(automaticMode);

		if(automaticMode)
			airingCheckBox.setOpacity(1);
	}

	public void setMediaType(Type mediaType) {
		this.mediaType = mediaType;
		setupMediaFields();
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

		Status status = media.getStatus();

		if(status == null)
			status = Status.PLANNED;

		statusComboBox.setValue(status);

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
			playtimeField.setText(String.valueOf(game.getAvgPlaytimeMins()));
		}

		if(media instanceof Show) {
			Show show = (Show)media;

			detailOneField.setText(show.getGenre());
			yearField.setText(String.valueOf(show.getYearStart()));
			yearEndField.setText(String.valueOf(show.getYearEnd()));
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
		songRuntimeBox.setVisible(mediaType == Type.SONG);
		songRuntimeBox.setManaged(mediaType == Type.SONG);

		gamePlaytimeBox.setVisible(mediaType == Type.GAME);
		gamePlaytimeBox.setManaged(mediaType == Type.GAME);

		showDetailsBox.setVisible(mediaType == Type.SHOW);
		showDetailsBox.setManaged(mediaType == Type.SHOW);

		yearEndBox.setVisible(mediaType == Type.SHOW);
		yearEndBox.setManaged(mediaType == Type.SHOW);

		if(mediaType == Type.SONG) {
			yearLabel.setText("Year Released:");
			detailOneLabel.setText("Album:");
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Artist");
		}

		if(mediaType == Type.GAME) {
			yearLabel.setText("Year Released:");
			detailOneLabel.setText("Genre:");
			titleField.setPromptText("Title");
			creatorField.setPromptText("by Developer");
		}

		if(mediaType == Type.SHOW) {
			yearLabel.setText("Year Started:");
			detailOneLabel.setText("Genre:");
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
	private void handleSaveAndAdd() {
		boolean valid = validateInputs();

		if(valid) {
			try {
				Status status = statusComboBox.getValue();
				double rating = 0;

				if(!ratingField.getText().trim().isEmpty())
					rating = Double.parseDouble(ratingField.getText().trim());

				String review = reviewArea.getText().trim();

				if(!automaticMode)
					media = createMediaFromFields();

				media.setStatus(status);
				media.setUserRating(rating);
				media.setReview(review);

				if(saveAction != null)
					saveAction.accept(media);

				if(closeAction != null)
					closeAction.run();
			}
			catch(NumberFormatException e) {
				showStatus("One or more number fields are invalid.", true);
			}
			catch(Exception e) {
				showStatus("Failed to add media.", true);
				e.printStackTrace();
			}
		}
	}

	private Media createMediaFromFields() {
		String title = titleField.getText().trim();
		String creator = creatorField.getText().trim();
		int year = Integer.parseInt(yearField.getText().trim());
		String detailOne = detailOneField.getText().trim();
		Status status = statusComboBox.getValue();
		double rating = 0;
		String review = reviewArea.getText().trim();

		if(!ratingField.getText().trim().isEmpty())
			rating = Double.parseDouble(ratingField.getText().trim());

		if(mediaType == Type.SONG) {
			int minutes = Integer.parseInt(runtimeMinutesField.getText().trim());
			int seconds = Integer.parseInt(runtimeSecondsField.getText().trim());
			int runtimeSeconds = minutes * 60 + seconds;

			return new Song(title, status, rating, detailOne, creator, year, runtimeSeconds, review, imagePath);
		}

		if(mediaType == Type.GAME) {
			int playtime = Integer.parseInt(playtimeField.getText().trim());

			return new Game(title, creator, year, status, rating, review, detailOne, playtime, imagePath);
		}

		if(mediaType == Type.SHOW) {
			int yearEnd = Integer.parseInt(yearEndField.getText().trim());
			int seasons = Integer.parseInt(seasonsField.getText().trim());
			boolean airing = airingCheckBox.isSelected();

			return new Show(title, creator, year, yearEnd, status, rating, review, detailOne, seasons, airing, imagePath);
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
		else if(yearField.getText().trim().isEmpty()) {
			showStatus("Year cannot be empty.", true);
			valid = false;
		}
		else if(detailOneField.getText().trim().isEmpty()) {
			showStatus(detailOneLabel.getText().replace(":", "") + " cannot be empty.", true);
			valid = false;
		}
		else if(statusComboBox.getValue() == null) {
			showStatus("Select a status.", true);
			valid = false;
		}
		else {
			try {
				int year = Integer.parseInt(yearField.getText().trim());

				if(year <= 0) {
					showStatus("Year must be greater than zero.", true);
					valid = false;
				}
			}
			catch(NumberFormatException e) {
				showStatus("Year must be a number.", true);
				valid = false;
			}
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
			try {
				int yearStart = Integer.parseInt(yearField.getText().trim());
				int yearEnd = Integer.parseInt(yearEndField.getText().trim());
				int seasons = Integer.parseInt(seasonsField.getText().trim());

				if(yearEnd < yearStart) {
					showStatus("Year ended cannot be earlier than year started.", true);
					valid = false;
				}
				else if(seasons <= 0) {
					showStatus("Number of seasons must be greater than zero.", true);
					valid = false;
				}
			}
			catch(NumberFormatException e) {
				showStatus("Show details must contain valid numbers.", true);
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
	private void handleCancel() {
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
				dot.setOpacity(0.35);
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
}