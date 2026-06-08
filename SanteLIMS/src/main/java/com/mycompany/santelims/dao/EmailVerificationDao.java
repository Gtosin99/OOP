package com.mycompany.santelims.dao;

import com.mycompany.santelims.models.EmailVerification;
import com.mycompany.santelims.utils.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class EmailVerificationDao {

    public void createToken(String userEmail, String token) {
        String sql = "INSERT INTO email_verifications(user_email, token, expires_at, is_used) VALUES (?, ?, ?, FALSE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userEmail);
            stmt.setString(2, token);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusHours(24)));
            stmt.executeUpdate();
            System.out.println("=== EmailVerificationDao: Token created for: " + userEmail);
        } catch (Exception e) {
            System.out.println("=== EmailVerificationDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public EmailVerification findByToken(String token) {
        String sql = "SELECT * FROM email_verifications WHERE token = ? AND is_used = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                EmailVerification ev = new EmailVerification();
                ev.setId(rs.getInt("id"));
                ev.setUserEmail(rs.getString("user_email")); // ✅ new column
                ev.setToken(rs.getString("token"));
                ev.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                ev.setUsed(rs.getBoolean("is_used"));
                return ev;
            }
        } catch (Exception e) {
            System.out.println("=== EmailVerificationDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void markTokenUsed(String token) {
        String sql = "UPDATE email_verifications SET is_used = TRUE WHERE token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
            System.out.println("=== EmailVerificationDao: Token marked used: " + token);
        } catch (Exception e) {
            System.out.println("=== EmailVerificationDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }
}