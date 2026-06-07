package com.sante.lims.controllers;

import com.sante.lims.models.TestRequestQueueItem;
import com.sante.lims.models.User;
import com.sante.lims.services.LabRequestService;
import com.sante.lims.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class RequestQueueController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> paymentFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private TableView<TestRequestQueueItem> requestTable;
    @FXML
    private TableColumn<TestRequestQueueItem, Integer> idColumn;
    @FXML
    private TableColumn<TestRequestQueueItem, String> customerColumn;
    @FXML
    private TableColumn<TestRequestQueueItem, String> emailColumn;
    @FXML
    private TableColumn<TestRequestQueueItem, String> testColumn;
    @FXML
    private TableColumn<TestRequestQueueItem, String> paymentColumn;
    @FXML
    private TableColumn<TestRequestQueueItem, LocalDateTime> expectedColumn;
    @FXML
    private TableColumn<TestRequestQueueItem, String> statusColumn;

    private final LabRequestService labRequestService = new LabRequestService();

    @FXML
    public void initialize() {
        paymentFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "PAID", "UNPAID"));
        paymentFilter.setValue("ALL");
        statusFilter.setItems(FXCollections.observableArrayList(
                "ALL", "REQUESTED", "SAMPLE_COLLECTED", "SAMPLE_PROCESSING", "SAMPLE_VALIDATED", "COMPLETED"));
        statusFilter.setValue("ALL");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        testColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        expectedColumn.setCellValueFactory(new PropertyValueFactory<>("expectedCompletion"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        refreshRequests();
    }

    @FXML
    private void refreshRequests() {
        try {
            requestTable.setItems(FXCollections.observableArrayList(
                    labRequestService.findRequests(searchField.getText(), paymentFilter.getValue(), statusFilter.getValue())));
        } catch (SQLException exception) {
            showError("Unable to load test requests.", exception);
        }
    }

    @FXML
    private void markSelectedRequestPaid() {
        TestRequestQueueItem selectedRequest = requestTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showInfo("Select a test request first.");
            return;
        }

        try {
            labRequestService.markRequestAsPaid(selectedRequest.getId(), getCurrentUserId());
            refreshRequests();
            showInfo("Payment marked as paid.");
        } catch (SQLException exception) {
            showError("Unable to update payment status.", exception);
        }
    }

    private int getCurrentUserId() {
        User user = SessionManager.getCurrentUser();
        return user == null ? 1 : user.getId();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void showError(String message, Exception exception) {
        new Alert(Alert.AlertType.ERROR, message + "\n" + exception.getMessage()).showAndWait();
    }
}
