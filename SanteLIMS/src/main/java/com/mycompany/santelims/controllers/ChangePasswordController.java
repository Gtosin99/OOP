package com.mycompany.santelims.controllers;

import com.mycompany.santelims.dao.UserDao;
import com.mycompany.santelims.models.User;
import com.mycompany.santelims.services.AuditService;
import com.mycompany.santelims.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class ChangePasswordController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UserDao userDao = new UserDao();

    @FXML
    public void handleChangePassword() {
        String newPass     = newPasswordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            showMessage("All fields are required.", "red");
            return;
        }

        if (newPass.length() < 6) {
            showMessage("Password must be at least 6 characters.", "red");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showMessage("Passwords do not match.", "red");
            return;
        }

        User user = Session.getCurrentUser();
        String hashed = BCrypt.hashpw(newPass, BCrypt.gensalt());

        userDao.updatePassword(user.getId(), hashed);
        AuditService.logPasswordChange(user.getEmail());

        // Update session
        user.setPasswordHash(hashed);
        user.setMustChangePassword(false);
        Session.setCurrentUser(user);

        // Go to dashboard
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/admin-dashboard.fxml"));
            Stage stage  = (Stage) newPasswordField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sante LIMS - Dashboard");
            stage.show();
        } catch (Exception e) {
            showMessage("Failed to load dashboard.", "red");
            e.printStackTrace();
        }
    }

    private void showMessage(String text, String color) {
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
        messageLabel.setText(text);
    }
}
