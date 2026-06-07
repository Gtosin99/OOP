package com.sante.lims.controllers;

import com.sante.lims.models.Sample;
import com.sante.lims.models.SampleStatusHistory;
import com.sante.lims.models.User;
import com.sante.lims.services.SampleService;
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

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class SampleController {
    @FXML
    private TextField requestIdField;
    @FXML
    private Label sampleIdLabel;
    @FXML
    private Label currentStatusLabel;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TableView<SampleStatusHistory> historyTable;
    @FXML
    private TableColumn<SampleStatusHistory, String> statusColumn;
    @FXML
    private TableColumn<SampleStatusHistory, String> updatedByColumn;
    @FXML
    private TableColumn<SampleStatusHistory, LocalDateTime> updatedAtColumn;

    private final SampleService sampleService = new SampleService();
    private Sample currentSample;

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(
                "SAMPLE_COLLECTED", "SAMPLE_PROCESSING", "SAMPLE_VALIDATED", "SAMPLE_COMPLETED"));
        statusComboBox.setValue("SAMPLE_COLLECTED");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        updatedByColumn.setCellValueFactory(new PropertyValueFactory<>("updatedByName"));
        updatedAtColumn.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
    }

    @FXML
    private void loadSample() {
        Integer requestId = readRequestId();
        if (requestId == null) {
            return;
        }

        try {
            Optional<Sample> sample = sampleService.findSampleByRequestId(requestId);
            if (sample.isEmpty()) {
                currentSample = null;
                sampleIdLabel.setText("No sample created");
                currentStatusLabel.setText("-");
                historyTable.getItems().clear();
                showInfo("No sample exists for this request yet.");
                return;
            }

            currentSample = sample.get();
            renderCurrentSample();
        } catch (SQLException exception) {
            showError("Unable to load sample.", exception);
        }
    }

    @FXML
    private void createSample() {
        Integer requestId = readRequestId();
        if (requestId == null) {
            return;
        }

        try {
            currentSample = sampleService.createSample(requestId, getCurrentUserId());
            renderCurrentSample();
            showInfo("Sample created and marked as collected.");
        } catch (SQLException exception) {
            showError("Unable to create sample.", exception);
        }
    }

    @FXML
    private void updateStatus() {
        if (currentSample == null) {
            showInfo("Load or create a sample first.");
            return;
        }

        try {
            sampleService.updateSampleStatus(currentSample.getId(), statusComboBox.getValue(), getCurrentUserId());
            loadSample();
            showInfo("Sample status updated.");
        } catch (SQLException exception) {
            showError("Unable to update sample status.", exception);
        }
    }

    private void renderCurrentSample() throws SQLException {
        sampleIdLabel.setText(String.valueOf(currentSample.getId()));
        currentStatusLabel.setText(currentSample.getCurrentStatus());
        statusComboBox.setValue(currentSample.getCurrentStatus());
        historyTable.setItems(FXCollections.observableArrayList(
                sampleService.findHistoryBySampleId(currentSample.getId())));
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
