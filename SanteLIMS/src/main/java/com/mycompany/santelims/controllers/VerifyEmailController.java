package com.mycompany.santelims.controllers;

import com.mycompany.santelims.services.EmailVerificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class VerifyEmailController {

    @FXML private TextField tokenField;
    @FXML private Label messageLabel;

    private final EmailVerificationService verificationService = new EmailVerificationService();

    @FXML
    public void handleVerify() {
        String token = tokenField.getText().trim().toUpperCase();

        if (token.isEmpty()) {
            showMessage("Please enter your verification token.", "red");
            return;
        }

        String result = verificationService.verifyToken(token);

        if (result.equals("SUCCESS")) {
            showMessage("Email verified successfully! You can now log in.", "green");
            tokenField.clear();

            // Redirect to login after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
                            Stage stage = (Stage) tokenField.getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Sante LIMS - Login");
                            stage.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            showMessage(result, "red");
        }
    }

    @FXML
    public void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) tokenField.getScene().getWindow();
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