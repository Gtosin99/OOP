package com.sante.lims.controller;

import com.sante.lims.dao.CustomerModuleRepository;
import com.sante.lims.model.TestCatalogItem;
import com.sante.lims.service.CustomerService;
import com.sante.lims.util.AppConfig;
import com.sante.lims.util.SceneNavigator;
import com.sante.lims.util.SessionContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class RequestController {

    @FXML
    private TableView<TestCatalogItem> catalogTable;
    @FXML
    private TableColumn<TestCatalogItem, String> colName;
    @FXML
    private TableColumn<TestCatalogItem, String> colCategory;
    @FXML
    private TableColumn<TestCatalogItem, String> colPrice;
    @FXML
    private TableColumn<TestCatalogItem, String> colTat;
    @FXML
    private TableColumn<TestCatalogItem, String> colFormat;

    private final CustomerService customerService = new CustomerService();
    private final CustomerModuleRepository repository = new CustomerModuleRepository();

    @FXML
    public void initialize() {
        configureTable();
        loadCatalog();
    }

    private void configureTable() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        colPrice.setCellValueFactory(data -> new SimpleStringProperty("NGN " + data.getValue().getPrice().setScale(2, RoundingMode.HALF_UP)));
        colTat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTatHours() + " hrs"));
        colFormat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResultFormat()));
    }

    private void loadCatalog() {
        try {
            List<TestCatalogItem> tests = customerService.getTestCatalog();
            catalogTable.setItems(FXCollections.observableArrayList(tests));
        } catch (SQLException e) {
            showError("Unable to load tests", e.getMessage());
        }
    }

    @FXML
    private void placeOrder() {
        TestCatalogItem selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a test", "Please select a test from the catalog before placing an order.");
            return;
        }

        long customerId = SessionContext.getCurrentCustomerId();
        try {
            long requestId = customerService.placeOrder(customerId, selected.getId());

            String bankDetails = "Bank: " + AppConfig.get("lab.bank.name")
                    + "\nAccount Name: " + AppConfig.get("lab.bank.accountName")
                    + "\nAccount Number: " + AppConfig.get("lab.bank.accountNumber");

            repository.createNotification(
                    customerId,
                    "Order Submitted",
                    "Order #" + requestId + " submitted for " + selected.getName() + ". Please complete transfer payment."
            );

            showInfo(
                    "Order Confirmation",
                    "Order #" + requestId + " submitted successfully.\n\n"
                            + "Please make bank transfer payment using:\n"
                            + bankDetails
            );
        } catch (SQLException e) {
            showError("Failed to place order", e.getMessage());
        }
    }

    @FXML
    private void backToDashboard() {
        SceneNavigator.switchScene("/fxml/customer-dashboard.fxml", "Customer Dashboard");
    }

    @FXML
    private void openHistory() {
        SceneNavigator.switchScene("/fxml/request-history.fxml", "Request History");
    }

    @FXML
    private void openResultVault() {
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
