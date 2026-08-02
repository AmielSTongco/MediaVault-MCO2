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
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SettingsController {
	
	/*
	 * Controls account settings including username, password,
	 * profile picture, logout, and account deletion
	 */

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

	private UserDAO userDAO;
	private Runnable closeAction;
	private Runnable deleteSuccessAction;
	private Runnable profileUpdatedAction;
	private String selectedProfilePicturePath;
	
	/**
	 * Initializes account fields, status message, and default profile picture.
	 */
	@FXML
	public void initialize() {
		statusLabel.setVisible(false);
		statusLabel.setManaged(false);

		usernameField.setText(UserSession.getCurrentUsername());
		loadDefaultProfilePicture();
	}
	
	/**
	 * Sets database connection and initializes user data access.
	 *
	 * @param conn active database connection
	 */
	public void setConnection(Connection conn) {
		userDAO = new UserDAO(conn);
		loadCurrentProfilePicture();
	}
	
	/**
	 * Sets action executed when settings popup closes.
	 *
	 * @param closeAction callback to execute
	 */
	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}
	
	/**
	 * Sets action executed after successful account deletion.
	 *
	 * @param deleteSuccessAction callback to execute
	 */
	public void setDeleteSuccessAction(Runnable deleteSuccessAction) {
		this.deleteSuccessAction = deleteSuccessAction;
	}
	
	/**
	 * Sets action executed after profile information changes.
	 *
	 * @param profileUpdatedAction callback to execute
	 */
	public void setProfileUpdatedAction(Runnable profileUpdatedAction) {
		this.profileUpdatedAction = profileUpdatedAction;
	}
	
	/**
	 * Opens a file chooser and previews selected profile picture.
	 */
	@FXML
	private void handleProfilePicture() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Profile Picture");
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

		File selectedFile = chooser.showOpenDialog(profilePicture.getScene().getWindow());

		if(selectedFile != null)
		{
			selectedProfilePicturePath = selectedFile.getAbsolutePath();

			// Displays selected profile picture
			Image image = new Image(selectedFile.toURI().toString());
			setCircularProfileImage(profilePicture, image);

			showStatus("Profile picture selected.", false);
		}
	}
	
	/**
	 * Validates and saves changed account information.
	 */
	@FXML
	private void handleSaveChanges() {
		if(userDAO == null)
			showStatus("Database connection is unavailable.", true);
		else
		{
			int userId = UserSession.getCurrentUserId();
			String newUsername = usernameField.getText().trim();
			String currentPassword = currentPasswordField.getText();
			String newPassword = newPasswordField.getText();
			boolean valid = true;

			// Validates username
			if(newUsername.isEmpty())
			{
				showStatus("Username cannot be empty.", true);
				valid = false;
			}

			// Requires current password before changing password
			if(valid && !newPassword.isEmpty() && currentPassword.isEmpty())
			{
				showStatus("Enter your current password.", true);
				valid = false;
			}

			try {
				// Verifies current password
				if(valid && !newPassword.isEmpty() && !userDAO.verifyPassword(userId, currentPassword))
				{
					showStatus("Current password is incorrect.", true);
					valid = false;
				}

				if(valid)
				{
					// Updates username
					if(!newUsername.equals(UserSession.getCurrentUsername()))
					{
						userDAO.updateUsername(userId, newUsername);
						UserSession.setCurrentUser(userId, newUsername);
					}

					// Updates password
					if(!newPassword.isEmpty())
						userDAO.updatePassword(userId, newPassword);

					// Updates profile picture
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
	
	/**
	 * Confirms and permanently deletes the current account.
	 */
	@FXML
	private void handleDeleteAccount() {
		Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
		confirmation.setTitle("Delete Account");
		confirmation.setHeaderText("Delete your MediaVault account?");
		confirmation.setContentText("This action cannot be undone.");

		/* Use of Optional.orElse learned from Java documentation */
		ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);

		if(result == ButtonType.OK)
		{
			try {
				userDAO.deleteUser(UserSession.getCurrentUserId());
				UserSession.clear();

				if(closeAction != null)
					closeAction.run();

				if(deleteSuccessAction != null)
					deleteSuccessAction.run();

				navigateToLogin();
			}
			catch(SQLException e) {
				showStatus("Unable to delete account.", true);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Closes settings popup.
	 */
	@FXML
	private void handleClose() {
		if(closeAction != null)
			closeAction.run();
	}
	
	/**
	 * Clears current session and returns to login screen.
	 *
	 * @param event button click event
	 */
	@FXML
	private void handleLogOut(ActionEvent event) {
		UserSession.clear();
		navigateToLogin();
	}
	
	/**
	 * Switches scene back to login screen.
	 */
	private void navigateToLogin() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/application/fxml/Login.fxml"));
			Parent root = loader.load();

			Stage stage = (Stage)usernameField.getScene().getWindow();
			stage.getScene().setRoot(root);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads current user's saved profile picture.
	 */
	private void loadCurrentProfilePicture() {
		if(userDAO != null)
		{
			try {
				String path = userDAO.getProfilePicture(UserSession.getCurrentUserId());

				if(path != null && !path.isBlank())
				{
					File file = new File(path);

					if(file.exists())
					{
						Image image = new Image(file.toURI().toString());
						setCircularProfileImage(profilePicture, image);
					}
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
	}
	
	/**
	 * Loads default profile picture.
	 */
	private void loadDefaultProfilePicture() {
		Image image = new Image(getClass().getResourceAsStream("/resources/application/images/default/default-profile.png"));
		setCircularProfileImage(profilePicture, image);
	}
	
	/**
	 * Displays success or error message.
	 *
	 * @param message message to display
	 * @param error true for error styling, otherwise false
	 */
	private void showStatus(String message, boolean error) {
		/* Use of ternary learned from Exercism */
		statusLabel.setText(message);
		statusLabel.setTextFill(error ? javafx.scene.paint.Color.web("#FF8F9B") : javafx.scene.paint.Color.web("#9BE7B0"));
		statusLabel.setVisible(true);
		statusLabel.setManaged(true);
	}
	
	/**
	 * Crops and clips an image into a circular profile picture.
	 *
	 * @param imageView target image view
	 * @param image profile picture
	 */
	private void setCircularProfileImage(ImageView imageView, Image image) {
		imageView.setFitWidth(140);
		imageView.setFitHeight(140);
		imageView.setPreserveRatio(false);
		imageView.setSmooth(true);

		// Calculates centered square crop
		double imageWidth = image.getWidth();
		double imageHeight = image.getHeight();
		double cropSize = Math.min(imageWidth, imageHeight);
		double cropX = (imageWidth - cropSize)/2.0;
		double cropY = (imageHeight - cropSize)/2.0;

		imageView.setViewport(new Rectangle2D(cropX, cropY, cropSize, cropSize));
		imageView.setImage(image);

		// Applies circular clip
		Circle clip = new Circle();
		clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
		clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2));
		clip.radiusProperty().bind(imageView.fitWidthProperty().divide(2));

		imageView.setClip(clip);
	}
}