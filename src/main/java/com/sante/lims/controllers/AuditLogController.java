package com.sante.lims.controllers;

import com.sante.lims.util.DatabaseUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AuditLogController {
    @FXML private TableView<AuditRow> auditTable;
    @FXML private TableColumn<AuditRow, String> colTime;
    @FXML private TableColumn<AuditRow, String> colUser;
    @FXML private TableColumn<AuditRow, String> colAction;
    @FXML private TableColumn<AuditRow, String> colDescription;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().createdAt()));
        colUser.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().userEmail()));
        colAction.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().action()));
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().description()));
        refreshLogs();
    }

    @FXML
    public void refreshLogs() {
        try {
            auditTable.setItems(FXCollections.observableArrayList(loadLogs()));
            messageLabel.setText("");
        } catch (Exception ex) {
            messageLabel.setText("Unable to load audit logs: " + ex.getMessage());
        }
    }

    private List<AuditRow> loadLogs() throws Exception {
        String sql = """
                SELECT al.created_at, COALESCE(u.email, 'System') AS user_email, al.action, al.description
                FROM audit_logs al
                LEFT JOIN users u ON al.user_id = u.id
                ORDER BY al.created_at DESC
                """;
        List<AuditRow> rows = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new AuditRow(
                        String.valueOf(rs.getTimestamp("created_at")),
                        rs.getString("user_email"),
                        rs.getString("action"),
                        rs.getString("description")
                ));
            }
        }
        return rows;
    }

    public record AuditRow(String createdAt, String userEmail, String action, String description) {
    }
}
