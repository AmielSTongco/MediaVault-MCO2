package application.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import application.dao.UserDAO;
import application.model.Type;
import application.model.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TableView;
import java.util.function.Consumer;
import javafx.scene.input.MouseButton;
import java.net.URL;
import javafx.geometry.Rectangle2D;

public abstract class BaseMediaPageController {
	
	/*
	 * The superclass which basically controls the background and
	 * navigation buttons of the scene. Almost all other controllers
	 * are subclasses of this one (as it is the foundation for each scene)
	 */

	@FXML
	protected StackPane rootStackPane;

	@FXML
	protected BorderPane rootPane;

	@FXML
	protected Canvas backgroundCanvas;

	@FXML
	protected ImageView mediaVaultLogo;

	@FXML
	protected ImageView mediaVaultTitle;

	@FXML
	protected ImageView mediaLogo;

	@FXML
	protected ImageView profileAvatar;

	@FXML
	protected ImageView settingsIcon;

	@FXML
	protected Label mediaLabel;

	@FXML
	protected Label userName;

	@FXML
	protected HBox extendableNavigationPane;

	protected Connection conn;
	protected UserDAO userDAO;
	protected Type mediaType;

	private Rectangle clipRect;
	private DropShadow selectedButtonShadow;
	private final List<NavigationButton> navigationButtons = new ArrayList<>();
	private final PauseTransition resizeDelay = new PauseTransition(Duration.millis(220));

	private static final int navigationYOffset = 10;
	private static final double navigationButtonOffset = 10;
	
	private Object pendingItem;
	private long pendingClickTime;

	/**
	 * Initializes shared images, user information, background, effects, and settings behavior.
	 */
	protected void initializeBase() {
		setupImages();
		setupUserInformation();
		setupBackground();
		setupMediaLabelEffect();
		setupSettings();
	}
	
	/**
	 * Sets database connection and loads user-specific data.
	 *
	 * @param conn active database connection
	 */
	public void setConnection(Connection conn) {
		this.conn = conn;
		this.userDAO = new UserDAO(conn);
		loadProfilePicture();
		loadTableData();
	}
	
	/**
	 * Loads data required by the current media page.
	 */
	protected abstract void loadTableData();
	
	/**
	 * Applies media-specific title, theme, and icon.
	 *
	 * @param mediaType selected media type
	 */
	protected void setupView(Type mediaType) {
		this.mediaType = mediaType;

		mediaLabel.setText(mediaType.getTitle());
		
		// Applies media color theme
		rootStackPane.getStyleClass().removeAll("theme-songs", "theme-games", "theme-shows");
		rootStackPane.getStyleClass().add(mediaType.getStyleClass());
		
		// Loads media icon
		if(mediaLogo != null)
		{
			mediaLogo.setPreserveRatio(true);
			switch(mediaType) {
				case SONG:
					mediaLogo.setImage(loadImage("/resources/application/images/icons/songs-icon.png"));
					break;
				case GAME:
					mediaLogo.setImage(loadImage("/resources/application/images/icons/games-icon.png"));
					break;
				case SHOW:
					mediaLogo.setImage(loadImage("/resources/application/images/icons/shows-icon.png"));
					break;
			}
		}
	}
	
	/**
	 * Loads shared navigation and profile images.
	 */
	private void setupImages() {
		mediaVaultLogo.setImage(loadImage("/resources/application/images/logos/logo.png"));
		mediaVaultTitle.setImage(loadImage("/resources/application/images/logos/title.png"));
		settingsIcon.setImage(loadImage("/resources/application/images/icons/settings-gear-svgrepo-com.png"));
		loadDefaultProfilePicture();
		
		// Preserves image proportions
		mediaVaultLogo.setPreserveRatio(true);
		mediaVaultTitle.setPreserveRatio(true);
		profileAvatar.setPreserveRatio(true);
		settingsIcon.setPreserveRatio(true);
	}
	
	/**
	 * Displays the current username.
	 */
	private void setupUserInformation() {
		userName.setText(UserSession.getCurrentUsername());
	}
	
	/**
	 * Configures background canvas resizing.
	 */
	private void setupBackground() {
		backgroundCanvas.widthProperty().bind(rootStackPane.widthProperty());
		backgroundCanvas.heightProperty().bind(rootStackPane.heightProperty());
		backgroundCanvas.setMouseTransparent(true);
		backgroundCanvas.setCache(true);
		
		// Delays resizing work until layout stabilizes
		rootStackPane.widthProperty().addListener((observable, oldValue, newValue) -> resizeDelay.playFromStart());
		rootStackPane.heightProperty().addListener((observable, oldValue, newValue) -> resizeDelay.playFromStart());
	}
	
	/**
	 * Applies lighting and shadow effects to the media label.
	 */
	private void setupMediaLabelEffect() {
		DropShadow shadow = new DropShadow();
		shadow.setRadius(10);
		shadow.setOffsetY(5);
		shadow.setColor(Color.color(0, 0, 0, 0.4));

		Light.Distant light = new Light.Distant();
		light.setAzimuth(-135);

		Lighting lighting = new Lighting();
		lighting.setLight(light);
		lighting.setDiffuseConstant(1.45);
		lighting.setSurfaceScale(1);

		shadow.setInput(lighting);
		mediaLabel.setEffect(shadow);
	}
	
	/**
	 * Configures settings icon hover and click behavior.
	 */
	private void setupSettings() {
		settingsIcon.setOnMouseEntered(event -> settingsIcon.setScaleX(1.08));
		settingsIcon.setOnMouseEntered(event -> {
			settingsIcon.setScaleX(1.08);
			settingsIcon.setScaleY(1.08);
		});

		settingsIcon.setOnMouseExited(event -> {
			settingsIcon.setScaleX(1);
			settingsIcon.setScaleY(1);
		});

		settingsIcon.setOnMouseClicked(event -> openSettings());
	}
	
	/**
	 * Opens the settings scene.
	 */
	protected void openSettings() {
		switchScene("/resources/application/fxml/Settings.fxml");
	}
	
	/*
	 * DISCLAIMER: The following programmed animation for the navigation
	 * buttons are adapted from the following link
	 * (https://stevenschwenke.de/extendableNavigationPaneInJavaFX)
	 */
	
	/**
	 * Creates and registers a navigation button.
	 *
	 * @param button target button
	 * @param iconPath icon resource path
	 * @param text expanded button text
	 * @param action action executed when pressed
	 */
	protected void makeNavigationButton(Button button, String iconPath, String text, Runnable action) {
		registerNavigationButton(button, iconPath, text, action);
	}
	
	/**
	 * Configures a navigation button and stores its expanded text.
	 *
	 * @param button target button
	 * @param iconPath icon resource path
	 * @param text expanded button text
	 * @param action action executed when pressed
	 */
	private void registerNavigationButton(Button button, String iconPath, String text, Runnable action) {
		setButtonIcon(button, iconPath);

		button.setText(null);
		button.setOnAction(event -> {
			selectNavigationButton(button);
			action.run();
		});

		navigationButtons.add(new NavigationButton(button, text));
	}
	
	/**
	 * Initializes navigation pane clipping, effects, and hover behavior.
	 */
	protected void initializeNavigationBar() {
		clipRect = new Rectangle();
		clipRect.widthProperty().bind(extendableNavigationPane.widthProperty());
		extendableNavigationPane.setClip(clipRect);

		selectedButtonShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLUE, 7, 0.2, 0, 1);

		extendableNavigationPane.setOnMouseEntered(event -> showNavigationPane());
		extendableNavigationPane.setOnMouseExited(event -> hideNavigationPane());

		hideNavigationPane();
	}
	
	/**
	 * Expands the navigation pane and reveals button labels.
	 */
	private void showNavigationPane() {
		Timeline timeline = new Timeline();
		
		// Expands pane clip
		KeyValue clipHeight = new KeyValue(clipRect.heightProperty(), extendableNavigationPane.getHeight());
		KeyValue clipPosition = new KeyValue(clipRect.translateYProperty(), 0);
		KeyValue panePosition = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset);
		KeyFrame paneFrame = new KeyFrame(Duration.millis(100), createBouncingEffect(extendableNavigationPane.getHeight()), clipHeight, clipPosition, panePosition);

		timeline.getKeyFrames().add(paneFrame);
		
		// Spreads navigation buttons
		for(int i=0; i < navigationButtons.size(); i++)
		{
			NavigationButton navigationButton = navigationButtons.get(i);
			double offset = getExpandedButtonOffset(i);

			navigationButton.button.setText(navigationButton.text);

			KeyValue buttonPosition = new KeyValue(navigationButton.button.translateXProperty(), offset);
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(200), buttonPosition));
		}

		timeline.play();
	}
	
	/**
	 * Collapses the navigation pane and hides button labels.
	 */
	private void hideNavigationPane() {
		if(clipRect != null) {
			Timeline timeline = new Timeline();
			
			// Collapses pane clip
			KeyValue clipHeight = new KeyValue(clipRect.heightProperty(), 150);
			KeyValue panePosition = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset + 10);
			KeyFrame paneFrame = new KeyFrame(Duration.millis(200), clipHeight, panePosition);

			timeline.getKeyFrames().add(paneFrame);
			
			// Returns buttons to original positions
			for(NavigationButton navigationButton : navigationButtons)
			{
				navigationButton.button.setText(null);
				KeyValue buttonPosition = new KeyValue(navigationButton.button.translateXProperty(), 0);
				timeline.getKeyFrames().add(new KeyFrame(Duration.millis(200), buttonPosition));
			}

			timeline.play();
		}
	}
	
	/**
	 * Calculates a navigation button's expanded horizontal offset.
	 *
	 * @param index button index
	 * @return horizontal offset
	 */
	private double getExpandedButtonOffset(int index) {
		double middle = (navigationButtons.size() - 1)/2.0;
		return (index - middle)*navigationButtonOffset;
	}
	
	/**
	 * Applies the selected effect to a navigation button.
	 *
	 * @param selectedButton selected navigation button
	 */
	private void selectNavigationButton(Button selectedButton) {
		// Clears previous selection
		for(NavigationButton navigationButton : navigationButtons)
		{
			navigationButton.button.setEffect(null);
		}

		selectedButton.setEffect(selectedButtonShadow);
	}
	
	/**
	 * Creates the bounce animation used when expanding the navigation pane.
	 *
	 * @param height expanded pane height
	 * @return event handler that starts the animation
	 */
	private EventHandler<javafx.event.ActionEvent> createBouncingEffect(double height) {
		Timeline timelineBounce = new Timeline();
		timelineBounce.setCycleCount(2);
		timelineBounce.setAutoReverse(true);

		KeyValue heightValue = new KeyValue(clipRect.heightProperty(), height - 15);
		KeyValue clipPosition = new KeyValue(clipRect.translateYProperty(), 15);
		KeyValue panePosition = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset - 15);
		KeyFrame frame = new KeyFrame(Duration.millis(100), heightValue, clipPosition, panePosition);

		timelineBounce.getKeyFrames().add(frame);

		return event -> timelineBounce.play();
	}
	
	/**
	 * Sets the icon displayed by a navigation button.
	 *
	 * @param button target button
	 * @param path icon resource path
	 */
	protected void setButtonIcon(Button button, String path) {
		ImageView imageView = new ImageView(loadImage(path));

		imageView.setFitWidth(72);
		imageView.setFitHeight(72);
		imageView.setPreserveRatio(true);

		button.setGraphic(imageView);
		button.setContentDisplay(ContentDisplay.TOP);
	}
	
	/**
	 * Loads and switches to another FXML scene.
	 *
	 * @param fxmlPath FXML resource path
	 */
	protected void switchScene(String fxmlPath) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
			Parent root = loader.load();
			
			// Passes current database connection to the next scene
			passConnection(loader.getController());

			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			System.err.println("Unable to load FXML file: " + fxmlPath);
			e.printStackTrace();
		}
	}
	
	/**
	 * Passes the current database connection to a controller when supported.
	 *
	 * @param controller destination controller
	 */
	private void passConnection(Object controller) {
		if(controller != null && conn != null)
		{
			try {
				// Searches for compatible connection setter
				Method method = controller.getClass().getMethod("setConnection", Connection.class);
				method.invoke(controller, conn);
			}
			catch(NoSuchMethodException e) {
				System.out.println(controller.getClass().getSimpleName() + " does not require a connection.");
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Loads the current user's saved profile picture.
	 */
	private void loadProfilePicture() {
		try {
			String path = userDAO.getProfilePicture(UserSession.getCurrentUserId());

			if(path != null && !path.isBlank())
			{
				File file = new File(path);

				if(file.exists())
					profileAvatar.setImage(new Image(file.toURI().toString()));
				else
					loadDefaultProfilePicture();
			}
			else
				loadDefaultProfilePicture();
		}
		catch(SQLException e) {
			loadDefaultProfilePicture();
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the default profile picture.
	 */
	private void loadDefaultProfilePicture() {
		profileAvatar.setImage(loadImage("/resources/application/images/default/default-profile.png"));
	}
	
	/**
	 * Loads an image from an online URL, local file, or application resource.
	 *
	 * @param path image path to load
	 * @return loaded image, or null when unavailable
	 */
	protected Image loadImage(String path) {
		if(path == null || path.isBlank())
			return null;

		try {
			if(path.startsWith("http://") || path.startsWith("https://"))
				return new Image(path, true);
			
			// Loads local file
			File file = new File(path);

			if(file.exists())
				return new Image(file.toURI().toString());
			
			// Loads application resource
			URL resource = getClass().getResource(path);

			if(resource != null)
				return new Image(resource.toExternalForm());
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Executes an action after a valid table row double-click.
	 *
	 * @param table target table
	 * @param action action receiving the clicked item
	 * @param <T> table item type
	 */
	protected <T> void handleDoubleClick(TableView<T> table, Consumer<T> action) {
		final long clickLimit = 500;
		
		/* Inspiration for use of lambda is from (https://medium.com/@nagarjun_nagesh/lambdas-in-event-driven-programming-fd448541991e) */
		table.setOnMouseClicked(event -> {
			if(event.getButton() == MouseButton.PRIMARY)
			{
				T clickedItem = table.getSelectionModel().getSelectedItem();

				if(clickedItem != null)
				{
					long currentTime = System.currentTimeMillis();

					if(clickedItem != pendingItem)
					{
						pendingItem = clickedItem;
						pendingClickTime = currentTime;
					}
					else if(currentTime - pendingClickTime <= clickLimit)
					{
						action.accept(clickedItem);
						pendingItem = null;
						pendingClickTime = 0;
					}
					else
					{
						table.getSelectionModel().clearSelection();
						pendingItem = null;
						pendingClickTime = 0;
					}
				}
			}
		});
	}
	
	/**
	 * Stores a navigation button and its expanded text.
	 */
	private static class NavigationButton {
		private final Button button;
		private final String text;
		
		/**
		 * Creates a navigation button entry.
		 *
		 * @param button navigation button
		 * @param text expanded button text
		 */
		private NavigationButton(Button button, String text) {
			this.button = button;
			this.text = text;
		}
	}
	
	/**
	 * Displays an image using a centered square crop and rounded corners.
	 *
	 * @param imageView target image view
	 */
	protected void cropImage(ImageView imageView) {
		Image image = imageView.getImage();

		if(image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
			imageView.setViewport(null);
			return;
		}

		double size = 108;

		imageView.setFitWidth(size);
		imageView.setFitHeight(size);
		
		// Calculates centered square viewport
		double cropSize = Math.min(image.getWidth(), image.getHeight());
		double cropX = (image.getWidth() - cropSize)/2;
		double cropY = (image.getHeight() - cropSize)/2;

		imageView.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		imageView.setPreserveRatio(false);
		
		// Clips rounded corners
		Rectangle clip = new Rectangle(size, size);
		clip.setArcWidth(24);
		clip.setArcHeight(24);
		imageView.setClip(clip);
	}
}