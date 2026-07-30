package application.controller;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MediaController {

	// For Expandable Navigation Bar
	private static final int deltaXEditButton   = 40;
	private static final int deltaXRemoveButton = 20;
	private static final int deltaXDeleteButton = 0;
	private static final int deltaXBackButton   = -20;
	private static final int deltaXHomeButton   = -40;
	
	private Rectangle clipRect;
	 
	private DropShadow dropShadowForSelectedPane;
	
	// For editing details
	private boolean isEditing = false;
	
    @FXML
    private ImageView mediaArt;

    @FXML
    private Label creatorLabel;
    
    @FXML
    private TextField creatorField;

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
    private Button editButton;

    @FXML
    private Button removeButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Button backButton;

    @FXML
    private Button homeButton;

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

		setIcon(editButton, "/resources/application/images/icons/pencil-svgrepo-com.png");
		setIcon(removeButton, "/resources/application/images/icons/minus-svgrepo-com.png");
		setIcon(deleteButton, "/resources/application/images/icons/trash-can-svgrepo-com.png");
		setIcon(backButton, "/resources/application/images/icons/back-reply-svgrepo-com.png");
		setIcon(homeButton, "/resources/application/images/icons/home-icon-svgrepo-com.png");
		hidePane();
    	
        // Create a rounded rectangle for media image
        Rectangle clip = new Rectangle(650, 650); // match prefHeight/prefWidth
        clip.setArcWidth(65);
        clip.setArcHeight(65);
        
        mediaArt.setClip(clip);
        
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
        
        creatorField.textProperty().addListener((obs, oldText, newText) ->
        creatorLabel.setText("Artist: " + newText)
		);
    }
    
    @FXML
	private void showPane() { 
	    	editButton.setText("Update Details");
	    	removeButton.setText("Remove From Playlist");
	    	deleteButton.setText("Delete Media");
	    	backButton.setText("Back");
	    	homeButton.setText("Home");
    	
		// Animation for showing the pane completely
		Timeline timelineDown = new Timeline();
 
		final KeyValue kvDwn1 = new KeyValue(clipRect.heightProperty(), extendableNavigationPane.getHeight());
		final KeyValue kvDwn2 = new KeyValue(clipRect.translateYProperty(), 0);
		final KeyValue kvDwn3 = new KeyValue(extendableNavigationPane.translateYProperty(), 0);
		final KeyFrame kfDwn = new KeyFrame(Duration.millis(100), createBouncingEffect(extendableNavigationPane.getHeight()), kvDwn1, kvDwn2, kvDwn3);
 
		// Animation for moving Edit button
		final KeyValue kvEdit = new KeyValue(editButton.translateXProperty(), -deltaXEditButton);
		final KeyFrame kfEdit = new KeyFrame(Duration.millis(200), kvEdit);

		// Animation for moving Remove button
		final KeyValue kvRemove = new KeyValue(removeButton.translateXProperty(), -deltaXRemoveButton);
		final KeyFrame kfRemove = new KeyFrame(Duration.millis(200), kvRemove);

		// Animation for moving Delete button
		final KeyValue kvDelete = new KeyValue(deleteButton.translateXProperty(), -deltaXDeleteButton);
		final KeyFrame kfDelete = new KeyFrame(Duration.millis(200), kvDelete);

		// Animation for moving Back button
		final KeyValue kvBack = new KeyValue(backButton.translateXProperty(), -deltaXBackButton);
		final KeyFrame kfBack = new KeyFrame(Duration.millis(200), kvBack);

		// Animation for moving Home button
		final KeyValue kvHome = new KeyValue(homeButton.translateXProperty(), -deltaXHomeButton);
		final KeyFrame kfHome = new KeyFrame(Duration.millis(200), kvHome);

		timelineDown.getKeyFrames().addAll(kfDwn, kfEdit, kfRemove, kfDelete, kfBack, kfHome);
		timelineDown.play();
	}
 
	@FXML
	private void hidePane() {
		editButton.setText(null);
		removeButton.setText(null);
		deleteButton.setText(null);
		backButton.setText(null);
		homeButton.setText(null);
		
		// Animation for hiding the pane..
		Timeline timelineUp = new Timeline();
 
		final KeyValue kvUp1 = new KeyValue(clipRect.heightProperty(), 55);
		final KeyValue kvUp2 = new KeyValue(extendableNavigationPane.translateYProperty(), 10);
		final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2);
 
		// Animation for moving Edit button
		final KeyValue kvEdit = new KeyValue(editButton.translateXProperty(), deltaXEditButton);
		final KeyFrame kfEdit = new KeyFrame(Duration.millis(200), kvEdit);

		// Animation for moving Remove button
		final KeyValue kvRemove = new KeyValue(removeButton.translateXProperty(), deltaXRemoveButton);
		final KeyFrame kfRemove = new KeyFrame(Duration.millis(200), kvRemove);

		// Animation for moving Delete button
		final KeyValue kvDelete = new KeyValue(deleteButton.translateXProperty(), deltaXDeleteButton);
		final KeyFrame kfDelete = new KeyFrame(Duration.millis(200), kvDelete);
		
		// Animation for moving Back button
		final KeyValue kvBack = new KeyValue(backButton.translateXProperty(), deltaXBackButton);
		final KeyFrame kfBack = new KeyFrame(Duration.millis(200), kvBack);

		// Animation for moving Home button
		final KeyValue kvHome = new KeyValue(homeButton.translateXProperty(), deltaXHomeButton);
		final KeyFrame kfHome = new KeyFrame(Duration.millis(200), kvHome);

		timelineUp.getKeyFrames().addAll(kfUp, kfEdit, kfRemove, kfDelete, kfBack, kfHome);
		timelineUp.play();
	}
 
	@FXML
	private void toggleEdit() {
		deselectAllPanes();
		isEditing = !isEditing;
		
		if (!isEditing) {
			setIcon(editButton, "/resources/application/images/icons/pencil-svgrepo-com.png");
			
			creatorField.positionCaret(creatorField.getText().length());
			
			creatorLabel.setVisible(true);
		    creatorLabel.setManaged(true);

		    creatorField.setVisible(false);
		    creatorField.setManaged(false);
		}
		else {
			setIcon(editButton, "/resources/application/images/icons/minus-svgrepo-com.png");
		}
	}
 
	@FXML
	private void removeMedia() {
		System.out.println("Selecting pane 2");
		deselectAllPanes();
		removeButton.setEffect(dropShadowForSelectedPane);
	}
	
	@FXML
	private void deleteMedia() {
		System.out.println("Selecting pane 3");
		deselectAllPanes();
		deleteButton.setEffect(dropShadowForSelectedPane);
	}
	
	@FXML
	private void goToBack() {
		System.out.println("Selecting pane 4");
		deselectAllPanes();
		backButton.setEffect(dropShadowForSelectedPane);
	}
	
	@FXML
	private void goToHome(ActionEvent event) {
		deselectAllPanes();
		
		try {
	    		FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/Menu.fxml"));
	    		Parent root = loader.load();
	        
	        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	        stage.getScene().setRoot(root);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	@FXML
	private void editCreator() {
		if (isEditing) {
			creatorLabel.setVisible(false);
		    creatorLabel.setManaged(false);

		    creatorField.setVisible(true);
		    creatorField.setManaged(true);
		}
	}
 
	private void deselectAllPanes() {
		editButton.setEffect(null);
		removeButton.setEffect(null);
		deleteButton.setEffect(null);
		backButton.setEffect(null);
		homeButton.setEffect(null);
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
