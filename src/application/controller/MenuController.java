package application.controller;

import java.io.IOException;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class MenuController {
    
    @FXML 
    private ImageView songsTile;
    
    @FXML 
    private ImageView gamesTile;
    
    @FXML 
    private ImageView showsTile;
    
    @FXML 
    private ImageView mediaVaultLogo;
    
    @FXML
    private ImageView mediaVaultTitle;
    
    @FXML 
    private ImageView profileAvatar;
    
    @FXML
    private ImageView settingsIcon;

    @FXML
    public void initialize() {
        // Load images and icons
	    	songsTile.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/backgrounds/songs-tile.png")));
	    	gamesTile.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/backgrounds/games-tile.png")));
	    	showsTile.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/backgrounds/shows-tile.png")));
        
	    	mediaVaultLogo.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png")));
	    	mediaVaultTitle.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/logos/title.png")));
	    	settingsIcon.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/icons/settings-gear-svgrepo-com.png")));
	    	profileAvatar.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/default/default-profile.png")));        
	    
        // Ensure that when a tile expands, it renders on top of the other tiles
        songsTile.setViewOrder(0);
        gamesTile.setViewOrder(0);
        showsTile.setViewOrder(0);
    }

    @FXML
    private void handleMouseEntered(MouseEvent event) {
    		ImageView hoveredTile = (ImageView) event.getSource();
        
        // Bring the hovered tile to the front layer
        hoveredTile.setViewOrder(-1.0); 

        // Create the pop-out zoom animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), hoveredTile);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.play();
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
    		ImageView exitedTile = (ImageView) event.getSource();
        
        // Reset the layer order
        exitedTile.setViewOrder(0);

        // Scale back down to standard size
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), exitedTile);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();
    }
    
    @FXML
    private void handleTileClick(MouseEvent event) {
    		Object source = event.getSource();
        String title = null;
        String styleClass = null;

        // Identify which tile was clicked
        if (source == songsTile) {
            title = "Songs";
            styleClass = "theme-songs";
        } else if (source == gamesTile) {
	        	title = "Games";
	        	styleClass = "theme-games";
        } else if (source == showsTile) {
        		title = "Shows";
            styleClass = "theme-shows";
        }
        
        try {
        		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/MediaPlaylistsScene.fxml"));
        		Parent root = loader.load();
        		
            MediaPlaylistsItemsController controller = loader.getController();
            controller.setupView(title, styleClass);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}