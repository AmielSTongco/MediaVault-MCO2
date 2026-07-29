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

    public void setSignupSuccessAction(Runnable signupSuccessAction) {
        this.signupSuccessAction = signupSuccessAction;
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please complete all fields.");
        }

        else if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            passwordField.clear();
            confirmPasswordField.clear();
            passwordField.requestFocus();
        }

        else if (userDAO == null) {
            showError("Database connection is unavailable.");
        }

        else {
            try {
                if (userDAO.usernameExists(username)) {
                    showError("That username is already taken.");
                    usernameField.requestFocus();
                }

                else {
                    userDAO.addUser(username, password);

                    int userId = userDAO.getUserID(username);

                    if (userId == -1) {
                        showError("Unable to create the account.");
                    }

                    else {
                        UserSession.setCurrentUser(userId, username);

                        if (signupSuccessAction != null) {
                            signupSuccessAction.run();
                        }
                    }
                }
            }

            catch (SQLException e) {
                showError("Unable to create the account. Please try again.");
                e.printStackTrace();
            }
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
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
        errorLabel.toFront();
    }
}