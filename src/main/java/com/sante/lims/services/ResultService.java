package com.sante.lims.services;

import com.sante.lims.config.AppConfig;
import com.sante.lims.database.DatabaseConnection;
import com.sante.lims.models.ResultRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ResultService {
    private final AuditLogService auditLogService = new AuditLogService();

    public ResultRecord uploadResult(int testRequestId, Path sourceFile, String resultType, int uploadedBy)
            throws SQLException, IOException {
        Path storedPath = storeResultFile(testRequestId, sourceFile, resultType);
        String sql = """
                INSERT INTO results (test_request_id, file_path, result_type, validation_status, uploaded_by)
                VALUES (?, ?, ?, 'PENDING_VALIDATION', ?)
                RETURNING id, test_request_id, file_path, result_type, validation_status,
                          uploaded_by, validated_by, uploaded_at, validated_at
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, testRequestId);
            statement.setString(2, storedPath.toString());
            statement.setString(3, resultType);
            statement.setInt(4, uploadedBy);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                ResultRecord result = mapResult(resultSet);
                auditLogService.logAction(uploadedBy, "RESULT_UPLOADED",
                        "Uploaded " + resultType + " result #" + result.getId()
                                + " for test request #" + testRequestId + ".");
                return result;
            }
        }
    }

    public void validateResult(int resultId, int validatedBy) throws SQLException {
        String sql = """
                UPDATE results
                SET validation_status = 'VALIDATED',
                    validated_by = ?,
                    validated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validatedBy);
            statement.setInt(2, resultId);
            statement.executeUpdate();
        }

        auditLogService.logAction(validatedBy, "RESULT_VALIDATED",
                "Validated result #" + resultId + ".");
    }

    public List<ResultRecord> findResultsByRequestId(int testRequestId) throws SQLException {
        List<ResultRecord> results = new ArrayList<>();
        String sql = """
                SELECT id, test_request_id, file_path, result_type, validation_status,
                       uploaded_by, validated_by, uploaded_at, validated_at
                FROM results
                WHERE test_request_id = ?
                ORDER BY uploaded_at DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, testRequestId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapResult(resultSet));
                }
            }
        }

        return results;
    }

    public List<ResultRecord> findValidatedResultsForCustomer(int customerId) throws SQLException {
        List<ResultRecord> results = new ArrayList<>();
        String sql = """
                SELECT r.id, r.test_request_id, r.file_path, r.result_type, r.validation_status,
                       r.uploaded_by, r.validated_by, r.uploaded_at, r.validated_at
                FROM results r
                JOIN test_requests tr ON r.test_request_id = tr.id
                WHERE tr.customer_id = ?
                  AND r.validation_status = 'VALIDATED'
                ORDER BY r.validated_at DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapResult(resultSet));
                }
            }
        }

        return results;
    }

    private Path storeResultFile(int testRequestId, Path sourceFile, String resultType) throws IOException {
        String uploadDirectory = "IMAGE".equalsIgnoreCase(resultType)
                ? AppConfig.IMAGE_UPLOAD_DIR
                : AppConfig.PDF_UPLOAD_DIR;
        Path targetDirectory = Path.of(uploadDirectory);
        Files.createDirectories(targetDirectory);

        String safeFileName = sourceFile.getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedFileName = "request-" + testRequestId + "-" + System.currentTimeMillis() + "-" + safeFileName;
        Path targetPath = targetDirectory.resolve(storedFileName);
        Files.copy(sourceFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }

    private ResultRecord mapResult(ResultSet resultSet) throws SQLException {
        ResultRecord result = new ResultRecord();
        result.setId(resultSet.getInt("id"));
        result.setTestRequestId(resultSet.getInt("test_request_id"));
        result.setFilePath(resultSet.getString("file_path"));
        result.setResultType(resultSet.getString("result_type"));
        result.setValidationStatus(resultSet.getString("validation_status"));
        result.setUploadedBy(resultSet.getInt("uploaded_by"));
        int validatedBy = resultSet.getInt("validated_by");
        result.setValidatedBy(resultSet.wasNull() ? null : validatedBy);
        result.setUploadedAt(toLocalDateTime(resultSet.getTimestamp("uploaded_at")));
        result.setValidatedAt(toLocalDateTime(resultSet.getTimestamp("validated_at")));
        return result;
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
