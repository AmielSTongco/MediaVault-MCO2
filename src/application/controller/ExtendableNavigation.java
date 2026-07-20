package application.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ExtendableNavigation extends Application {

	
 
	public static void main(String[] args) {
		launch(args);
	}
 
	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getResource("extendableNavigation.fxml"));
 
		Scene scene = new Scene(root, 600, 200);
		scene.getStylesheets().add("my.css");
 
		stage.setTitle("Extendable navigation pane demo");
		stage.setScene(scene);
		stage.show();
	}
 
	@FXML
	void initialize() {
		
	}
 
	
 
	
}
