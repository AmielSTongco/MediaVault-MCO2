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
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both your username and password.");
            return;
        }

        if (userDAO == null) {
            showError("Database connection is unavailable.");
            return;
        }

        try {
            boolean validLogin = userDAO.login(username, password);

            if (!validLogin) {
                showError("Incorrect username or password.");
                passwordField.clear();
                passwordField.requestFocus();
                return;
            }

            int userId = userDAO.getUserID(username);

            if (userId == -1) {
                showError("Unable to retrieve the user account.");
                return;
            }

            UserSession.setCurrentUser(userId, username);

            if (loginSuccessAction != null) {
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