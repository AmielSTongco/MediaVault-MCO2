package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
//import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
//import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.sql.Connection;
import application.db.DatabaseConnection;
import application.db.DatabaseInitializer;
import application.controller.LoginController;
import application.dao.UserDAO;


public class Main extends Application {
 
	@Override
	public void start(Stage stage) {
	    try {
	    	
	    	Connection conn = DatabaseConnection.connect();
	    	DatabaseInitializer.initialize(conn);
	    	
	    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/Login.fxml"));
	        Parent root = loader.load();
	        
	        Font.loadFont(getClass().getResourceAsStream("/resources/application/fonts/PublicSans-VariableFont_wght.ttf"), 16);
	        Font.loadFont(getClass().getResourceAsStream("/resources/application/fonts/OpenSauceSans-Regular.ttf"), 16);
	        Font.loadFont(getClass().getResourceAsStream("/resources/application/fonts/OpenSauceSans-Bold.ttf"), 16);
	        
	        LoginController controller = loader.getController();
	        controller.setConnection(conn);
	        
	        Scene scene = new Scene(root);
	        
	        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/application/images/logos/logo.png")));     
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