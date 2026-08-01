package application.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import application.dao.UserDAO;
import application.model.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Circle;

public class SettingsController {

	@FXML
	private ImageView profilePicture;

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField currentPasswordField;

	@FXML
	private PasswordField newPasswordField;

	@FXML
	private Label statusLabel;

	private Runnable profileUpdatedAction;
	
	private UserDAO userDAO;
	private Runnable closeAction;
	private Runnable deleteSuccessAction;
	private String selectedProfilePicturePath;

	@FXML
	public void initialize() {
		statusLabel.setVisible(false);
		statusLabel.setManaged(false);
		usernameField.setText(UserSession.getCurrentUsername());
		loadDefaultProfilePicture();
	}

	public void setConnection(Connection conn) {
		userDAO = new UserDAO(conn);
		loadCurrentProfilePicture();
	}

	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	public void setDeleteSuccessAction(Runnable deleteSuccessAction) {
		this.deleteSuccessAction = deleteSuccessAction;
	}
	
	public void setProfileUpdatedAction(Runnable profileUpdatedAction) {
		this.profileUpdatedAction = profileUpdatedAction;
	}
	
	@FXML
	private void handleProfilePicture() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Profile Picture");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

		File selectedFile = chooser.showOpenDialog(profilePicture.getScene().getWindow());

		if(selectedFile != null) {
			selectedProfilePicturePath = selectedFile.getAbsolutePath();

			Image image = new Image(selectedFile.toURI().toString());
			setCircularProfileImage(profilePicture, image);

			showStatus("Profile picture selected.", false);
		}
	}

	@FXML
	private void handleSaveChanges() {
		if(userDAO == null) {
			showStatus("Database connection is unavailable.", true);
		}
		else {
			int userId = UserSession.getCurrentUserId();
			String newUsername = usernameField.getText().trim();
			String currentPassword = currentPasswordField.getText();
			String newPassword = newPasswordField.getText();
			boolean valid = true;

			if(newUsername.isEmpty()) {
				showStatus("Username cannot be empty.", true);
				valid = false;
			}

			if(valid && !newPassword.isEmpty() && currentPassword.isEmpty()) {
				showStatus("Enter your current password.", true);
				valid = false;
			}

			try {
				if(valid && !newPassword.isEmpty() && !userDAO.verifyPassword(userId, currentPassword)) {
					showStatus("Current password is incorrect.", true);
					valid = false;
				}

				if(valid) {
					if(!newUsername.equals(UserSession.getCurrentUsername())) {
						userDAO.updateUsername(userId, newUsername);
						UserSession.setCurrentUser(userId, newUsername);
					}

					if(!newPassword.isEmpty())
						userDAO.updatePassword(userId, newPassword);

					if(selectedProfilePicturePath != null)
						userDAO.updateProfilePicture(userId, selectedProfilePicturePath);
					
					if(profileUpdatedAction != null)
						profileUpdatedAction.run();

					currentPasswordField.clear();
					newPasswordField.clear();
					showStatus("Account settings updated.", false);
				}
			}
			catch(SQLException e) {
				showStatus("Unable to save account settings.", true);
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void handleDeleteAccount() {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Delete Account");
		confirmation.setHeaderText("Delete your MediaVault account?");
		confirmation.setContentText("This action cannot be undone.");

		ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

		if(result == ButtonType.OK) {
			try {
				userDAO.deleteUser(UserSession.getCurrentUserId());
				UserSession.clear();

				if(closeAction != null)
					closeAction.run();

				if(deleteSuccessAction != null)
					deleteSuccessAction.run();
			}
			catch(SQLException e) {
				showStatus("Unable to delete account.", true);
				e.printStackTrace();
			}
		}
		
		navigateToLogin();
	}

	@FXML
	private void handleClose() {
		if(closeAction != null)
			closeAction.run();
	}
	
	@FXML
	private void handleLogOut(ActionEvent event) {
		navigateToLogin();
	}
	
	/**
	 * Switches the scene back to the login screen
	 */
	private void navigateToLogin() {
		try {
	        FXMLLoader loader = new FXMLLoader(
	        getClass().getResource("/resources/application/fxml/Login.fxml"));

	        Parent root = loader.load();

	        Stage stage = (Stage) usernameField.getScene().getWindow(); // or any node in this controller
	        stage.getScene().setRoot(root);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	private void loadCurrentProfilePicture() {
		try {
			String path = userDAO.getProfilePicture(UserSession.getCurrentUserId());

			if(path != null && !path.isBlank()) {
				File file = new File(path);

				if(file.exists())
					profilePicture.setImage(new Image(file.toURI().toString()));
				else
					loadDefaultProfilePicture();
			}
			else
				loadDefaultProfilePicture();
		}
		catch(SQLException e) {
			loadDefaultProfilePicture();
			e.printStackTrace();
		}
	}
	
	private void loadDefaultProfilePicture() {
		profilePicture.setImage(new Image(getClass().getResourceAsStream("/resources/application/images/default/default-profile.png")));
	}

	private void showStatus(String message, boolean error) {
		statusLabel.setText(message);
		statusLabel.setTextFill(error ? javafx.scene.paint.Color.web("#FF8F9B") : javafx.scene.paint.Color.web("#9BE7B0"));
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
	}
	
	private void setCircularProfileImage(ImageView imageView, Image image) {
		imageView.setFitWidth(140);
		imageView.setFitHeight(140);
		imageView.setPreserveRatio(false);
		imageView.setSmooth(true);

		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double cropSize = Math.min(imageWidth, imageHeight);

		double cropX = (imageWidth - cropSize) / 2.0;
		double cropY = (imageHeight - cropSize) / 2.0;

		imageView.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		imageView.setImage(image);

		Circle clip = new Circle();
		clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
		clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2));
		clip.radiusProperty().bind(imageView.fitWidthProperty().divide(2));

		imageView.setClip(clip);
	}
}