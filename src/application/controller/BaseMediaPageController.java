package application.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import application.dao.UserDAO;
import application.model.Type;
import application.model.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
//import javafx.geometry.Pos;
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

public abstract class BaseMediaPageController {

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


	protected void initializeBase() {
		setupImages();
		setupUserInformation();
		setupBackground();
		setupMediaLabelEffect();
		setupSettings();
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
		this.userDAO = new UserDAO(conn);
		loadProfilePicture();
		loadTableData();
	}

	protected abstract void loadTableData();

	protected void setupView(Type mediaType) {
		this.mediaType = mediaType;

		mediaLabel.setText(mediaType.getTitle());

		rootStackPane.getStyleClass().removeAll("theme-songs", "theme-games", "theme-shows");
		rootStackPane.getStyleClass().add(mediaType.getStyleClass());
		
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

	private void setupImages() {
		mediaVaultLogo.setImage(loadImage("/resources/application/images/logos/logo.png"));
		mediaVaultTitle.setImage(loadImage("/resources/application/images/logos/title.png"));
		settingsIcon.setImage(loadImage("/resources/application/images/icons/settings-gear-svgrepo-com.png"));
		loadDefaultProfilePicture();

		mediaVaultLogo.setPreserveRatio(true);
		mediaVaultTitle.setPreserveRatio(true);
		profileAvatar.setPreserveRatio(true);
		settingsIcon.setPreserveRatio(true);
	}

	private void setupUserInformation() {
		userName.setText(UserSession.getCurrentUsername());
	}

	private void setupBackground() {
		backgroundCanvas.widthProperty().bind(rootStackPane.widthProperty());
		backgroundCanvas.heightProperty().bind(rootStackPane.heightProperty());
		backgroundCanvas.setMouseTransparent(true);
		backgroundCanvas.setCache(true);

		rootStackPane.widthProperty().addListener((observable, oldValue, newValue) ->
			resizeDelay.playFromStart()
		);
	
		rootStackPane.heightProperty().addListener((observable, oldValue, newValue) ->
			resizeDelay.playFromStart()
		);
	}

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

	protected void openSettings() {
		switchScene("/resources/application/fxml/Settings.fxml");
	}

	protected void makeNavigationButton(Button button, String iconPath, String text, Runnable action) {
		registerNavigationButton(button, iconPath, text, action);
	}

	private void registerNavigationButton(Button button, String iconPath, String text, Runnable action) {
		setButtonIcon(button, iconPath);

		button.setText(null);
		button.setOnAction(event -> {
			selectNavigationButton(button);
			action.run();
		});

		navigationButtons.add(new NavigationButton(button, text));
	}

	protected void initializeNavigationBar() {
		clipRect = new Rectangle();
		clipRect.widthProperty().bind(extendableNavigationPane.widthProperty());
		extendableNavigationPane.setClip(clipRect);

		selectedButtonShadow = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLUE, 7, 0.2, 0, 1);

		extendableNavigationPane.setOnMouseEntered(event -> showNavigationPane());
		extendableNavigationPane.setOnMouseExited(event -> hideNavigationPane());

		hideNavigationPane();
	}

	private void showNavigationPane() {
		Timeline timeline = new Timeline();

		KeyValue clipHeight = new KeyValue(clipRect.heightProperty(), extendableNavigationPane.getHeight());
		KeyValue clipPosition = new KeyValue(clipRect.translateYProperty(), 0);
		KeyValue panePosition = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset);
		KeyFrame paneFrame = new KeyFrame(Duration.millis(100), createBouncingEffect(extendableNavigationPane.getHeight()), clipHeight, clipPosition, panePosition);

		timeline.getKeyFrames().add(paneFrame);

		for(int i=0; i < navigationButtons.size(); i++) {
			NavigationButton navigationButton = navigationButtons.get(i);
			double offset = getExpandedButtonOffset(i);

			navigationButton.button.setText(navigationButton.text);

			KeyValue buttonPosition = new KeyValue(navigationButton.button.translateXProperty(), offset);
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(200), buttonPosition));
		}

		timeline.play();
	}

	private void hideNavigationPane() {
		if(clipRect != null) {
			Timeline timeline = new Timeline();

			KeyValue clipHeight = new KeyValue(clipRect.heightProperty(), 150);
			KeyValue panePosition = new KeyValue(extendableNavigationPane.translateYProperty(), navigationYOffset + 10);
			KeyFrame paneFrame = new KeyFrame(Duration.millis(200), clipHeight, panePosition);

			timeline.getKeyFrames().add(paneFrame);

			for(NavigationButton navigationButton : navigationButtons) {
				navigationButton.button.setText(null);

				KeyValue buttonPosition = new KeyValue(navigationButton.button.translateXProperty(), 0);
				timeline.getKeyFrames().add(new KeyFrame(Duration.millis(200), buttonPosition));
			}

			timeline.play();
		}
	}

	private double getExpandedButtonOffset(int index) {
		double middle = (navigationButtons.size() - 1)/2.0;
		return (index - middle)*navigationButtonOffset;
	}

	private void selectNavigationButton(Button selectedButton) {
		for(NavigationButton navigationButton : navigationButtons) {
			navigationButton.button.setEffect(null);
		}

		selectedButton.setEffect(selectedButtonShadow);
	}

	private javafx.event.EventHandler<javafx.event.ActionEvent> createBouncingEffect(double height) {
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

	private void setButtonIcon(Button button, String path) {
		ImageView imageView = new ImageView(loadImage(path));

		imageView.setFitWidth(72);
		imageView.setFitHeight(72);
		imageView.setPreserveRatio(true);

		button.setGraphic(imageView);
		button.setContentDisplay(ContentDisplay.TOP);
	}
	
	protected void switchScene(String fxmlPath) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
			Parent root = loader.load();

			passConnection(loader.getController());

			Stage stage = (Stage)rootPane.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			System.err.println("Unable to load FXML file: " + fxmlPath);
			e.printStackTrace();
		}
	}

	private void passConnection(Object controller) {
		if(controller != null && conn != null) {
			try {
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

	private void loadProfilePicture() {
		try {
			String path = userDAO.getProfilePicture(UserSession.getCurrentUserId());

			if(path != null && !path.isBlank()) {
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

	private void loadDefaultProfilePicture() {
		profileAvatar.setImage(loadImage("/resources/application/images/default/default-profile.png"));
	}

	protected Image loadImage(String path) {
		return new Image(getClass().getResourceAsStream(path));
	}
	
	protected <T> void handleDoubleClick(TableView<T> table, Consumer<T> action) {
		final long clickLimit = 500;

		table.setOnMouseClicked(event -> {
			if(event.getButton() == MouseButton.PRIMARY) {
				T clickedItem = table.getSelectionModel().getSelectedItem();

				if(clickedItem != null) {
					long currentTime = System.currentTimeMillis();

					if(clickedItem != pendingItem) {
						pendingItem = clickedItem;
						pendingClickTime = currentTime;
					}
					else if(currentTime - pendingClickTime <= clickLimit) {
						action.accept(clickedItem);
						pendingItem = null;
						pendingClickTime = 0;
					}
					else {
						table.getSelectionModel().clearSelection();
						pendingItem = null;
						pendingClickTime = 0;
					}
				}
			}
		});
	}

	private static class NavigationButton {
		private final Button button;
		private final String text;

		private NavigationButton(Button button, String text) {
			this.button = button;
			this.text = text;
		}
	}
}