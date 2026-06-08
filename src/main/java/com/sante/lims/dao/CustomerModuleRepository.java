package com.sante.lims.dao;

import com.sante.lims.model.CustomerProfile;
import com.sante.lims.model.LabResult;
import com.sante.lims.model.NotificationItem;
import com.sante.lims.model.TestCatalogItem;
import com.sante.lims.model.TestRequest;
import com.sante.lims.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerModuleRepository {

    public CustomerProfile getCustomerProfile(long customerId) throws SQLException {
        String sql = """
                SELECT id, full_name, email, email_verified
                FROM users
                WHERE id = ? AND role = 'CUSTOMER'
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerProfile(
                            rs.getLong("id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getBoolean("email_verified")
                    );
                }
            }
        }
        return null;
    }

    public List<TestCatalogItem> getAvailableTests() throws SQLException {
        String sql = """
                SELECT id, name, category, price, tat_hours, result_format
                FROM test_catalog
                WHERE active = TRUE
                ORDER BY name
                """;

        List<TestCatalogItem> tests = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tests.add(new TestCatalogItem(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("price"),
                        rs.getInt("tat_hours"),
                        rs.getString("result_format")
                ));
            }
        }
        return tests;
    }

    public long createTestRequest(long customerId, long testId) throws SQLException {
        String sql = """
                INSERT INTO test_request
                    (customer_id, test_id, request_date, expected_completion, payment_status, sample_status, processing_status)
                VALUES
                    (?, ?, NOW(), NOW() + (SELECT (tat_hours || ' hours')::interval FROM test_catalog WHERE id = ?),
                     'UNPAID', 'COLLECTION_PENDING', 'PENDING')
                RETURNING id
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, testId);
            ps.setLong(3, testId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        throw new SQLException("Unable to create test request");
    }

    public List<TestRequest> getActiveRequests(long customerId) throws SQLException {
        String sql = """
                SELECT tr.id, tc.name AS test_name, tr.request_date, tr.expected_completion,
                       tr.payment_status, tr.sample_status, tr.processing_status
                FROM test_request tr
                JOIN test_catalog tc ON tr.test_id = tc.id
                WHERE tr.customer_id = ?
                  AND tr.processing_status NOT IN ('VALIDATED', 'DELIVERED')
                ORDER BY tr.request_date DESC
                """;

        return fetchRequests(customerId, sql);
    }

    public List<TestRequest> getRequestHistory(long customerId) throws SQLException {
        String sql = """
                SELECT tr.id, tc.name AS test_name, tr.request_date, tr.expected_completion,
                       tr.payment_status, tr.sample_status, tr.processing_status
                FROM test_request tr
                JOIN test_catalog tc ON tr.test_id = tc.id
                WHERE tr.customer_id = ?
                ORDER BY tr.request_date DESC
                """;

        return fetchRequests(customerId, sql);
    }

    private List<TestRequest> fetchRequests(long customerId, String sql) throws SQLException {
        List<TestRequest> requests = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(new TestRequest(
                            rs.getLong("id"),
                            rs.getString("test_name"),
                            toLocalDateTime(rs.getTimestamp("request_date")),
                            toLocalDateTime(rs.getTimestamp("expected_completion")),
                            rs.getString("payment_status"),
                            rs.getString("sample_status"),
                            rs.getString("processing_status")
                    ));
                }
            }
        }
        return requests;
    }

    public List<LabResult> getValidatedResults(long customerId) throws SQLException {
        String sql = """
                SELECT lr.id, lr.request_id, tc.name AS test_name, lr.file_path, lr.file_type,
                       lr.validated, lr.validated_at
                FROM lab_result lr
                JOIN test_request tr ON lr.request_id = tr.id
                JOIN test_catalog tc ON tr.test_id = tc.id
                WHERE tr.customer_id = ?
                  AND lr.validated = TRUE
                ORDER BY lr.validated_at DESC
                """;

        List<LabResult> results = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new LabResult(
                            rs.getLong("id"),
                            rs.getLong("request_id"),
                            rs.getString("test_name"),
                            rs.getString("file_path"),
                            rs.getString("file_type"),
                            rs.getBoolean("validated"),
                            toLocalDateTime(rs.getTimestamp("validated_at"))
                    ));
                }
            }
        }
        return results;
    }

    public List<NotificationItem> getNotifications(long customerId) throws SQLException {
        String sql = """
                SELECT id, subject, message, created_at, read_flag
                FROM notifications
                WHERE customer_id = ?
                ORDER BY created_at DESC
                """;

        List<NotificationItem> notifications = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new NotificationItem(
                            rs.getLong("id"),
                            rs.getString("subject"),
                            rs.getString("message"),
                            toLocalDateTime(rs.getTimestamp("created_at")),
                            rs.getBoolean("read_flag")
                    ));
                }
            }
        }
        return notifications;
    }

    public void createNotification(long customerId, String subject, String message) throws SQLException {
        String sql = """
                INSERT INTO notifications(customer_id, subject, message, read_flag, created_at)
                VALUES (?, ?, ?, FALSE, NOW())
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setString(2, subject);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }

    public String getCustomerEmail(long customerId) throws SQLException {
        String sql = "SELECT email FROM users WHERE id = ? AND role = 'CUSTOMER'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }

    public long createCustomerSelfRegistration(String fullName, String email, String passwordHash, String token) throws SQLException {
        String createUser = """
                INSERT INTO users(full_name, email, password_hash, role, email_verified, force_password_change, created_at)
                VALUES (?, ?, ?, 'CUSTOMER', FALSE, FALSE, NOW())
                RETURNING id
                """;
        String createToken = """
                INSERT INTO email_verification_token(user_id, token, expires_at, used)
                VALUES (?, ?, NOW() + INTERVAL '24 hours', FALSE)
                """;

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long userId;
                try (PreparedStatement ps = conn.prepareStatement(createUser)) {
                    ps.setString(1, fullName);
                    ps.setString(2, email);
                    ps.setString(3, passwordHash);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Failed to create customer account");
                        }
                        userId = rs.getLong("id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(createToken)) {
                    ps.setLong(1, userId);
                    ps.setString(2, token);
                    ps.executeUpdate();
                }

                conn.commit();
                return userId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean verifyEmailToken(String token) throws SQLException {
        String findToken = """
                SELECT user_id
                FROM email_verification_token
                WHERE token = ? AND used = FALSE AND expires_at > NOW()
                """;

        String markUserVerified = "UPDATE users SET email_verified = TRUE WHERE id = ?";
        String markTokenUsed = "UPDATE email_verification_token SET used = TRUE WHERE token = ?";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long userId = null;
                try (PreparedStatement ps = conn.prepareStatement(findToken)) {
                    ps.setString(1, token);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getLong("user_id");
                        }
                    }
                }

                if (userId == null) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement(markUserVerified)) {
                    ps.setLong(1, userId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(markTokenUsed)) {
                    ps.setString(1, token);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
