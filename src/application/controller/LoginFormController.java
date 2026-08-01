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

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;
    
    private UserDAO userDAO;
    private Runnable closeAction;
    private Runnable loginSuccessAction;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    public void setConnection(Connection conn) {
        this.userDAO = new UserDAO(conn);
    }

    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    public void setLoginSuccessAction(Runnable loginSuccessAction) {
        this.loginSuccessAction = loginSuccessAction;
    }

    @FXML
    private void handleLogin() {
    	boolean valid = true;
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both your username and password.");
            valid = false;
        }

        if (userDAO == null && valid) {
            showError("Database connection is unavailable.");
            valid = false;
        }

        try {
            boolean validLogin = userDAO.login(username, password);

            if (!validLogin && valid) {
                showError("Incorrect username or password.");
                passwordField.clear();
                passwordField.requestFocus();
                valid = false;
            }

            int userId = userDAO.getUserID(username);

            if (userId == -1 && valid) {
                showError("Unable to retrieve the user account.");
                valid = false;
            }

            UserSession.setCurrentUser(userId, username);

            if (loginSuccessAction != null && valid) {
                loginSuccessAction.run();
            }
        } catch (SQLException e) {
            showError("Unable to log in. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        if (closeAction != null) {
            closeAction.run();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}