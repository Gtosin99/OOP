package com.sante.lims.controllers;

import com.sante.lims.service.RegistrationService;
import com.sante.lims.service.RegistrationService.RegistrationResult;
import com.sante.lims.util.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VerifyEmailController {
    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private Label messageLabel;

    private final RegistrationService registrationService = new RegistrationService();

    @FXML
    public void initialize() {
        RegistrationResult result = RegistrationService.getLastRegistrationResult();
        if (result != null) {
            emailField.setText(result.email());
            if (!result.emailSent()) {
                tokenField.setText(result.token());
                messageLabel.setText(result.emailStatus());
            }
        }
    }

    @FXML
    public void handleVerify() {
        try {
            boolean verified = registrationService.verifyEmailToken(tokenField.getText().trim(), emailField.getText().trim());
            if (verified) {
                messageLabel.setText("Email verified. You can now log in.");
            } else {
                messageLabel.setText("Invalid or expired verification token.");
            }
        } catch (Exception ex) {
            messageLabel.setText("Unable to verify email: " + ex.getMessage());
        }
    }

    @FXML
    public void backToLogin() {
        SceneNavigator.switchScene("/fxml/login.fxml", "Sante LIMS - Login");
    }
}
