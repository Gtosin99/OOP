package com.mycompany.santelims.dao;

import com.mycompany.santelims.models.Role;
import com.mycompany.santelims.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDao {

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT id, role_name FROM roles";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Role role = new Role();
                role.setId(rs.getInt("id"));
                role.setRoleName(rs.getString("role_name"));
                roles.add(role);
            }
            System.out.println("=== RoleDao: Loaded " + roles.size() + " roles");

        } catch (Exception e) {
            System.out.println("=== RoleDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }
}