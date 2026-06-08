package com.mycompany.santelims.dao;

import com.mycompany.santelims.models.AuditLog;
import com.mycompany.santelims.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDao {

    public void log(String userEmail, String action, String details) {
        String sql = "INSERT INTO audit_logs(user_id, action, description) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userEmail);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.executeUpdate();
            System.out.println("=== AUDIT: [" + action + "] " + userEmail + " — " + details);
        } catch (Exception e) {
            System.out.println("=== AuditLogDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<AuditLog> getAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getInt("id"));
                log.setUserEmail(rs.getString("user_id"));       // ✅ mapped
                log.setAction(rs.getString("action"));
                log.setDetails(rs.getString("description"));     // ✅ mapped
                log.setCreatedAt(rs.getTimestamp("timestamp").toLocalDateTime()); // ✅ mapped
                logs.add(log);
            }
        } catch (Exception e) {
            System.out.println("=== AuditLogDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }
}