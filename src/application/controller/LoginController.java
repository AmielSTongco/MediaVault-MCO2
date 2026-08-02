package application.controller;

import java.io.IOException;
import application.dao.UserDAO;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.ImageView;
import javafx.scene.effect.GaussianBlur;
//import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import java.sql.Connection;
import javafx.animation.PauseTransition;
import javafx.application.Platform;

public class LoginController {
	
	/*
	 * This class controls the scene which lets the
	 * user log-in as existing user or sign-up as a new user
	 * or exit the program
	 */
	
    @FXML
    private StackPane loginBox;
	
    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private HBox mainContainer;

    @FXML
    private Rectangle loginHighlight;
    
    @FXML
    private Rectangle signupHighlight;

    @FXML
    private Rectangle exitHighlight;

    @FXML
    private StackPane signupOption;

    @FXML
    private StackPane exitOption;

    @FXML
    private StackPane logoContainer;
    
    @FXML
    private Canvas backgroundCanvas;
    
    @FXML
    private StackPane rootStackPane;
    
    @FXML
    private ImageView mediaVaultLogo;

    @FXML
    private ImageView mediaVaultTitle;
    
    private Connection conn;
    
    private UserDAO userDAO;
    private final PauseTransition resizeDelay = new PauseTransition(Duration.millis(220));
    
    /**
     * Initializes login screen, background, animations, and resizing.
     */
    @FXML
    public void initialize() {
    	mediaVaultLogo.setPreserveRatio(true);
    	mediaVaultTitle.setPreserveRatio(true);

    	// Binds background canvas to window size
    	backgroundCanvas.widthProperty().bind(rootStackPane.widthProperty());
    	backgroundCanvas.heightProperty().bind(rootStackPane.heightProperty());
    	backgroundCanvas.setCache(true);

    	resizeDelay.setOnFinished(event -> drawBackground());

    	// Updates layout when window size changes
    	rootStackPane.widthProperty().addListener((obs, oldVal, newVal) -> handleResize());
    	rootStackPane.heightProperty().addListener((obs, oldVal, newVal) -> handleResize());

    	loginBox.setMaxWidth(Double.MAX_VALUE);
    	signupOption.setMaxWidth(Double.MAX_VALUE);
    	exitOption.setMaxWidth(Double.MAX_VALUE);

    	// Initializes hover animations
    	addHoverAnimation(loginBox, loginHighlight);
    	addHoverAnimation(signupOption, signupHighlight);
    	addHoverAnimation(exitOption, exitHighlight);

    	Platform.runLater(this::handleResize);
    }
    
    /**
     * Sets database connection for login operations.
     *
     * @param conn active database connection
     */
    public void setConnection(Connection conn) {
    	this.conn = conn;
    	this.userDAO = new UserDAO(conn);
    }

    /**
     * Enlarges hovered menu option.
     *
     * @param event mouse enter event
     */
    @FXML
    private void handleOptionEntered(MouseEvent event) {
    	StackPane option = (StackPane)event.getSource();

    	ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), option);

    	scaleUp.setToX(1.02);
    	scaleUp.setToY(1.02);
    	scaleUp.play();
    }

    /**
     * Restores menu option size after hover.
     *
     * @param event mouse exit event
     */
    @FXML
    private void handleOptionExited(MouseEvent event) {
    	StackPane option = (StackPane)event.getSource();

    	ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), option);

    	scaleDown.setToX(1.0);
    	scaleDown.setToY(1.0);
    	scaleDown.play();
    }

    /**
     * Opens login popup.
     *
     * @param event mouse click event
     */
    @FXML
    private void handleLoginClick(MouseEvent event) {
    	showLoginPopup();
    }

    /**
     * Opens signup popup.
     *
     * @param event mouse click event
     */
    @FXML
    private void handleSignupClick(MouseEvent event) {
    	showSignupPopup();
    }

    /**
     * Closes application.
     *
     * @param event mouse click event
     */
    @FXML
    private void handleExitClick(MouseEvent event) {
    	Stage stage = (Stage)rootBorderPane.getScene().getWindow();
    	stage.close();
    }

    /**
     * Switches to another scene.
     *
     * @param fxmlPath target FXML file
     */
    private void switchScene(String fxmlPath) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    		Parent root = loader.load();

    		// Passes database connection to menu
    		if(loader.getController() instanceof MenuController)
    		{
    			MenuController controller = loader.getController();
    			controller.setConnection(conn);
    		}

    		Stage stage = (Stage)rootBorderPane.getScene().getWindow();
    		stage.getScene().setRoot(root);
    	}
    	catch(IOException e) {
    		System.err.println("Unable to load FXML file: " + fxmlPath);
    		e.printStackTrace();
    	}
    }
    
    /**
     * Draws animated background dot pattern.
     */
    private void drawBackground() {
    	GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

    	double width = backgroundCanvas.getWidth();
    	double height = backgroundCanvas.getHeight();

    	gc.clearRect(0, 0, width, height);

    	if(width <= 0 || height <= 0)
    		return;

    	double spacing = 55;
    	double totalSize = width + height;

    	gc.setFill(Color.rgb(100, 105, 170, 0.15));

    	// Draws background dots
    	for(double y=spacing/2; y<height; y+=spacing)
    	{
    		for(double x=spacing/2; x<width; x+=spacing)
    		{
    			double positionRatio = (x + y)/totalSize;
    			double radius = 1.5 + positionRatio*4.5;

    			gc.fillOval(x - radius, y - radius, radius*2, radius*2);
    		}
    	}
    }

    /**
     * Creates hover animation for a menu option.
     *
     * @param box target menu option
     * @param highlight matching highlight bar
     */
    private void addHoverAnimation(StackPane box, Rectangle highlight) {
    	highlight.setWidth(0);
    	highlight.setManaged(false);
    	highlight.setMouseTransparent(true);
    	highlight.heightProperty().bind(box.heightProperty());

    	StackPane.setAlignment(highlight, Pos.CENTER_LEFT);

    	box.setOnMouseEntered(event -> animateHighlight(highlight, highlight.getWidth(), box.getWidth()));
    	box.setOnMouseExited(event -> animateHighlight(highlight, highlight.getWidth(), 0));
    }

    /**
     * Animates menu option highlight.
     *
     * @param highlight target highlight bar
     * @param startWidth starting width
     * @param endWidth ending width
     */
    private void animateHighlight(Rectangle highlight, double startWidth, double endWidth) {
    	Timeline animation = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(highlight.widthProperty(), startWidth)),
    									  new KeyFrame(Duration.millis(125), new KeyValue(highlight.widthProperty(), endWidth)));

    	animation.play();
    }
    
    /**
     * Closes active popup.
     *
     * @param overlay popup overlay
     */
    private void closePopup(StackPane overlay) {
    	rootStackPane.getChildren().remove(overlay);

    	// Removes blur effect
    	rootBorderPane.setEffect(null);
    }
    
    /**
     * Updates component sizes based on window size.
     */
    private void updateLayout() {
    	double width = rootStackPane.getWidth();
    	double height = rootStackPane.getHeight();

    	// Scales logo and title
    	mediaVaultLogo.setFitWidth(width*0.32);
    	mediaVaultTitle.setFitWidth(width*0.45);

    	logoContainer.setPrefWidth(width*0.42);

    	// Scales menu options
    	loginBox.setPrefHeight(height*0.12);
    	signupOption.setPrefHeight(height*0.12);
    	exitOption.setPrefHeight(height*0.12);
    }

    /**
     * Opens login popup.
     */
    private void showLoginPopup() {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/LoginForm.fxml"));
    		Parent popup = loader.load();

    		// Applies background blur
    		rootBorderPane.setEffect(new GaussianBlur(12));

    		StackPane overlay = new StackPane();
    		overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
    		overlay.setPickOnBounds(true);
    		overlay.getChildren().add(popup);

    		LoginFormController controller = loader.getController();
    		controller.setConnection(conn);
    		controller.setCloseAction(() -> closePopup(overlay));
    		controller.setLoginSuccessAction(this::openMenu);

    		popup.setOnMouseClicked(event -> event.consume());
    		overlay.setOnMouseClicked(event -> closePopup(overlay));

    		rootStackPane.getChildren().add(overlay);
    		StackPane.setAlignment(popup, Pos.CENTER);
    	}
    	catch(IOException e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Opens signup popup.
     */
    private void showSignupPopup() {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SigninForm.fxml"));
    		Parent popup = loader.load();

    		// Applies background blur
    		rootBorderPane.setEffect(new GaussianBlur(12));

    		StackPane overlay = new StackPane();
    		overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
    		overlay.setPickOnBounds(true);
    		overlay.getChildren().add(popup);
    		
    		// Opens controller for signing-up
    		SignupFormController controller = loader.getController();
    		controller.setConnection(conn);
    		controller.setCloseAction(() -> closePopup(overlay));
    		
    		// When signed-up, it automatically goes to Menu
    		controller.setSignupSuccessAction(this::openMenu);

    		popup.setOnMouseClicked(event -> event.consume());
    		overlay.setOnMouseClicked(event -> closePopup(overlay));

    		rootStackPane.getChildren().add(overlay);
    		StackPane.setAlignment(popup, Pos.CENTER);
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Opens main menu after successful login or signup.
     */
    private void openMenu() {
    	switchScene("/resources/application/fxml/Menu.fxml");
    }

    /**
     * Updates layout after window resize.
     */
    private void handleResize() {
    	updateLayout();
    	resizeDelay.playFromStart();
    }
}