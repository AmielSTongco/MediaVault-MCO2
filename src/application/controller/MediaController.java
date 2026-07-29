package application.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class MediaController {

	// For Expandable Navigation Bar
	private static final int deltaXNavButton1 = 10;
	private static final int deltaXNavButton2 = -10;
	
	private Rectangle clipRect;
	 
	private DropShadow dropShadowForSelectedPane;
	
    @FXML
    private ImageView mediaArt;

    @FXML
    private Label creatorLabel;

    @FXML
    private HBox extendableNavigationPane;

    @FXML
    private Label genreLabel;
    
    @FXML
    private Label mediaLabel;

    @FXML
    private ImageView mediaVaultLogo;

    @FXML
    private ImageView mediaVaultTitle;
    
    @FXML
    private ImageView profileAvatar;

    @FXML
    private Button navButton1;

    @FXML
    private Button navButton2;
    
    @FXML
    private Button navButton3;

    	@FXML 
    	private Label avgPlaytimeLabel;
    
    @FXML
    private Label playtimeLabel;

    @FXML
    private Label ratingLabel;

    @FXML
    private Label reviewLabel;
    
    @FXML
    private Label yearFirstAiredLabel;
    
    @FXML
    private Label yearLastAiredLabel;
    
    @FXML
    private Label numOfEpisodesLabel;
    
    @FXML
    private Label numOfSeasonsLabel;
    
    @FXML
    private Label airingLabel;

    @FXML
    private ImageView settingsIcon;

    @FXML
    private Label statusLabel;

    @FXML
    private Label title;

    @FXML
    private Label yearLabel;
    
	private String mediaType;

    @FXML
    public void initialize() {
		mediaType = mediaLabel.getText();		
		
		Image logoImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png"));
        Image titleImg = new Image(getClass().getResourceAsStream("/resources/application/images/logos/title.png"));
        Image settingsImg = new Image(getClass().getResourceAsStream("/resources/application/images/icons/settings-gear-svgrepo-com.png"));
        Image profileImg = new Image(getClass().getResourceAsStream("/resources/application/images/default/default-profile.png"));

        // Assign images to ImageView nodes
        mediaVaultLogo.setImage(logoImg);
        mediaVaultTitle.setImage(titleImg);
        settingsIcon.setImage(settingsImg);
        profileAvatar.setImage(profileImg);
        
        clipRect = new Rectangle();
		clipRect.setWidth(extendableNavigationPane.getPrefWidth());
		setIcon(navButton1, "/resources/application/images/icons/plus-svgrepo-com.png");
		setIcon(navButton2, "/resources/application/images/icons/back-reply-svgrepo-com.png");
		hidePane();
    	
        // Create a rounded rectangle for media image
        Rectangle clip = new Rectangle(650, 650); // match prefHeight/prefWidth
        clip.setArcWidth(65);
        clip.setArcHeight(65);
        
        mediaArt.setClip(clip);
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        //shadow.setOffsetX(5);
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
        creatorLabel.setEffect(shadow);
        genreLabel.setEffect(shadow);
        statusLabel.setEffect(shadow);
        ratingLabel.setEffect(shadow);
        reviewLabel.setEffect(shadow);
        
        switch(mediaType) {
        		case "SONGS":
	        		playtimeLabel.setEffect(shadow);
	        		yearLabel.setEffect(shadow);
	        		break;
        		case "GAMES":
        			avgPlaytimeLabel.setEffect(shadow);
        			yearLabel.setEffect(shadow);
        			break;
        		case "SHOWS":
        			airingLabel.setEffect(shadow);
        			yearFirstAiredLabel.setEffect(shadow);
        			yearLastAiredLabel.setEffect(shadow);
        			numOfEpisodesLabel.setEffect(shadow);
        			numOfSeasonsLabel.setEffect(shadow);
        			break;
        }	
    }
    
    @FXML
	private void showPane() { 
		// Animation for showing the pane completely
		Timeline timelineDown = new Timeline();
 
		final KeyValue kvDwn1 = new KeyValue(clipRect.heightProperty(), extendableNavigationPane.getHeight());
		final KeyValue kvDwn2 = new KeyValue(clipRect.translateYProperty(), 0);
		final KeyValue kvDwn3 = new KeyValue(extendableNavigationPane.translateYProperty(), 0);
		final KeyFrame kfDwn = new KeyFrame(Duration.millis(100), createBouncingEffect(extendableNavigationPane.getHeight()), kvDwn1, kvDwn2, kvDwn3);
 
		// Animation for moving button 1
		final KeyValue kvB1 = new KeyValue(navButton1.translateXProperty(), -deltaXNavButton1);
		final KeyFrame kfB1 = new KeyFrame(Duration.millis(200), kvB1);
 
		// Animation for moving button 2
		final KeyValue kvB2 = new KeyValue(navButton2.translateXProperty(), -deltaXNavButton2);
		final KeyFrame kfB2 = new KeyFrame(Duration.millis(200), kvB2);
 
		navButton1.setText("Add Playlist");
		navButton2.setText("Back");
		timelineDown.getKeyFrames().addAll(kfDwn, kfB1, kfB2);
		timelineDown.play();
	}
 
	@FXML
	private void hidePane() { 
		// Animation for hiding the pane..
		Timeline timelineUp = new Timeline();
 
		final KeyValue kvUp1 = new KeyValue(clipRect.heightProperty(), 55);
		final KeyValue kvUp2 = new KeyValue(extendableNavigationPane.translateYProperty(), 10);
		final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2);
 
		// Animation for moving button 1
		final KeyValue kvB1 = new KeyValue(navButton1.translateXProperty(), deltaXNavButton1);
		final KeyFrame kfB1 = new KeyFrame(Duration.millis(200), kvB1);
 
		final KeyValue kvB2 = new KeyValue(navButton2.translateXProperty(), deltaXNavButton2);
		final KeyFrame kfB2 = new KeyFrame(Duration.millis(200), kvB2);
 
		navButton1.setText(null);
		navButton2.setText(null);
		timelineUp.getKeyFrames().addAll(kfUp, kfB1, kfB2);
		timelineUp.play();
	}
 
	@FXML
	private void selectPane1() {
		System.out.println("Selecting pane 1");
		deselectAllPanes();
		navButton1.setEffect(dropShadowForSelectedPane);
	}
 
	@FXML
	private void selectPane2() {
		System.out.println("Selecting pane 2");
		deselectAllPanes();
		navButton2.setEffect(dropShadowForSelectedPane);
	}
 
	private void deselectAllPanes() {
		navButton1.setEffect(null);
		navButton2.setEffect(null);
	}
 
	private EventHandler<ActionEvent> createBouncingEffect(double height) {
		final Timeline timelineBounce = new Timeline();
		timelineBounce.setCycleCount(2);
		timelineBounce.setAutoReverse(true);
		final KeyValue kv1 = new KeyValue(clipRect.heightProperty(), (height - 15));
		final KeyValue kv2 = new KeyValue(clipRect.translateYProperty(), 15);
		final KeyValue kv3 = new KeyValue(extendableNavigationPane.translateYProperty(), -15);
		final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
		timelineBounce.getKeyFrames().add(kf1);
 
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				timelineBounce.play();
			}
		};
		return handler;
	}
	
	private void setIcon(Button button, String name) {
		Image image = new Image(getClass().getResourceAsStream(name));
		ImageView imageView = new ImageView(image);
        
        imageView.setFitWidth(72);
        imageView.setFitHeight(72); 
        imageView.setPreserveRatio(true);
		
		button.setGraphic(imageView);
		button.setContentDisplay(ContentDisplay.TOP);
	}
}
