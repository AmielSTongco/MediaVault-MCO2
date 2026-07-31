package application.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import application.dao.UserDAO;
import application.dao.impl.MediaPlaylistDAOImpl;
import application.model.Type;
import application.model.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class AddPlaylistController {

	@FXML
    private TextField playlistNameField;

    @FXML
    private ImageView playlistPicture;

    @FXML
    private Label statusLabel;
	
	private MediaPlaylistDAOImpl mediaPlaylistDAO;
	private Runnable closeAction;
	private Runnable deleteSuccessAction;
	private String playlistPicturePath;
	
	private static Type mediaType;

	@FXML
	public void initialize() {
		statusLabel.setVisible(false);
		statusLabel.setManaged(false);
	}

	public void setConnection(Connection conn) {
		mediaPlaylistDAO = new MediaPlaylistDAOImpl(conn, UserSession.getCurrentUserId());
	}

	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	@FXML
	private void handleCreate() {
		if(mediaPlaylistDAO == null) {
			showStatus("Database connection is unavailable.", true);
		}
		else {
			String playlistName = playlistNameField.getText().trim();
			boolean valid = true;

			if(playlistName.isEmpty()) {
				showStatus("Playlist name cannot be empty.", true);
				valid = false;
			}

			try {
				if(valid) {
					boolean created = mediaPlaylistDAO.createPlaylist(playlistName, playlistPicturePath, mediaType);
					
					if (created) {
			            showStatus("Playlist created successfully.", false);
			            playlistNameField.clear();
			        } else {
			            showStatus("Failed to create playlist. The name may already exist or be reserved.", true);
			        }
				}
			}
			catch(SQLException e) {
				showStatus("An unexpected database error occurred.", true);
		        e.printStackTrace();
			}
		}
	}
	
	@FXML
	private void handlePlaylistPicture() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Profile Picture");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

		File selectedFile = chooser.showOpenDialog(playlistPicture.getScene().getWindow());

		if(selectedFile != null) {
			 playlistPicturePath = selectedFile.getAbsolutePath();
			
			Image image = new Image(selectedFile.toURI().toString());
			playlistPicture.setImage(image);
			
			Rectangle rect = new Rectangle(220, 220);
			rect.setArcHeight(90);
	        rect.setArcWidth(90);
	        rect.setEffect(new Reflection());
	        playlistPicture.setClip(rect);

			showStatus("Profile picture selected.", false);
		}
	}

	@FXML
	private void handleClose() {
		if(closeAction != null)
			closeAction.run();
	}

	private void showStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setTextFill(error ? javafx.scene.paint.Color.web("#FF8F9B") : javafx.scene.paint.Color.web("#9BE7B0"));
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
	}
}