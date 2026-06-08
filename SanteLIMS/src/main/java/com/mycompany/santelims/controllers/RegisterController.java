package com.mycompany.santelims.controllers;

import com.mycompany.santelims.dao.UserDao;
import com.mycompany.santelims.models.Role;
import com.mycompany.santelims.models.User;
import com.mycompany.santelims.services.AuditService;
import com.mycompany.santelims.services.EmailVerificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UserDao userDao = new UserDao();
    private final EmailVerificationService verificationService = new EmailVerificationService();

    @FXML
    public void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm  = confirmPasswordField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showMessage("All fields are required.", "red");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showMessage("Enter a valid email address.", "red");
            return;
        }

        if (password.length() < 6) {
            showMessage("Password must be at least 6 characters.", "red");
            return;
        }

        if (!password.equals(confirm)) {
            showMessage("Passwords do not match.", "red");
            return;
        }

        User existing = userDao.findByEmail(email);
        if (existing != null) {
            showMessage("An account with this email already exists.", "red");
            return;
        }

        // Create customer account
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        Role role = new Role();
        role.setRoleName("CUSTOMER");

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(hashed);
        user.setMustChangePassword(false);
        user.setVerified(false);
        user.setRole(role);

        userDao.addUser(user);
        AuditService.log(email, "SELF_REGISTRATION", fullName + " registered as CUSTOMER");

        // Generate and print verification token to console
        verificationService.generateAndSendToken(email);

        showMessage("Account created! Check the console for your verification token.", "green");

        // Redirect to verify screen
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/verify-email.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sante LIMS - Verify Email");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sante LIMS - Login");
            stage.show();
        } catch (Exception e) {
            showMessage("Failed to load login screen.", "red");
            e.printStackTrace();
        }
    }

    private void showMessage(String text, String color) {
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
        messageLabel.setText(text);
    }
}