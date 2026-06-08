package com.mycompany.santelims.controllers;

import com.mycompany.santelims.dao.AuditLogDao;
import com.mycompany.santelims.models.AuditLog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class AuditLogController {

    @FXML private TableView<AuditLog> logTable;
    @FXML private TableColumn<AuditLog, Integer> colId;
    @FXML private TableColumn<AuditLog, String> colEmail;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colDetails;
    @FXML private TableColumn<AuditLog, String> colTime;

    private final AuditLogDao auditLogDao = new AuditLogDao();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
        colTime.setCellValueFactory(data ->
            new SimpleStringProperty(
                data.getValue().getCreatedAt() != null
                    ? data.getValue().getCreatedAt().toString().replace("T", " ")
                    : ""
            )
        );

        loadLogs();
    }

    private void loadLogs() {
        List<AuditLog> logs = auditLogDao.getAllLogs();
        logTable.setItems(FXCollections.observableArrayList(logs));
        System.out.println("=== AuditLog: Loaded " + logs.size() + " entries");
    }
}