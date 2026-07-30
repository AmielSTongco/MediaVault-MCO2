package application.controller;

import java.io.IOException;

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
    private final PauseTransition resizeDelay = new PauseTransition(Duration.millis(220));
    
    @FXML
    public void initialize() {
        mediaVaultLogo.setPreserveRatio(true);
        mediaVaultTitle.setPreserveRatio(true);

        backgroundCanvas.widthProperty().bind(rootStackPane.widthProperty());
        backgroundCanvas.heightProperty().bind(rootStackPane.heightProperty());
        backgroundCanvas.setCache(true);

        resizeDelay.setOnFinished(event -> drawBackground());

        rootStackPane.widthProperty().addListener((obs, oldVal, newVal) -> handleResize());
        rootStackPane.heightProperty().addListener((obs, oldVal, newVal) -> handleResize());

        loginBox.setMaxWidth(Double.MAX_VALUE);
        signupOption.setMaxWidth(Double.MAX_VALUE);
        exitOption.setMaxWidth(Double.MAX_VALUE);

        addHoverAnimation(loginBox, loginHighlight);
        addHoverAnimation(signupOption, signupHighlight);
        addHoverAnimation(exitOption, exitHighlight);

        Platform.runLater(this::handleResize);
    }
    
    
    public void setConnection(Connection conn) {
    	this.conn = conn;
    }

    @FXML
    private void handleOptionEntered(MouseEvent event) {
        StackPane option = (StackPane) event.getSource();

        ScaleTransition scaleUp = new ScaleTransition(
            Duration.millis(150),
            option
        );

        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);
        scaleUp.play();
    }

    @FXML
    private void handleOptionExited(MouseEvent event) {
        StackPane option = (StackPane) event.getSource();

        ScaleTransition scaleDown = new ScaleTransition(
            Duration.millis(150),
            option
        );

        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();
    }

    @FXML
    private void handleLoginClick(MouseEvent event) {
        showLoginPopup();
    }

    @FXML
    private void handleSignupClick(MouseEvent event) {
        showSignupPopup();
    }

    @FXML
    private void handleExitClick(MouseEvent event) {
        Stage stage = (Stage) rootBorderPane.getScene().getWindow();
        stage.close();
    }

    private void switchScene(String fxmlPath) {
    	try {
    		FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
    		Parent root = loader.load();

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
    
    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();

        gc.clearRect(0, 0, width, height);

        if (width <= 0 || height <= 0) {
            return;
        }

        double spacing = 55;
        double totalSize = width + height;

        gc.setFill(Color.rgb(100, 105, 170, 0.15));

        for (double y = spacing / 2; y < height; y += spacing) {
            for (double x = spacing / 2; x < width; x += spacing) {
                double positionRatio = (x + y) / totalSize;
                double radius = 1.5 + positionRatio * 4.5;

                gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
            }
        }
    }
    
    private void addHoverAnimation(StackPane box, Rectangle highlight) {
        highlight.setWidth(0);
        highlight.setManaged(false);
        highlight.setMouseTransparent(true);
        highlight.heightProperty().bind(box.heightProperty());

        StackPane.setAlignment(highlight, Pos.CENTER_LEFT);

        box.setOnMouseEntered(event -> animateHighlight(highlight, highlight.getWidth(), box.getWidth()));
        box.setOnMouseExited(event -> animateHighlight(highlight, highlight.getWidth(), 0));
    }

    private void animateHighlight(
            Rectangle highlight,
            double startWidth,
            double endWidth) {

        Timeline animation = new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(
                    highlight.widthProperty(),
                    startWidth
                )
            ),
            new KeyFrame(
                Duration.millis(125),
                new KeyValue(
                    highlight.widthProperty(),
                    endWidth
                )
            )
        );

        animation.play();
    }
    
    private void closePopup(StackPane overlay) {
        rootStackPane.getChildren().remove(overlay);

        rootBorderPane.setEffect(null);
        //backgroundCanvas.setEffect(null);
    }
    
    private void updateLayout() {
        double width = rootStackPane.getWidth();
        double height = rootStackPane.getHeight();

        mediaVaultLogo.setFitWidth(width * 0.32);
        mediaVaultTitle.setFitWidth(width * 0.45);

        logoContainer.setPrefWidth(width * 0.42);

        loginBox.setPrefHeight(height * 0.12);
        signupOption.setPrefHeight(height * 0.12);
        exitOption.setPrefHeight(height * 0.12);
    }
    
    private void showLoginPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/LoginForm.fxml"));
            Parent popup = loader.load();

            rootBorderPane.setEffect(new GaussianBlur(12));
            //backgroundCanvas.setEffect(new GaussianBlur(12));

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
            overlay.setPickOnBounds(true);
            overlay.getChildren().add(popup);

            LoginFormController controller = loader.getController();
            
            controller.setConnection(conn);
            controller.setCloseAction(() -> closePopup(overlay));
            controller.setLoginSuccessAction(this::openMenu);
            
            controller.setCloseAction(() -> closePopup(overlay));
            controller.setLoginSuccessAction(this::openMenu);

            popup.setOnMouseClicked(event -> event.consume());
            overlay.setOnMouseClicked(event -> closePopup(overlay));

            rootStackPane.getChildren().add(overlay);
            StackPane.setAlignment(popup, Pos.CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showSignupPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/SigninForm.fxml"));
            Parent popup = loader.load();

            rootBorderPane.setEffect(new GaussianBlur(12));
            backgroundCanvas.setEffect(new GaussianBlur(12));

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
            overlay.setPickOnBounds(true);
            overlay.getChildren().add(popup);

            SignupFormController controller = loader.getController();
            controller.setConnection(conn);
            controller.setCloseAction(() -> closePopup(overlay));
            controller.setSignupSuccessAction(this::openMenu);

            popup.setOnMouseClicked(event -> event.consume());
            overlay.setOnMouseClicked(event -> closePopup(overlay));

            rootStackPane.getChildren().add(overlay);
            StackPane.setAlignment(popup, Pos.CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void openMenu() {
        switchScene("/resources/application/fxml/Menu.fxml");
    }
    
    private void handleResize() {
        updateLayout();
        resizeDelay.playFromStart();
    }
}