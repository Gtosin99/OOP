package com.mycompany.santelims.controllers;

import com.mycompany.santelims.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText().trim();

        String result = authService.login(email, password);

        switch (result) {
            case "SUCCESS" ->
                loadScene("/admin-dashboard.fxml", "Sante LIMS - Dashboard");
            case "MUST_CHANGE_PASSWORD" ->
                loadScene("/change-password.fxml", "Sante LIMS - Change Password");
            default ->
                messageLabel.setText(result);
        }
    }

    @FXML
    public void handleRegister() {
        loadScene("/register.fxml", "Sante LIMS - Create Account");
    }

    private void loadScene(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Failed to load screen.");
            e.printStackTrace();
        }
    }
}