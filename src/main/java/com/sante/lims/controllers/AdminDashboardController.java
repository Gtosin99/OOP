package com.sante.lims.controllers;

import com.sante.lims.services.AuditLogService;
import com.sante.lims.util.SceneNavigator;
import com.sante.lims.util.SessionContext;
import com.sante.lims.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class AdminDashboardController {
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        showUsers();
    }

    @FXML public void showUsers() { loadView("/fxml/user-management.fxml"); }
    @FXML public void showTests() { loadView("/fxml/test-builder.fxml"); }
    @FXML public void showLabQueue() { loadView("/fxml/request-queue.fxml"); }
    @FXML public void showAuditLogs() { loadView("/fxml/audit-logs.fxml"); }

    @FXML
    public void handleLogout() {
        try {
            if (SessionManager.getCurrentUser() != null) {
                new AuditLogService().logAction(SessionManager.getCurrentUser().getId(), "LOGOUT", "User logged out.");
            }
        } catch (Exception ignored) {
        }
        SessionManager.endSession();
        SessionContext.clear();
        SceneNavigator.switchScene("/fxml/login.fxml", "Sante LIMS - Login");
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load " + fxmlPath, ex);
        }
    }
}
