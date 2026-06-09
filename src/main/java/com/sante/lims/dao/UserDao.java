package com.sante.lims.dao;

import com.sante.lims.models.User;
import com.sante.lims.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public User findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, role, email_verified,
                       force_password_change, created_at
                FROM users
                WHERE LOWER(email) = LOWER(?)
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public List<User> getAllUsers() throws SQLException {
        String sql = """
                SELECT id, full_name, email, password_hash, role, email_verified,
                       force_password_change, created_at
                FROM users
                ORDER BY created_at DESC
                """;
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    public void addStaffCreatedUser(String fullName, String email, String passwordHash, String role) throws SQLException {
        String sql = """
                INSERT INTO users(full_name, email, password_hash, role, email_verified, force_password_change, created_at)
                VALUES (?, ?, ?, ?, TRUE, TRUE, NOW())
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            ps.executeUpdate();
        }
    }

    public void updatePassword(long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, force_password_change = FALSE WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public void deleteUser(long userId) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                executeUserCleanup(conn, "DELETE FROM email_verification_token WHERE user_id = ?", userId);
                executeUserCleanup(conn, "DELETE FROM notifications WHERE customer_id = ?", userId);
                executeUserCleanup(conn, """
                        DELETE FROM sample_status_history
                        WHERE sample_id IN (
                            SELECT s.id
                            FROM samples s
                            JOIN test_request tr ON tr.id = s.test_request_id
                            WHERE tr.customer_id = ?
                        )
                        """, userId);
                executeUserCleanup(conn, """
                        DELETE FROM samples
                        WHERE test_request_id IN (
                            SELECT id
                            FROM test_request
                            WHERE customer_id = ?
                        )
                        """, userId);
                executeUserCleanup(conn, """
                        DELETE FROM lab_result
                        WHERE request_id IN (
                            SELECT id
                            FROM test_request
                            WHERE customer_id = ?
                        )
                        """, userId);
                executeUserCleanup(conn, "DELETE FROM test_request WHERE customer_id = ?", userId);

                executeUserCleanup(conn, "UPDATE lab_result SET uploaded_by = NULL WHERE uploaded_by = ?", userId);
                executeUserCleanup(conn, "UPDATE lab_result SET validated_by = NULL WHERE validated_by = ?", userId);
                executeUserCleanup(conn, "UPDATE sample_status_history SET updated_by = NULL WHERE updated_by = ?", userId);
                executeUserCleanup(conn, "DELETE FROM audit_logs WHERE user_id = ?", userId);
                executeUserCleanup(conn, "DELETE FROM users WHERE id = ?", userId);

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private void executeUserCleanup(Connection conn, String sql, long userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setForcePasswordChange(rs.getBoolean("force_password_change"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        user.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return user;
    }
}
