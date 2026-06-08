package com.sante.lims.dao;

import com.sante.lims.model.TestCatalogItem;
import com.sante.lims.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TestTypeDao {

    public List<TestCatalogItem> getAllTests() throws SQLException {
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

    public void addTest(String name, String category, BigDecimal price, int tatHours, String resultFormat) throws SQLException {
        String sql = """
                INSERT INTO test_catalog(name, category, price, tat_hours, result_format, active)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setBigDecimal(3, price);
            ps.setInt(4, tatHours);
            ps.setString(5, resultFormat);
            ps.executeUpdate();
        }
    }

    public void updateTest(long id, String name, String category, BigDecimal price, int tatHours, String resultFormat) throws SQLException {
        String sql = """
                UPDATE test_catalog
                SET name = ?, category = ?, price = ?, tat_hours = ?, result_format = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, category);
            ps.setBigDecimal(3, price);
            ps.setInt(4, tatHours);
            ps.setString(5, resultFormat);
            ps.setLong(6, id);
            ps.executeUpdate();
        }
    }

    public void deactivateTest(long id) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE test_catalog SET active = FALSE WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
