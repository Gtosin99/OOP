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
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
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
