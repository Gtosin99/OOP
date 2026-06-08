package com.mycompany.santelims.controllers;

import com.mycompany.santelims.services.AuditService;
import com.mycompany.santelims.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        showUsers();
    }

    @FXML public void showUsers() { loadView("/user-management.fxml"); }
    @FXML public void showTests() { loadView("/test-builder.fxml"); }
    @FXML public void showSamples() { loadView("/sample-view.fxml"); }
    @FXML public void showAuditLogs() { loadView("/audit-logs.fxml"); }

    @FXML
    public void handleLogout() {
        if (Session.getCurrentUser() != null) {
            AuditService.logLogout(Session.getCurrentUser().getEmail());
        }
        Session.setCurrentUser(null);
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sante LIMS - Login");
            stage.show();
        } catch (Exception e) {
            System.out.println("=== Logout error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.out.println("=== Failed to load: " + fxmlPath);
            e.printStackTrace();
        }
    }
}