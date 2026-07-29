package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
//import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
//import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
 
	@Override
	public void start(Stage stage) {
	    try {
	        Parent root = FXMLLoader.load(getClass().getResource("/resources/application/fxml/Menu.fxml"));

	        Font.loadFont(getClass().getResourceAsStream("/resources/application/fonts/PublicSans-VariableFont_wght.ttf"), 16);
	        Font.loadFont(getClass().getResourceAsStream("/resources/application/fonts/OpenSauceSans-Regular.ttf"), 16);
	        Font.loadFont(getClass().getResourceAsStream("/resources/application/fonts/OpenSauceSans-Bold.ttf"), 16);

	        Scene scene = new Scene(root);

	        stage.setScene(scene);
	        stage.setTitle("MediaVault");
	        stage.setFullScreen(true);
	        stage.setMaximized(true);
	        stage.show();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}

	public static void main(String[] args) {
		launch(args);
	}
}