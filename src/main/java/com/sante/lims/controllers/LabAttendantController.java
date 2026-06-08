package com.sante.lims.controllers;

import com.sante.lims.services.AuditLogService;
import com.sante.lims.util.SceneNavigator;
import com.sante.lims.util.SessionContext;
import com.sante.lims.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class LabAttendantController {
    @FXML
    private BorderPane contentPane;

    @FXML
    public void initialize() {
        showRequestQueue();
    }

    @FXML
    private void showRequestQueue() {
        loadScreen("/fxml/request-queue.fxml");
    }

    @FXML
    private void showSampleTracking() {
        loadScreen("/fxml/sample-tracking.fxml");
    }

    @FXML
    private void showResultUpload() {
        loadScreen("/fxml/result-upload.fxml");
    }

    @FXML
    private void handleLogout() {
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

    private void loadScreen(String fxmlPath) {
        try {
            Parent screen = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.setCenter(screen);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + fxmlPath, exception);
        }
    }
}
