package com.mycompany.santelims.dao;

import com.mycompany.santelims.models.User;
import com.mycompany.santelims.models.Role;
import com.mycompany.santelims.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // FIND USER BY EMAIL
    public User findByEmail(String email) {
        String sql = "SELECT u.id, u.full_name, u.email, u.password_hash, u.role_id, " +
                     "u.must_change_password, u.is_verified, r.role_name " +
                     "FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.id " +
                     "WHERE LOWER(u.email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setMustChangePassword(rs.getBoolean("must_change_password"));
                user.setVerified(rs.getBoolean("is_verified"));

                Role role = new Role();
                role.setRoleName(rs.getString("role_name"));
                user.setRole(role);

                return user;
            }

        } catch (Exception e) {
            System.out.println("=== UserDao EXCEPTION in findByEmail: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // GET ALL USERS
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.full_name, u.email, u.password_hash, u.role_id, " +
                     "u.must_change_password, u.is_verified, r.role_name " +
                     "FROM users u " +
                     "LEFT JOIN roles r ON u.role_id = r.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setMustChangePassword(rs.getBoolean("must_change_password"));
                user.setVerified(rs.getBoolean("is_verified"));

                Role role = new Role();
                role.setRoleName(rs.getString("role_name"));
                user.setRole(role);

                users.add(user);
            }

            System.out.println("=== UserDao: Loaded " + users.size() + " users");

        } catch (Exception e) {
            System.out.println("=== UserDao EXCEPTION in getAllUsers: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    // ADD USER
    public void addUser(User user) {
        String getRoleIdSql = "SELECT id FROM roles WHERE LOWER(role_name) = LOWER(?)";
        String insertSql    = "INSERT INTO users(full_name, email, password_hash, role_id, " +
                              "must_change_password, is_verified) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement roleStmt = conn.prepareStatement(getRoleIdSql)) {

            roleStmt.setString(1, user.getRole().getRoleName());
            ResultSet rs = roleStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("=== UserDao: Role not found: " + user.getRole().getRoleName());
                return;
            }

            int roleId = rs.getInt("id");

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, user.getFullName());
                insertStmt.setString(2, user.getEmail());
                insertStmt.setString(3, user.getPasswordHash());
                insertStmt.setInt(4, roleId);
                insertStmt.setBoolean(5, user.isMustChangePassword());
                insertStmt.setBoolean(6, user.isVerified());
                insertStmt.executeUpdate();
                System.out.println("=== UserDao: Added user: " + user.getEmail());
            }

        } catch (Exception e) {
            System.out.println("=== UserDao EXCEPTION in addUser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // DELETE USER
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("=== UserDao: Deleted user id: " + id);
        } catch (Exception e) {
            System.out.println("=== UserDao EXCEPTION in deleteUser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // UPDATE PASSWORD
    public void updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash = ?, must_change_password = false WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("=== UserDao: Password updated for user id: " + userId);
        } catch (Exception e) {
            System.out.println("=== UserDao EXCEPTION in updatePassword: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // MARK USER VERIFIED
    public void markUserVerified(String email) {
        String sql = "UPDATE users SET is_verified = TRUE WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
            System.out.println("=== UserDao: User verified: " + email);
        } catch (Exception e) {
            System.out.println("=== UserDao EXCEPTION in markUserVerified: " + e.getMessage());
            e.printStackTrace();
        }
    }
}