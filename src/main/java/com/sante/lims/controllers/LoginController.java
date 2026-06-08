package com.sante.lims.controllers;

import com.sante.lims.models.User;
import com.sante.lims.service.AuthService;
import com.sante.lims.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void handleLogin() {
        AuthService.LoginResult result = authService.login(emailField.getText(), passwordField.getText());
        if (result.mustChangePassword()) {
            SceneNavigator.switchScene("/fxml/change-password.fxml", "Sante LIMS - Change Password");
            return;
        }
        if (!result.success()) {
            messageLabel.setText(result.message());
            return;
        }
        openDashboard(result.user());
    }

    @FXML
    public void handleRegister() {
        SceneNavigator.switchScene("/fxml/register.fxml", "Sante LIMS - Customer Registration");
    }

    private void openDashboard(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        switch (role) {
            case "SUPER_ADMIN" -> SceneNavigator.switchScene("/fxml/admin-dashboard.fxml", "Sante LIMS - Super Admin");
            case "LAB_ATTENDANT" -> SceneNavigator.switchScene("/fxml/attendant-dashboard.fxml", "Sante LIMS - Lab Attendant");
            case "CUSTOMER" -> SceneNavigator.switchScene("/fxml/customer-dashboard.fxml", "Sante LIMS - Customer Dashboard");
            default -> messageLabel.setText("No dashboard configured for role: " + user.getRole());
        }
    }
}
