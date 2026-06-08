package com.sante.lims.controller;

import com.sante.lims.model.LabResult;
import com.sante.lims.service.ReportService;
import com.sante.lims.service.ResultService;
import com.sante.lims.util.SceneNavigator;
import com.sante.lims.util.SessionContext;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ResultVaultController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private TableView<LabResult> resultTable;
    @FXML
    private TableColumn<LabResult, String> colResultId;
    @FXML
    private TableColumn<LabResult, String> colRequestId;
    @FXML
    private TableColumn<LabResult, String> colTestName;
    @FXML
    private TableColumn<LabResult, String> colFileType;
    @FXML
    private TableColumn<LabResult, String> colPaymentStatus;
    @FXML
    private TableColumn<LabResult, String> colAccessStatus;
    @FXML
    private TableColumn<LabResult, String> colValidatedAt;
    @FXML
    private ImageView imagePreview;

    private final ResultService resultService = new ResultService();
    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        configureTable();
        loadResults();
        resultTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> showImagePreview(newValue));
    }

    private void configureTable() {
        colResultId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colRequestId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getRequestId())));
        colTestName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTestName()));
        colFileType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFileType()));
        colPaymentStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentStatus()));
        colAccessStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isPaid() ? "Available" : "Payment Required"));
        colValidatedAt.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getValidatedAt() == null ? "" : data.getValue().getValidatedAt().format(DATE_FORMAT)
        ));
    }

    private void loadResults() {
        long customerId = SessionContext.getCurrentCustomerId();
        try {
            List<LabResult> results = resultService.getValidatedResults(customerId);
            resultTable.setItems(FXCollections.observableArrayList(results));
        } catch (SQLException e) {
            showError("Unable to load result vault", e.getMessage());
        }
    }

    @FXML
    private void viewSelectedResult() {
        LabResult selected = resultTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a result", "Please select a result first.");
            return;
        }
        if (!canAccessResult(selected)) {
            return;
        }

        Path file = Path.of(selected.getFilePath());
        if (!Files.exists(file)) {
            showError("File not found", "Result file does not exist at: " + file);
            return;
        }

        try {
            Desktop.getDesktop().open(file.toFile());
        } catch (IOException e) {
            showError("Unable to open file", e.getMessage());
        }
    }

    @FXML
    private void downloadSelectedResult() {
        LabResult selected = resultTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Select a result", "Please select a result to download.");
            return;
        }
        if (!canAccessResult(selected)) {
            return;
        }

        Path source = Path.of(selected.getFilePath());
        if (!Files.exists(source)) {
            showError("File not found", "Cannot download missing file: " + source);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Result File");
        fileChooser.setInitialFileName(source.getFileName().toString());

        java.io.File target = fileChooser.showSaveDialog(resultTable.getScene().getWindow());
        if (target == null) {
            return;
        }

        try {
            Files.copy(source, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showInfo("Download complete", "File saved to: " + target.getAbsolutePath());
        } catch (IOException e) {
            showError("Download failed", e.getMessage());
        }
    }

    @FXML
    private void exportResultHistoryReport() {
        long customerId = SessionContext.getCurrentCustomerId();
        try {
            List<LabResult> results = resultService.getValidatedResults(customerId);
            Path report = reportService.exportResultHistoryCsv(results, customerId);
            showInfo("Report generated", "Result history saved to: " + report.toAbsolutePath());
        } catch (SQLException | IOException e) {
            showError("Unable to export report", e.getMessage());
        }
    }

    private boolean canAccessResult(LabResult result) {
        if (result.isPaid()) {
            return true;
        }
        showInfo(
                "Payment required",
                "This result has been uploaded and validated, but it cannot be opened or downloaded until payment is marked as PAID."
        );
        return false;
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
    private void openHistory() {
        SceneNavigator.switchScene("/fxml/request-history.fxml", "Request History");
    }

    private void showImagePreview(LabResult result) {
        imagePreview.setImage(null);
        if (result == null || result.getFilePath() == null) {
            return;
        }

        String lower = result.getFilePath().toLowerCase();
        if (!lower.endsWith(".png") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".gif") && !lower.endsWith(".bmp")) {
            return;
        }

        Path imagePath = Path.of(result.getFilePath());
        if (!Files.exists(imagePath)) {
            return;
        }

        imagePreview.setImage(new Image(imagePath.toUri().toString(), true));
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
