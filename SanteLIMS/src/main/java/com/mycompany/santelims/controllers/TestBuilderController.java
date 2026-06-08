package com.mycompany.santelims.controllers;

import com.mycompany.santelims.dao.TestTypeDao;
import com.mycompany.santelims.models.TestType;
import com.mycompany.santelims.services.AuditService;
import com.mycompany.santelims.utils.Session;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class TestBuilderController {

    @FXML private TextField testNameField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextField tatField;
    @FXML private ComboBox<String> resultFormatCombo;
    @FXML private Label messageLabel;
    @FXML private TableView<TestType> testTable;
    @FXML private TableColumn<TestType, Integer> colId;
    @FXML private TableColumn<TestType, String> colName;
    @FXML private TableColumn<TestType, String> colCategory;
    @FXML private TableColumn<TestType, Double> colPrice;
    @FXML private TableColumn<TestType, String> colTat;
    @FXML private TableColumn<TestType, String> colFormat;

    private final TestTypeDao testTypeDao = new TestTypeDao();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(data ->
            new SimpleDoubleProperty(data.getValue().getPrice()).asObject()
        );
        colTat.setCellValueFactory(new PropertyValueFactory<>("turnaroundTime"));
        colFormat.setCellValueFactory(new PropertyValueFactory<>("resultFormat"));

        resultFormatCombo.setItems(FXCollections.observableArrayList(
            "Numeric", "Text", "PDF", "Image"
        ));

        // Auto-fill form when row is selected
        testTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                testNameField.setText(newVal.getTestName());
                categoryField.setText(newVal.getCategory());
                priceField.setText(String.valueOf(newVal.getPrice()));
                tatField.setText(newVal.getTurnaroundTime());
                resultFormatCombo.setValue(newVal.getResultFormat());
            }
        });

        loadTests();
    }

    private void loadTests() {
        List<TestType> tests = testTypeDao.getAllTests();
        testTable.setItems(FXCollections.observableArrayList(tests));
    }

    @FXML
    public void handleAdd() {
        String name   = testNameField.getText().trim();
        String cat    = categoryField.getText().trim();
        String priceS = priceField.getText().trim();
        String tat    = tatField.getText().trim();
        String format = resultFormatCombo.getValue();

        if (name.isEmpty() || cat.isEmpty() || priceS.isEmpty() || tat.isEmpty() || format == null) {
            showMessage("All fields are required.", "red");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceS);
        } catch (NumberFormatException e) {
            showMessage("Price must be a number.", "red");
            return;
        }

        TestType t = new TestType();
        t.setTestName(name);
        t.setCategory(cat);
        t.setPrice(price);
        t.setTurnaroundTime(tat);
        t.setResultFormat(format);

        testTypeDao.addTest(t);
        AuditService.logTestCreated(Session.getCurrentUser().getEmail(), name);
        showMessage("Test added successfully.", "green");
        handleClear();
        loadTests();
    }

    @FXML
    public void handleUpdate() {
        TestType selected = testTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a test to update.", "red");
            return;
        }

        String name   = testNameField.getText().trim();
        String cat    = categoryField.getText().trim();
        String priceS = priceField.getText().trim();
        String tat    = tatField.getText().trim();
        String format = resultFormatCombo.getValue();

        if (name.isEmpty() || cat.isEmpty() || priceS.isEmpty() || tat.isEmpty() || format == null) {
            showMessage("All fields are required.", "red");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceS);
        } catch (NumberFormatException e) {
            showMessage("Price must be a number.", "red");
            return;
        }

        selected.setTestName(name);
        selected.setCategory(cat);
        selected.setPrice(price);
        selected.setTurnaroundTime(tat);
        selected.setResultFormat(format);

        testTypeDao.updateTest(selected);
        AuditService.logTestUpdated(Session.getCurrentUser().getEmail(), name);
        showMessage("Test updated successfully.", "green");
        handleClear();
        loadTests();
    }

    @FXML
    public void handleDelete() {
        TestType selected = testTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a test to delete.", "red");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + selected.getTestName() + "?");
        alert.setContentText("This action cannot be undone.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AuditService.logTestDeleted(Session.getCurrentUser().getEmail(), selected.getTestName());
                testTypeDao.deleteTest(selected.getId());
                showMessage("Test deleted.", "green");
                handleClear();
                loadTests();
            }
        });
    }

    @FXML
    public void handleClear() {
        testNameField.clear();
        categoryField.clear();
        priceField.clear();
        tatField.clear();
        resultFormatCombo.setValue(null);
        testTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    private void showMessage(String text, String color) {
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
        messageLabel.setText(text);
    }
}