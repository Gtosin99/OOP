package com.sante.lims.services;

import com.sante.lims.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogService {
    public void logAction(Integer userId, String action, String description) throws SQLException {
        String sql = """
                INSERT INTO audit_logs (user_id, action, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId == null || userId <= 0) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, userId);
            }
            statement.setString(2, action);
            statement.setString(3, description);
            statement.executeUpdate();
        }
    }
}
