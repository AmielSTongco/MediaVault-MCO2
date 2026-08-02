package application.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import application.dao.UserDAO;
import application.dao.MediaPlaylistDAO;
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
	
	/*
	 * Controls the pop-up that shows when
	 * you try to add a playlist of a media
	 */
	
	@FXML
    private TextField playlistNameField;

    @FXML
    private ImageView playlistPicture;

    @FXML
    private Label statusLabel;
	
	private MediaPlaylistDAO mediaPlaylistDAO;
	private Runnable closeAction;
	private String playlistPicturePath;
	private Type mediaType;
	
	/**
	 * Initializes status message visibility.
	 */
	@FXML
	public void initialize() {
		// Hides status message
		statusLabel.setVisible(false);
		statusLabel.setManaged(false);
	}
	
	/**
	 * Sets database connection and initializes playlist data access.
	 *
	 * @param conn active database connection
	 */
	public void setConnection(Connection conn) {
		mediaPlaylistDAO = new MediaPlaylistDAO(conn, UserSession.getCurrentUserId());
	}
	
	/**
	 * Sets action executed when the popup closes.
	 *
	 * @param closeAction callback to execute
	 */
	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}
	
	/**
	 * Sets media type of the playlist being created.
	 *
	 * @param mediaType selected media type
	 */
	public void setMediaType(Type mediaType) {
		this.mediaType = mediaType;
	}
	
	/**
	 * Validates playlist information and creates a new playlist.
	 */
	@FXML
	private void handleCreate() {
		if(mediaPlaylistDAO == null)
		{
			showStatus("Database connection is unavailable.", true);
		}
		else
		{
			String playlistName = playlistNameField.getText().trim();
			boolean valid = true;
			
			// Validates playlist name
			if(playlistName.isEmpty())
			{
				showStatus("Playlist name cannot be empty.", true);
				valid = false;
			}

			try {
				if(valid)
				{
					// Assigns default playlist picture
					if(playlistPicturePath == null || playlistPicturePath.isBlank())
					{
						switch(mediaType)
						{
							case Type.SONG:
								playlistPicturePath = "/resources/application/images/icons/default-song-playlist-icon.png";
								break;
							case Type.GAME:
								playlistPicturePath = "/resources/application/images/icons/default-game-playlist-icon.png";
								break;
							case Type.SHOW:
								playlistPicturePath = "/resources/application/images/icons/default-show-playlist-icon.png";
								break;
						}
					}

					boolean created = mediaPlaylistDAO.createPlaylist(playlistName, playlistPicturePath, mediaType);
					
					// Displays creation result
					if(created)
					{
			            showStatus("Playlist created successfully.", false);
			            playlistNameField.clear();
			        }
					else
			        {
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
	
	/**
	 * Opens a file chooser and loads the selected playlist picture.
	 */
	@FXML
	private void handlePlaylistPicture() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Playlist Picture");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

		File selectedFile = chooser.showOpenDialog(playlistPicture.getScene().getWindow());

		if(selectedFile != null)
		{
			 playlistPicturePath = selectedFile.getAbsolutePath();
			
			// Loads selected picture
			Image image = new Image(selectedFile.toURI().toString());
			playlistPicture.setImage(image);
			
			// Clips picture corners
			Rectangle rect = new Rectangle(220, 220);
			rect.setArcHeight(90);
	        rect.setArcWidth(90);
	        rect.setEffect(new Reflection());
	        playlistPicture.setClip(rect);

			showStatus("Playlist picture selected.", false);
		}
	}
	
	/**
	 * Closes the add playlist popup.
	 */
	@FXML
	private void handleClose() {
		if(closeAction != null)
			closeAction.run();
	}
	
	/**
	 * Displays a success or error message.
	 *
	 * @param message message to display
	 * @param error true for error styling, otherwise false
	 */
	private void showStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setTextFill(error ? javafx.scene.paint.Color.web("#FF8F9B") : javafx.scene.paint.Color.web("#9BE7B0"));
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
	}
}