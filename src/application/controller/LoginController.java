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
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

public class LoginController {

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private HBox mainContainer;

    @FXML
    private StackPane loginOption;

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
    
    @FXML
    public void initialize() {
    	
        mediaVaultLogo.setFitWidth(500);
        mediaVaultLogo.setPreserveRatio(true);

        mediaVaultTitle.setFitWidth(1200);
        mediaVaultTitle.setPreserveRatio(true);
    	
        backgroundCanvas.widthProperty().bind(rootStackPane.widthProperty());
        backgroundCanvas.heightProperty().bind(rootStackPane.heightProperty());

        backgroundCanvas.widthProperty().addListener(
            (obs, oldValue, newValue) -> drawBackground()
        );

        backgroundCanvas.heightProperty().addListener(
            (obs, oldValue, newValue) -> drawBackground()
        );
    	
        loginOption.setMaxWidth(Double.MAX_VALUE);
        signupOption.setMaxWidth(Double.MAX_VALUE);
        exitOption.setMaxWidth(Double.MAX_VALUE);
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
        showPopup("/resources/application/fxml/LoginForm.fxml");
    }

    @FXML
    private void handleSignupClick(MouseEvent event) {
        showPopup("/resources/application/fxml/Signup.fxml");
    }

    @FXML
    private void handleExitClick(MouseEvent event) {
        Stage stage = (Stage) rootBorderPane.getScene().getWindow();
        stage.close();
    }

    private void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(fxmlPath)
            );

            Parent root = loader.load();

            Stage stage = (Stage) rootBorderPane.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println(
                "Unable to load FXML file: " + fxmlPath
            );

            e.printStackTrace();
        }
    }
    
    private void drawBackground() {
        GraphicsContext gc = backgroundCanvas.getGraphicsContext2D();

        double width = backgroundCanvas.getWidth();
        double height = backgroundCanvas.getHeight();

        gc.clearRect(0, 0, width, height);

        double spacing = 42;

        gc.setFill(Color.rgb(100, 105, 170, 0.15));

        for (double y = spacing / 2; y < height; y += spacing) {
            for (double x = spacing / 2; x < width; x += spacing) {
            	
                double positionRatio = (x + y) / (width + height);
                double radius = 1.5 + positionRatio * 4.5;

                gc.fillOval(
                    x - radius,
                    y - radius,
                    radius * 2,
                    radius * 2
                );
            }
        }
    }
    
    private void showPopup(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent popup = loader.load();

            rootBorderPane.setEffect(new GaussianBlur(12));
            backgroundCanvas.setEffect(new GaussianBlur(12));

            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
            overlay.setPickOnBounds(true);

            overlay.getChildren().add(popup);

            popup.setOnMouseClicked(event -> event.consume());

            overlay.setOnMouseClicked(event -> closePopup(overlay));

            rootStackPane.getChildren().add(overlay);

            StackPane.setAlignment(popup, Pos.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void closePopup(StackPane overlay) {
        rootStackPane.getChildren().remove(overlay);

        rootBorderPane.setEffect(null);
        backgroundCanvas.setEffect(null);
    }
}