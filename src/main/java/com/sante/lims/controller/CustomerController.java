package com.sante.lims.controller;

import com.sante.lims.model.CustomerProfile;
import com.sante.lims.model.NotificationItem;
import com.sante.lims.model.TestRequest;
import com.sante.lims.service.CustomerService;
import com.sante.lims.util.SceneNavigator;
import com.sante.lims.util.SessionContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private Label lblName;
    @FXML
    private Label lblEmail;
    @FXML
    private Label lblVerification;

    @FXML
    private TableView<TestRequest> activeRequestsTable;
    @FXML
    private TableColumn<TestRequest, String> colTestName;
    @FXML
    private TableColumn<TestRequest, String> colRequestDate;
    @FXML
    private TableColumn<TestRequest, String> colStatus;
    @FXML
    private TableColumn<TestRequest, String> colCountdown;

    @FXML
    private TableView<NotificationItem> notificationsTable;
    @FXML
    private TableColumn<NotificationItem, String> colSubject;
    @FXML
    private TableColumn<NotificationItem, String> colMessage;
    @FXML
    private TableColumn<NotificationItem, String> colCreated;

    private final CustomerService customerService = new CustomerService();
    private final ObservableList<TestRequest> activeRequests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTables();
        loadDashboard();

        Timeline timeline = new Timeline(new KeyFrame(Duration.minutes(1), event -> activeRequestsTable.refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void configureTables() {
        colTestName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTestName()));
        colRequestDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestDate().format(DATE_FORMAT)));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProcessingStatus()));
        colCountdown.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCountdownDisplay()));

        colSubject.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSubject()));
        colMessage.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));
        colCreated.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt().format(DATE_FORMAT)));

        activeRequestsTable.setItems(activeRequests);
    }

    private void loadDashboard() {
        long customerId = SessionContext.getCurrentCustomerId();
        try {
            CustomerProfile profile = customerService.getProfile(customerId);
            if (profile != null) {
                lblName.setText(profile.getFullName());
                lblEmail.setText(profile.getEmail());
                lblVerification.setText(profile.isEmailVerified() ? "Verified" : "Pending Verification");
            }

            List<TestRequest> active = customerService.getActiveRequests(customerId);
            activeRequests.setAll(active);

            List<NotificationItem> notifications = customerService.getNotifications(customerId);
            notificationsTable.setItems(FXCollections.observableArrayList(notifications));
        } catch (SQLException e) {
            showError("Unable to load dashboard", e.getMessage());
        }
    }

    @FXML
    private void openCatalog() {
        SceneNavigator.switchScene("/fxml/test-catalog.fxml", "Test Catalog");
    }

    @FXML
    private void openHistory() {
        SceneNavigator.switchScene("/fxml/request-history.fxml", "Request History");
    }

    @FXML
    private void openVault() {
        SceneNavigator.switchScene("/fxml/result-vault.fxml", "Result Vault");
    }

    @FXML
    private void refreshDashboard() {
        loadDashboard();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
