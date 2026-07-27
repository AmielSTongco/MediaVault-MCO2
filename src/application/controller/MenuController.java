package application.controller;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
//import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class MenuController {

    @FXML private VBox songsTile;
    @FXML private VBox gamesTile;
    @FXML private VBox showsTile;
    
    @FXML private ImageView songsImage;
    @FXML private ImageView gamesImage;
    @FXML private ImageView showsImage;

    @FXML
    public void initialize() {
        // Direct image loading assuming they are in your resources folder:
        // songsImage.setImage(new Image(getClass().getResourceAsStream("/images/songs.png")));
        // gamesImage.setImage(new Image(getClass().getResourceAsStream("/images/games.png")));
        // showsImage.setImage(new Image(getClass().getResourceAsStream("/images/shows.png")));
        
        // Ensure that when a tile expands, it renders on top of the other tiles
        songsTile.setViewOrder(0);
        gamesTile.setViewOrder(0);
        showsTile.setViewOrder(0);
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        VBox hoveredTile = (VBox) event.getSource();
        
        // Bring the hovered tile to the front layer so it doesn't clip behind neighbors
        hoveredTile.setViewOrder(-1.0); 

        // Create the pop-out zoom animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), hoveredTile);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.play();
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        VBox exitedTile = (VBox) event.getSource();
        
        // Reset the layer order
        exitedTile.setViewOrder(0);

        // Scale back down to standard size
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), exitedTile);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();
    }
}