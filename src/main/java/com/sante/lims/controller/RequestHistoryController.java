package com.sante.lims.controller;

import com.sante.lims.model.TestRequest;
import com.sante.lims.service.CustomerService;
import com.sante.lims.service.ReportService;
import com.sante.lims.util.SceneNavigator;
import com.sante.lims.util.SessionContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RequestHistoryController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<TestRequest> historyTable;
    @FXML
    private TableColumn<TestRequest, String> colRequestId;
    @FXML
    private TableColumn<TestRequest, String> colTestName;
    @FXML
    private TableColumn<TestRequest, String> colRequested;
    @FXML
    private TableColumn<TestRequest, String> colExpected;
    @FXML
    private TableColumn<TestRequest, String> colPayment;
    @FXML
    private TableColumn<TestRequest, String> colSample;
    @FXML
    private TableColumn<TestRequest, String> colStatus;

    private final CustomerService customerService = new CustomerService();
    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        configureTable();
        loadHistory();
    }

    private void configureTable() {
        colRequestId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getRequestId())));
        colTestName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTestName()));
        colRequested.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestDate().format(DATE_FORMAT)));
        colExpected.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpectedCompletion().format(DATE_FORMAT)));
        colPayment.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentStatus()));
        colSample.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSampleStatus()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProcessingStatus()));
    }

    private void loadHistory() {
        long customerId = SessionContext.getCurrentCustomerId();
        try {
            List<TestRequest> requests = customerService.getRequestHistory(customerId);
            historyTable.setItems(FXCollections.observableArrayList(requests));
        } catch (SQLException e) {
            showError("Unable to load request history", e.getMessage());
        }
    }

    @FXML
    private void exportHistoryReport() {
        long customerId = SessionContext.getCurrentCustomerId();
        try {
            List<TestRequest> requests = customerService.getRequestHistory(customerId);
            Path reportFile = reportService.exportRequestHistoryCsv(requests, customerId);
            showInfo("Report generated", "Request history saved to: " + reportFile.toAbsolutePath());
        } catch (SQLException | IOException e) {
            showError("Unable to export report", e.getMessage());
        }
    }

    @FXML
    private void backToDashboard() {
        SceneNavigator.switchScene("/fxml/customer-dashboard.fxml", "Customer Dashboard");
    }

    @FXML
    private void openCatalog() {
        SceneNavigator.switchScene("/fxml/test-catalog.fxml", "Test Catalog");
    }

    @FXML
    private void openVault() {
        SceneNavigator.switchScene("/fxml/result-vault.fxml", "Result Vault");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
