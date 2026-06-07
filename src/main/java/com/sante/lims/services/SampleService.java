package com.sante.lims.services;

import com.sante.lims.database.DatabaseConnection;
import com.sante.lims.models.Sample;
import com.sante.lims.models.SampleStatusHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SampleService {
    private final AuditLogService auditLogService = new AuditLogService();

    public Optional<Sample> findSampleByRequestId(int testRequestId) throws SQLException {
        String sql = """
                SELECT id, test_request_id, current_status, created_at
                FROM samples
                WHERE test_request_id = ?
                ORDER BY created_at DESC
                LIMIT 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, testRequestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapSample(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Sample createSample(int testRequestId, int createdBy) throws SQLException {
        String sql = """
                INSERT INTO samples (test_request_id, current_status)
                VALUES (?, 'SAMPLE_COLLECTED')
                RETURNING id, test_request_id, current_status, created_at
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, testRequestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                Sample sample = mapSample(resultSet);
                insertHistory(connection, sample.getId(), "SAMPLE_COLLECTED", createdBy);
                auditLogService.logAction(createdBy, "SAMPLE_CREATED",
                        "Created sample #" + sample.getId() + " for test request #" + testRequestId + ".");
                return sample;
            }
        }
    }

    public void updateSampleStatus(int sampleId, String status, int updatedBy) throws SQLException {
        String updateSampleSql = """
                UPDATE samples
                SET current_status = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(updateSampleSql)) {
                statement.setString(1, status);
                statement.setInt(2, sampleId);
                statement.executeUpdate();
            }

            insertHistory(connection, sampleId, status, updatedBy);
            connection.commit();
        }

        auditLogService.logAction(updatedBy, "SAMPLE_STATUS_UPDATED",
                "Updated sample #" + sampleId + " to " + status + ".");
    }

    public List<SampleStatusHistory> findHistoryBySampleId(int sampleId) throws SQLException {
        List<SampleStatusHistory> history = new ArrayList<>();
        String sql = """
                SELECT ssh.id, ssh.sample_id, ssh.status, ssh.updated_by, u.full_name,
                       ssh.updated_at
                FROM sample_status_history ssh
                JOIN users u ON ssh.updated_by = u.id
                WHERE ssh.sample_id = ?
                ORDER BY ssh.updated_at DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sampleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SampleStatusHistory item = new SampleStatusHistory();
                    item.setId(resultSet.getInt("id"));
                    item.setSampleId(resultSet.getInt("sample_id"));
                    item.setStatus(resultSet.getString("status"));
                    item.setUpdatedBy(resultSet.getInt("updated_by"));
                    item.setUpdatedByName(resultSet.getString("full_name"));
                    item.setUpdatedAt(toLocalDateTime(resultSet.getTimestamp("updated_at")));
                    history.add(item);
                }
            }
        }

        return history;
    }

    private void insertHistory(Connection connection, int sampleId, String status, int updatedBy) throws SQLException {
        String sql = """
                INSERT INTO sample_status_history (sample_id, status, updated_by)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            statement.setInt(1, sampleId);
            statement.setString(2, status);
            statement.setInt(3, updatedBy);
            statement.executeUpdate();
        }
    }

    private Sample mapSample(ResultSet resultSet) throws SQLException {
        Sample sample = new Sample();
        sample.setId(resultSet.getInt("id"));
        sample.setTestRequestId(resultSet.getInt("test_request_id"));
        sample.setCurrentStatus(resultSet.getString("current_status"));
        sample.setCreatedAt(toLocalDateTime(resultSet.getTimestamp("created_at")));
        return sample;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
