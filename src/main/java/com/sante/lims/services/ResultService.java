package com.sante.lims.services;

import com.sante.lims.config.AppConfig;
import com.sante.lims.database.DatabaseConnection;
import com.sante.lims.models.ResultRecord;
import com.sante.lims.service.EmailService;

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
    private final EmailService emailService = new EmailService();

    public ResultRecord uploadResult(int testRequestId, Path sourceFile, String resultType, int uploadedBy)
            throws SQLException, IOException {
        Path storedPath = storeResultFile(testRequestId, sourceFile, resultType);
        String sql = """
                INSERT INTO lab_result (request_id, file_path, file_type, validated, uploaded_by)
                VALUES (?, ?, ?, FALSE, ?)
                RETURNING id, request_id AS test_request_id, file_path, file_type AS result_type,
                          CASE WHEN validated THEN 'VALIDATED' ELSE 'PENDING_VALIDATION' END AS validation_status,
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
                UPDATE lab_result
                SET validated = TRUE,
                    validated_by = ?,
                    validated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validatedBy);
            statement.setInt(2, resultId);
            statement.executeUpdate();
            markRequestValidatedAndNotify(connection, resultId);
        }

        auditLogService.logAction(validatedBy, "RESULT_VALIDATED",
                "Validated result #" + resultId + ".");
    }

    private void markRequestValidatedAndNotify(Connection connection, int resultId) throws SQLException {
        String findSql = """
                SELECT tr.id AS request_id, tr.customer_id, u.email, tc.name AS test_name
                FROM lab_result lr
                JOIN test_request tr ON lr.request_id = tr.id
                JOIN users u ON tr.customer_id = u.id
                JOIN test_catalog tc ON tr.test_id = tc.id
                WHERE lr.id = ?
                """;
        Long customerId = null;
        String email = null;
        String testName = null;
        long requestId = 0L;
        try (PreparedStatement statement = connection.prepareStatement(findSql)) {
            statement.setInt(1, resultId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    requestId = resultSet.getLong("request_id");
                    customerId = resultSet.getLong("customer_id");
                    email = resultSet.getString("email");
                    testName = resultSet.getString("test_name");
                }
            }
        }
        if (customerId == null) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE test_request SET processing_status = 'VALIDATED' WHERE id = ?")) {
            statement.setLong(1, requestId);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO notifications(customer_id, subject, message, read_flag, created_at) VALUES (?, ?, ?, FALSE, NOW())")) {
            statement.setLong(1, customerId);
            statement.setString(2, "Result Ready");
            statement.setString(3, "Your validated result for " + testName + " is ready.");
            statement.executeUpdate();
        }
        try {
            emailService.sendResultReady(email, testName);
        } catch (Exception ignored) {
            // SMTP configuration may be unavailable in local/offline demos; the in-app notification is still recorded.
        }
    }

    public List<ResultRecord> findResultsByRequestId(int testRequestId) throws SQLException {
        List<ResultRecord> results = new ArrayList<>();
        String sql = """
                SELECT id, request_id AS test_request_id, file_path, file_type AS result_type,
                       CASE WHEN validated THEN 'VALIDATED' ELSE 'PENDING_VALIDATION' END AS validation_status,
                       uploaded_by, validated_by, uploaded_at, validated_at
                FROM lab_result
                WHERE request_id = ?
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
                SELECT r.id, r.request_id AS test_request_id, r.file_path, r.file_type AS result_type,
                       CASE WHEN r.validated THEN 'VALIDATED' ELSE 'PENDING_VALIDATION' END AS validation_status,
                       r.uploaded_by, r.validated_by, r.uploaded_at, r.validated_at
                FROM lab_result r
                JOIN test_request tr ON r.request_id = tr.id
                WHERE tr.customer_id = ?
                  AND r.validated = TRUE
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
