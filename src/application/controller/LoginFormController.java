package application.controller;

import java.sql.Connection;
import java.sql.SQLException;

import application.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import application.model.UserSession;

public class LoginFormController {
	
	/*
	 * Controls the pop-up where a person who has
	 * made an account before can log-in
	 */

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;
    
    private UserDAO userDAO;
    private Runnable closeAction;
    private Runnable loginSuccessAction;

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
		this.userDAO = new UserDAO(conn);
	}
	
	/**
	 * Sets action executed when login form closes.
	 *
	 * @param closeAction callback to execute
	 */
	public void setCloseAction(Runnable closeAction) {
		this.closeAction = closeAction;
	}
	
	/**
	 * Sets action executed after successful login.
	 *
	 * @param loginSuccessAction callback to execute
	 */
	public void setLoginSuccessAction(Runnable loginSuccessAction) {
		this.loginSuccessAction = loginSuccessAction;
	}
	
	/**
	 * Validates login fields and verifies user credentials.
	 */
	@FXML
	private void handleLogin() {
		boolean valid = true;
		String username = usernameField.getText().trim();
		String password = passwordField.getText();
		
		// Validates required fields
		if(username.isEmpty() || password.isEmpty())
		{
			showError("Please enter both your username and password.");
			valid = false;
		}
		
		// Checks database connection
		if(userDAO == null && valid)
		{
			showError("Database connection is unavailable.");
			valid = false;
		}

		if(valid)
		{
			try {
				boolean validLogin = userDAO.login(username, password);
				
				// Checks entered credentials
				if(!validLogin)
				{
					showError("Incorrect username or password.");
					passwordField.clear();
					passwordField.requestFocus();
					valid = false;
				}
				
				if(valid)
				{
					int userId = userDAO.getUserID(username);
					
					// Checks retrieved user account
					if(userId == -1)
					{
						showError("Unable to retrieve the user account.");
						valid = false;
					}
					else
						UserSession.setCurrentUser(userId, username);
				}
				
				if(loginSuccessAction != null && valid)
					loginSuccessAction.run();
			}
			catch(SQLException e) {
				showError("Unable to log in. Please try again.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Closes login form.
	 */
	@FXML
	private void handleClose() {
		if(closeAction != null)
			closeAction.run();
	}
	
	/**
	 * Displays login error message.
	 *
	 * @param message error message
	 */
	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setVisible(true);
		errorLabel.setManaged(true);
	}
}