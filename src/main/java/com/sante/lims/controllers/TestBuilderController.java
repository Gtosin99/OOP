package com.sante.lims.controllers;

import com.sante.lims.dao.TestTypeDao;
import com.sante.lims.model.TestCatalogItem;
import com.sante.lims.services.AuditLogService;
import com.sante.lims.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class TestBuilderController {
    @FXML private TextField testNameField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextField tatField;
    @FXML private ComboBox<String> resultFormatCombo;
    @FXML private Label messageLabel;
    @FXML private TableView<TestCatalogItem> testTable;
    @FXML private TableColumn<TestCatalogItem, String> colId;
    @FXML private TableColumn<TestCatalogItem, String> colName;
    @FXML private TableColumn<TestCatalogItem, String> colCategory;
    @FXML private TableColumn<TestCatalogItem, String> colPrice;
    @FXML private TableColumn<TestCatalogItem, String> colTat;
    @FXML private TableColumn<TestCatalogItem, String> colFormat;

    private final TestTypeDao testTypeDao = new TestTypeDao();
    private final AuditLogService auditLogService = new AuditLogService();

    @FXML
    public void initialize() {
        resultFormatCombo.setItems(FXCollections.observableArrayList("NUMERIC", "TEXT", "PDF", "IMAGE"));
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        colPrice.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrice().toPlainString()));
        colTat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTatHours() + " hrs"));
        colFormat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResultFormat()));
        testTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, item) -> populateForm(item));
        loadTests();
    }

    @FXML
    public void handleAdd() {
        try {
            testTypeDao.addTest(name(), category(), price(), tatHours(), resultFormat());
            log("TEST_CREATED", "Created test type " + name());
            handleClear();
            loadTests();
            messageLabel.setText("Test added.");
        } catch (Exception ex) {
            messageLabel.setText("Unable to add test: " + ex.getMessage());
        }
    }

    @FXML
    public void handleUpdate() {
        TestCatalogItem selected = testTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a test to update.");
            return;
        }
        try {
            testTypeDao.updateTest(selected.getId(), name(), category(), price(), tatHours(), resultFormat());
            log("TEST_UPDATED", "Updated test type " + name());
            handleClear();
            loadTests();
            messageLabel.setText("Test updated.");
        } catch (Exception ex) {
            messageLabel.setText("Unable to update test: " + ex.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        TestCatalogItem selected = testTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Select a test to remove.");
            return;
        }
        try {
            testTypeDao.deactivateTest(selected.getId());
            log("TEST_DEACTIVATED", "Deactivated test type " + selected.getName());
            handleClear();
            loadTests();
            messageLabel.setText("Test deactivated.");
        } catch (Exception ex) {
            messageLabel.setText("Unable to deactivate test: " + ex.getMessage());
        }
    }

    @FXML
    public void handleClear() {
        testNameField.clear();
        categoryField.clear();
        priceField.clear();
        tatField.clear();
        resultFormatCombo.setValue(null);
        testTable.getSelectionModel().clearSelection();
    }

    private void loadTests() {
        try {
            testTable.setItems(FXCollections.observableArrayList(testTypeDao.getAllTests()));
        } catch (Exception ex) {
            messageLabel.setText("Unable to load tests: " + ex.getMessage());
        }
    }

    private void populateForm(TestCatalogItem item) {
        if (item == null) {
            return;
        }
        testNameField.setText(item.getName());
        categoryField.setText(item.getCategory());
        priceField.setText(item.getPrice().toPlainString());
        tatField.setText(String.valueOf(item.getTatHours()));
        resultFormatCombo.setValue(item.getResultFormat());
    }

    private String name() {
        return testNameField.getText().trim();
    }

    private String category() {
        return categoryField.getText().trim();
    }

    private BigDecimal price() {
        return new BigDecimal(priceField.getText().trim());
    }

    private int tatHours() {
        return Integer.parseInt(tatField.getText().trim());
    }

    private String resultFormat() {
        return resultFormatCombo.getValue();
    }

    private void log(String action, String description) throws Exception {
        auditLogService.logAction(SessionManager.getCurrentUser() == null ? null : SessionManager.getCurrentUser().getId(), action, description);
    }
}
