package application.controller;

import java.sql.Connection;
import java.sql.SQLException;

import application.dao.UserDAO;
import application.model.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupFormController {
	
	/*
	 * Controls the signup form and creates new user accounts
	 */

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private PasswordField confirmPasswordField;

	@FXML
	private Label errorLabel;

	private UserDAO userDAO;
	private Runnable closeAction;
	private Runnable signupSuccessAction;

	/**
	 * Initializes error message visibility.
	 */
	@FXML
	public void initialize() {
		errorLabel.setVisible(false);
		errorLabel.setManaged(false);
	}

	/**
	 * Sets database connection and initializes user data access.
	 *
	 * @param conn active database connection
	 */
	public void setConnection(Connection conn) {
		userDAO = new UserDAO(conn);
	}

	/**
	 * Sets action executed when signup form closes.
	 *
	 * @param closeAction callback to execute
	 */
	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}

	/**
	 * Sets action executed after successful signup.
	 *
	 * @param signupSuccessAction callback to execute
	 */
	public void setSignupSuccessAction(Runnable signupSuccessAction) {
		this.signupSuccessAction = signupSuccessAction;
	}

	/**
	 * Validates signup fields and creates a new user account.
	 */
	@FXML
	private void handleSignup() {
		String username = usernameField.getText().trim();
		String password = passwordField.getText();
		String confirmPassword = confirmPasswordField.getText();

		// Validates required fields
		if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty())
			showError("Please complete all fields.");
		// Checks matching passwords
		else if(!password.equals(confirmPassword))
		{
			showError("Passwords do not match.");
			passwordField.clear();
			confirmPasswordField.clear();
			passwordField.requestFocus();
		}
		// Checks database connection
		else if(userDAO == null)
			showError("Database connection is unavailable.");
		else
		{
			try {
				// Prevents duplicate usernames
				if(userDAO.usernameExists(username))
				{
					showError("That username is already taken.");
					usernameField.requestFocus();
				}
				else
				{
					userDAO.addUser(username, password);

					int userId = userDAO.getUserID(username);

					if(userId == -1)
						showError("Unable to create the account.");
					else
					{
						UserSession.setCurrentUser(userId, username);

						if(signupSuccessAction != null)
							signupSuccessAction.run();
					}
				}
			}
			catch(SQLException e) {
				showError("Unable to create the account. Please try again.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Closes signup form.
	 */
	@FXML
	private void handleClose() {
		if(closeAction != null)
			closeAction.run();
	}

	/**
	 * Displays signup error message.
	 *
	 * @param message error message
	 */
	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setManaged(true);
		errorLabel.setVisible(true);
		errorLabel.toFront();
	}
}