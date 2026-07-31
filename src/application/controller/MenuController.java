package application.controller;

import java.io.IOException;
import java.sql.Connection;

//import java.util.List;
import javafx.animation.RotateTransition;
import java.util.ArrayList;
import application.model.Type;
import application.model.UserSession;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;
import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.application.Platform;
import java.io.File;
import java.sql.SQLException;
import application.dao.UserDAO;
import javafx.geometry.Rectangle2D;

public class MenuController {
    
	@FXML
	private StackPane songsContainer;
	
	@FXML
	private Rectangle songsBackground;
	
	@FXML
    private Pane songsDotsPane;
    
    @FXML
    private VBox songsContent;

    @FXML
    private Canvas songsIconCanvas;
    
    @FXML
    private Label songsLabel;
    
	@FXML
	private ImageView songsIcon;
    
	@FXML
	private StackPane gamesContainer;
	
	@FXML
	private Rectangle gamesBackground;
	
	@FXML
    private Pane gamesDotsPane;
    
    @FXML
    private VBox gamesContent;

    @FXML
    private Canvas gamesIconCanvas;
    
    @FXML
    private Label gamesLabel;
    
	@FXML
	private ImageView gamesIcon;
    
	@FXML
	private StackPane showsContainer;
	
	@FXML
	private Rectangle showsBackground;
	
	@FXML
    private Pane showsDotsPane;
    
    @FXML
    private VBox showsContent;

    @FXML
    private Canvas showsIconCanvas;
    
    @FXML
    private Label showsLabel;
    
	@FXML
	private ImageView showsIcon;
    
    @FXML 
    private ImageView mediaVaultLogo;
    
    @FXML
    private ImageView mediaVaultTitle;
    
    @FXML
    private HBox menuContainer;
    
    @FXML 
    private ImageView profileAvatar;
    
    @FXML
    private BorderPane rootBorderPane;
    
    @FXML
    private StackPane rootStackPane;
    
    @FXML
    private ImageView settingsIcon;
    
    @FXML
    private Label userName;
    
    private final InnerShadow hoverShadow = new InnerShadow(18, Color.rgb(0, 0, 0, 0.35));
    private final DropShadow iconHighlight = new DropShadow(35, Color.rgb(255, 255, 255, 0.16));
    
	private final ArrayList<Circle> songsDots = new ArrayList<>();
	private final ArrayList<Circle> gamesDots = new ArrayList<>();
	private final ArrayList<Circle> showsDots = new ArrayList<>();

    private static final double DOT_SPACING = 55.0;
    private Connection conn;
    

	@FXML
	public void initialize() {
		
		String username = UserSession.getCurrentUsername();
		
		mediaVaultLogo.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png")));
		mediaVaultTitle.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/logos/title.png")));
		settingsIcon.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/icons/settings-gear-svgrepo-com.png")));

		songsIcon.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/icons/songs-icon.png")));
		gamesIcon.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/icons/games-icon.png")));
		showsIcon.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/icons/shows-icon.png")));
		
		userName.setText(username);
		
		menuContainer.setAlignment(Pos.CENTER);

		bindTile(songsContainer, songsBackground, songsDotsPane);
		bindTile(gamesContainer, gamesBackground, gamesDotsPane);
		bindTile(showsContainer, showsBackground, showsDotsPane);

		createDotGrid(songsDotsPane, songsDots, Color.web("#132F44", 0.06));
		createDotGrid(gamesDotsPane, gamesDots, Color.web("#131B44", 0.06));
		createDotGrid(showsDotsPane, showsDots, Color.web("#000C4C", 0.06));

		addDotResizeListeners(songsDotsPane, songsDots);
		addDotResizeListeners(gamesDotsPane, gamesDots);
		addDotResizeListeners(showsDotsPane, showsDots);

		clipTile(songsContainer);
		clipTile(gamesContainer);
		clipTile(showsContainer);
	}
	
	public void setConnection(Connection conn) {
		this.conn = conn;
		loadProfilePicture();
	}
	
	private void bindTile(StackPane container, Rectangle background, Pane dotsPane) {
		background.widthProperty().bind(container.widthProperty());
		background.heightProperty().bind(container.heightProperty());

		dotsPane.prefWidthProperty().bind(container.widthProperty());
		dotsPane.prefHeightProperty().bind(container.heightProperty());
		dotsPane.maxWidthProperty().bind(container.widthProperty());
		dotsPane.maxHeightProperty().bind(container.heightProperty());
	}

	private void addDotResizeListeners(Pane pane, ArrayList<Circle> dots) {
		pane.widthProperty().addListener((observable, oldValue, newValue) -> updateDotGrid(pane, dots));
		pane.heightProperty().addListener((observable, oldValue, newValue) -> updateDotGrid(pane, dots));
	}

	private void clipTile(StackPane container) {
		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(container.widthProperty());
		clip.heightProperty().bind(container.heightProperty());
		clip.setArcWidth(20.0);
		clip.setArcHeight(20.0);
		container.setClip(clip);
	}
    
	@FXML
	private void handleSettingsClick(MouseEvent event) {
		event.consume();

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/Settings.fxml"));
			Parent popup = loader.load();

			StackPane overlay = new StackPane();
			overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
			overlay.setPickOnBounds(true);
			overlay.getChildren().add(popup);

			SettingsController controller = loader.getController();
			controller.setConnection(conn);
			controller.setCloseAction(() -> closeSettingsPopup(overlay));
			controller.setProfileUpdatedAction(this::loadProfilePicture);

			popup.setOnMouseClicked(e -> e.consume());
			overlay.setOnMouseClicked(e -> {
				e.consume();
				closeSettingsPopup(overlay);
			});

			Parent currentRoot = rootBorderPane.getScene().getRoot();

			if(currentRoot instanceof StackPane) {
				StackPane root = (StackPane)currentRoot;
				rootBorderPane.setEffect(new GaussianBlur(6));
				root.getChildren().add(overlay);
				StackPane.setAlignment(popup, Pos.CENTER);
			}
			else {
				Platform.runLater(() -> {
					StackPane newRoot = new StackPane();
					rootBorderPane.setEffect(new GaussianBlur(6));
					newRoot.getChildren().addAll(rootBorderPane, overlay);
					rootBorderPane.getScene().setRoot(newRoot);
					StackPane.setAlignment(popup, Pos.CENTER);
				});
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadProfilePicture() {
		try {
			UserDAO userDAO = new UserDAO(conn);
			String path = userDAO.getProfilePicture(UserSession.getCurrentUserId());

			if(path != null && !path.isBlank()) {
				File file = new File(path);

				if(file.exists()) {
					Image image = new Image(file.toURI().toString());
					setCircularProfileImage(profileAvatar, image);
				}
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
		Image image = new Image(getClass().getResourceAsStream("/resources/application/images/default/default-profile.png"));
		setCircularProfileImage(profileAvatar, image);
	}
	
	private void closeSettingsPopup(StackPane overlay) {
		Platform.runLater(() -> {
			if(overlay.getParent() instanceof StackPane) {
				StackPane root = (StackPane)overlay.getParent();
				root.getChildren().remove(overlay);
			}

			rootBorderPane.setEffect(null);
		});
	}
	
    @FXML
    private void handleMouseEntered(MouseEvent event) {
    	StackPane container = (StackPane) event.getSource();

    	ScaleTransition scale = new ScaleTransition(Duration.millis(120), container);
    	scale.setToX(0.992);
    	scale.setToY(0.992);
    	scale.play();

    	container.setOpacity(0.99);
    	
    	container.setEffect(hoverShadow);

		if(container == songsContainer)
		{
			expandDots(songsDotsPane);
			highlightIcon(songsIcon);
		}
		else if(container == gamesContainer)
		{
			expandDots(gamesDotsPane);
			highlightIcon(gamesIcon);
		}
		else if(container == showsContainer)
		{
			expandDots(showsDotsPane);
			highlightIcon(showsIcon);
		}
    }
    
	@FXML
	private void handleMouseExited(MouseEvent event) {
		StackPane container = (StackPane)event.getSource();

		ScaleTransition scale = new ScaleTransition(Duration.millis(120), container);
		scale.setToX(1.0);
		scale.setToY(1.0);
		scale.play();

		container.setOpacity(1.0);
		container.setEffect(null);

		if(container == songsContainer) {
			shrinkDots(songsDotsPane);
			removeIconHighlight(songsIcon);
		}
		else if(container == gamesContainer) {
			shrinkDots(gamesDotsPane);
			removeIconHighlight(gamesIcon);
		}
		else if(container == showsContainer) {
			shrinkDots(showsDotsPane);
			removeIconHighlight(showsIcon);
		}
	}
	
	@FXML
	private void handleSettingsRotate(MouseEvent event) {
		ImageView settings = (ImageView)event.getSource();

		RotateTransition rotate = new RotateTransition(Duration.millis(250), settings);
		rotate.setToAngle(45);
		highlightIcon(settingsIcon);
		rotate.play();
	}
	
	@FXML
	private void handleSettingUnrotate(MouseEvent event) {
		ImageView settings = (ImageView)event.getSource();

		RotateTransition rotate = new RotateTransition(Duration.millis(250), settings);
		rotate.setToAngle(0);
		removeIconHighlight(settingsIcon);
		rotate.play();
	}
    
	@FXML
	private void handleTileClick(MouseEvent event) {
		Object source = event.getSource();
		Type mediaType = null;

		if(source == songsContainer)
			mediaType = Type.SONG;
		else if(source == gamesContainer)
			mediaType = Type.GAME;
		else if(source == showsContainer)
			mediaType = Type.SHOW;

		if(mediaType != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsScene.fxml"));
				Parent root = loader.load();

				MediaPlaylistsController controller = loader.getController();
				controller.setConnection(conn);
				controller.setupView(mediaType);

				Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
				stage.getScene().setRoot(root);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
    
	private void createDotGrid(Pane pane, ArrayList<Circle> dots, Color color) {
		int columns = 15;
		int rows = 18;

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
    
    private void expandDots(Pane dotsPane) {
    	ScaleTransition scale = new ScaleTransition(Duration.millis(125), dotsPane);
    	scale.setToX(1.25);
    	scale.setToY(1.25);
    	scale.play();
    }

    private void shrinkDots(Pane dotsPane) {
    	ScaleTransition scale = new ScaleTransition(Duration.millis(125), dotsPane);
    	scale.setToX(1.0);
    	scale.setToY(1.0);
    	scale.play();
    }
    
	private void highlightIcon(ImageView icon) {
		icon.setEffect(iconHighlight);

		ScaleTransition scale = new ScaleTransition(Duration.millis(120), icon);
		scale.play();
	}

	private void removeIconHighlight(ImageView icon) {
		icon.setEffect(null);

		ScaleTransition scale = new ScaleTransition(Duration.millis(120), icon);
		scale.play();
	}
	
	private void setCircularProfileImage(ImageView imageView, Image image) {
		imageView.setFitWidth(65);
		imageView.setFitHeight(65);
		imageView.setPreserveRatio(false);
		imageView.setSmooth(true);

		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double cropSize = Math.min(imageWidth, imageHeight);

		double cropX = (imageWidth - cropSize) / 2.0;
		double cropY = (imageHeight - cropSize) / 2.0;

		imageView.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		imageView.setImage(image);

		Circle clip = new Circle(32.5, 32.5, 32.5);
		imageView.setClip(clip);
	}
 
}