package com.sante.lims.services;

import com.sante.lims.database.DatabaseConnection;
import com.sante.lims.models.TestRequestQueueItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LabRequestService {
    private final AuditLogService auditLogService = new AuditLogService();

    public List<TestRequestQueueItem> findRequests(String searchTerm, String paymentStatus, String requestStatus)
            throws SQLException {
        List<TestRequestQueueItem> requests = new ArrayList<>();
        String sql = """
                SELECT tr.id, tr.customer_id, u.full_name, u.email, tt.name AS test_name,
                       tt.category, tt.price, tr.payment_status, tr.request_date,
                       tr.expected_completion, tr.processing_status AS status
                FROM test_request tr
                JOIN users u ON tr.customer_id = u.id
                JOIN test_catalog tt ON tr.test_id = tt.id
                WHERE (? IS NULL OR LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?)
                       OR LOWER(tt.name) LIKE LOWER(?))
                  AND (? IS NULL OR tr.payment_status = ?)
                  AND (? IS NULL OR tr.processing_status = ?)
                ORDER BY tr.request_date DESC
                """;

        String likeSearch = isBlank(searchTerm) ? null : "%" + searchTerm.trim() + "%";
        String paymentFilter = normalizeFilter(paymentStatus);
        String statusFilter = normalizeFilter(requestStatus);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, likeSearch);
            statement.setString(2, likeSearch);
            statement.setString(3, likeSearch);
            statement.setString(4, likeSearch);
            statement.setString(5, paymentFilter);
            statement.setString(6, paymentFilter);
            statement.setString(7, statusFilter);
            statement.setString(8, statusFilter);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        }

        return requests;
    }

    public void markRequestAsPaid(int requestId, int verifiedBy) throws SQLException {
        String sql = """
                UPDATE test_request
                SET payment_status = 'PAID'
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.executeUpdate();
        }

        auditLogService.logAction(verifiedBy, "PAYMENT_VERIFIED",
                "Marked test request #" + requestId + " as paid.");
    }

    public void updateRequestStatus(int requestId, String status, int updatedBy) throws SQLException {
        String sql = """
                UPDATE test_request
                SET processing_status = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, requestId);
            statement.executeUpdate();
        }

        auditLogService.logAction(updatedBy, "REQUEST_STATUS_UPDATED",
                "Updated test request #" + requestId + " to " + status + ".");
    }

    private TestRequestQueueItem mapRequest(ResultSet resultSet) throws SQLException {
        TestRequestQueueItem item = new TestRequestQueueItem();
        item.setId(resultSet.getInt("id"));
        item.setCustomerId(resultSet.getInt("customer_id"));
        item.setCustomerName(resultSet.getString("full_name"));
        item.setCustomerEmail(resultSet.getString("email"));
        item.setTestName(resultSet.getString("test_name"));
        item.setTestCategory(resultSet.getString("category"));
        item.setPrice(resultSet.getBigDecimal("price"));
        item.setPaymentStatus(resultSet.getString("payment_status"));
        item.setRequestDate(toLocalDateTime(resultSet.getTimestamp("request_date")));
        item.setExpectedCompletion(toLocalDateTime(resultSet.getTimestamp("expected_completion")));
        item.setStatus(resultSet.getString("status"));
        return item;
    }

    private String normalizeFilter(String value) {
        if (isBlank(value) || "ALL".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
