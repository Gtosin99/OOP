package com.mycompany.santelims.controllers;

import com.mycompany.santelims.dao.SampleDao;
import com.mycompany.santelims.models.Sample;
import com.mycompany.santelims.models.User;
import com.mycompany.santelims.services.AuditService;
import com.mycompany.santelims.utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.List;

public class SampleController {

    @FXML private TextField patientNameField;
    @FXML private TextField testNameField;
    @FXML private DatePicker collectionDatePicker;
    @FXML private TextField resultField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label messageLabel;
    @FXML private TableView<Sample> sampleTable;
    @FXML private TableColumn<Sample, Integer> colId;
    @FXML private TableColumn<Sample, String> colPatient;
    @FXML private TableColumn<Sample, String> colTest;
    @FXML private TableColumn<Sample, String> colDate;
    @FXML private TableColumn<Sample, String> colStatus;
    @FXML private TableColumn<Sample, String> colResult;

    private final SampleDao sampleDao = new SampleDao();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colTest.setCellValueFactory(new PropertyValueFactory<>("testName"));
        colDate.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getCollectionDate() != null
                    ? data.getValue().getCollectionDate().toString()
                    : ""
            )
        );
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        statusCombo.setItems(FXCollections.observableArrayList(
            "Pending", "Processing", "Completed"
        ));

        // Auto-fill when row selected
        sampleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                patientNameField.setText(newVal.getPatientName());
                testNameField.setText(newVal.getTestName());
                collectionDatePicker.setValue(newVal.getCollectionDate());
                statusCombo.setValue(newVal.getStatus());
                resultField.setText(newVal.getResult() != null ? newVal.getResult() : "");
            }
        });

        loadSamples();
    }

    private void loadSamples() {
        List<Sample> samples = sampleDao.getAllSamples();
        sampleTable.setItems(FXCollections.observableArrayList(samples));
    }

    @FXML
    public void handleAddSample() {
        String patientName = patientNameField.getText().trim();
        String testName    = testNameField.getText().trim();
        LocalDate date     = collectionDatePicker.getValue();

        if (patientName.isEmpty() || testName.isEmpty() || date == null) {
            showMessage("Patient name, test name and date are required.", "red");
            return;
        }

        Sample s = new Sample();
        s.setPatientName(patientName);
        s.setTestName(testName);
        s.setCollectionDate(date);
        s.setStatus("Pending");

        sampleDao.addSample(s);

        String byEmail = Session.getCurrentUser().getEmail();
        AuditService.log(byEmail, "SAMPLE_CREATED",
            "Sample created for patient: " + patientName + " test: " + testName);

        showMessage("Sample added successfully.", "green");
        clearForm();
        loadSamples();
    }

    @FXML
    public void handleSaveResult() {
        // Only LAB_ATTENDANT and SUPER_ADMIN can enter results
        User currentUser = Session.getCurrentUser();
        String role = currentUser.getRole().getRoleName();

        if (!role.equals("LAB_ATTENDANT") && !role.equals("SUPER_ADMIN")) {
            showMessage("Only Lab Attendants can enter results.", "red");
            return;
        }

        Sample selected = sampleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a sample first.", "red");
            return;
        }

        String result = resultField.getText().trim();
        if (result.isEmpty()) {
            showMessage("Enter a result value.", "red");
            return;
        }

        sampleDao.updateResult(selected.getId(), result);
        AuditService.log(currentUser.getEmail(), "RESULT_ENTERED",
            "Result entered for sample id: " + selected.getId() +
            " patient: " + selected.getPatientName());

        showMessage("Result saved. Sample marked as Completed.", "green");
        clearForm();
        loadSamples();
    }

    @FXML
    public void handleUpdateStatus() {
        Sample selected = sampleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a sample first.", "red");
            return;
        }

        String status = statusCombo.getValue();
        if (status == null) {
            showMessage("Select a status.", "red");
            return;
        }

        sampleDao.updateStatus(selected.getId(), status);
        AuditService.log(Session.getCurrentUser().getEmail(), "STATUS_UPDATED",
            "Sample id: " + selected.getId() + " status changed to: " + status);

        showMessage("Status updated.", "green");
        loadSamples();
    }

    @FXML
    public void handleDeleteSample() {
        Sample selected = sampleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select a sample to delete.", "red");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete sample for " + selected.getPatientName() + "?");
        alert.setContentText("This action cannot be undone.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                AuditService.log(Session.getCurrentUser().getEmail(), "SAMPLE_DELETED",
                    "Deleted sample id: " + selected.getId());
                sampleDao.deleteSample(selected.getId());
                showMessage("Sample deleted.", "green");
                clearForm();
                loadSamples();
            }
        });
    }

    private void clearForm() {
        patientNameField.clear();
        testNameField.clear();
        collectionDatePicker.setValue(null);
        resultField.clear();
        statusCombo.setValue(null);
        sampleTable.getSelectionModel().clearSelection();
    }

    private void showMessage(String text, String color) {
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
        messageLabel.setText(text);
    }
}