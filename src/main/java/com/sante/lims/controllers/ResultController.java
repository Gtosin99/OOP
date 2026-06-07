package com.sante.lims.controllers;

import com.sante.lims.models.ResultRecord;
import com.sante.lims.models.User;
import com.sante.lims.services.ResultService;
import com.sante.lims.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ResultController {
    @FXML
    private TextField requestIdField;
    @FXML
    private ComboBox<String> resultTypeComboBox;
    @FXML
    private Label selectedFileLabel;
    @FXML
    private TableView<ResultRecord> resultTable;
    @FXML
    private TableColumn<ResultRecord, Integer> idColumn;
    @FXML
    private TableColumn<ResultRecord, String> filePathColumn;
    @FXML
    private TableColumn<ResultRecord, String> typeColumn;
    @FXML
    private TableColumn<ResultRecord, String> validationColumn;
    @FXML
    private TableColumn<ResultRecord, LocalDateTime> uploadedAtColumn;

    private final ResultService resultService = new ResultService();
    private File selectedFile;

    @FXML
    public void initialize() {
        resultTypeComboBox.setItems(FXCollections.observableArrayList("PDF", "IMAGE"));
        resultTypeComboBox.setValue("PDF");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        filePathColumn.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("resultType"));
        validationColumn.setCellValueFactory(new PropertyValueFactory<>("validationStatus"));
        uploadedAtColumn.setCellValueFactory(new PropertyValueFactory<>("uploadedAt"));
    }

    @FXML
    private void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose result file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF reports", "*.pdf"),
                new FileChooser.ExtensionFilter("Medical images", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        selectedFile = fileChooser.showOpenDialog(selectedFileLabel.getScene().getWindow());
        selectedFileLabel.setText(selectedFile == null ? "No file selected" : selectedFile.getName());
    }

    @FXML
    private void uploadResult() {
        Integer requestId = readRequestId();
        if (requestId == null) {
            return;
        }
        if (selectedFile == null) {
            showInfo("Choose a PDF report or medical image first.");
            return;
        }

        try {
            resultService.uploadResult(requestId, selectedFile.toPath(), resultTypeComboBox.getValue(), getCurrentUserId());
            selectedFile = null;
            selectedFileLabel.setText("No file selected");
            loadResults();
            showInfo("Result uploaded and set to pending validation.");
        } catch (SQLException | IOException exception) {
            showError("Unable to upload result.", exception);
        }
    }

    @FXML
    private void loadResults() {
        Integer requestId = readRequestId();
        if (requestId == null) {
            return;
        }

        try {
            resultTable.setItems(FXCollections.observableArrayList(
                    resultService.findResultsByRequestId(requestId)));
        } catch (SQLException exception) {
            showError("Unable to load results.", exception);
        }
    }

    @FXML
    private void validateSelectedResult() {
        ResultRecord selectedResult = resultTable.getSelectionModel().getSelectedItem();
        if (selectedResult == null) {
            showInfo("Select a result first.");
            return;
        }

        try {
            resultService.validateResult(selectedResult.getId(), getCurrentUserId());
            loadResults();
            showInfo("Result validated. It can now be shown to the customer.");
        } catch (SQLException exception) {
            showError("Unable to validate result.", exception);
        }
    }

    private Integer readRequestId() {
        try {
            return Integer.parseInt(requestIdField.getText().trim());
        } catch (NumberFormatException exception) {
            showInfo("Enter a valid test request ID.");
            return null;
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
