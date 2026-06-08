package com.sante.lims.controllers;

import com.sante.lims.service.RegistrationService;
import com.sante.lims.service.RegistrationService.RegistrationResult;
import com.sante.lims.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final RegistrationService registrationService = new RegistrationService();

    @FXML
    public void handleRegister() {
        try {
            RegistrationResult result = registrationService.selfRegister(
                    fullNameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText()
            );
            if (!result.emailSent()) {
                messageLabel.setText("Account created. " + result.emailStatus() + " Token: " + result.token());
            }
            SceneNavigator.switchScene("/fxml/verify-email.fxml", "Sante LIMS - Verify Email");
        } catch (Exception ex) {
            messageLabel.setText("Unable to register: " + ex.getMessage());
        }
    }

    @FXML
    public void backToLogin() {
        SceneNavigator.switchScene("/fxml/login.fxml", "Sante LIMS - Login");
    }
}
