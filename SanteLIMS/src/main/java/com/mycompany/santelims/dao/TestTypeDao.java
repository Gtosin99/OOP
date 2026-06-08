package com.mycompany.santelims.dao;

import com.mycompany.santelims.models.TestType;
import com.mycompany.santelims.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestTypeDao {

    public List<TestType> getAllTests() {
        List<TestType> tests = new ArrayList<>();
        String sql = "SELECT * FROM test_types ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                TestType t = new TestType();
                t.setId(rs.getInt("id"));
                t.setTestName(rs.getString("test_name"));
                t.setCategory(rs.getString("category"));
                t.setPrice(rs.getDouble("price"));
                t.setTurnaroundTime(rs.getString("turnaround_time"));
                t.setResultFormat(rs.getString("result_format"));
                tests.add(t);
            }
        } catch (Exception e) {
            System.out.println("=== TestTypeDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
        return tests;
    }

    public void addTest(TestType t) {
        String sql = "INSERT INTO test_types(test_name, category, price, turnaround_time, result_format) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getTestName());
            stmt.setString(2, t.getCategory());
            stmt.setDouble(3, t.getPrice());
            stmt.setString(4, t.getTurnaroundTime());
            stmt.setString(5, t.getResultFormat());
            stmt.executeUpdate();
            System.out.println("=== TestTypeDao: Added test: " + t.getTestName());
        } catch (Exception e) {
            System.out.println("=== TestTypeDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateTest(TestType t) {
        String sql = "UPDATE test_types SET test_name=?, category=?, price=?, turnaround_time=?, result_format=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, t.getTestName());
            stmt.setString(2, t.getCategory());
            stmt.setDouble(3, t.getPrice());
            stmt.setString(4, t.getTurnaroundTime());
            stmt.setString(5, t.getResultFormat());
            stmt.setInt(6, t.getId());
            stmt.executeUpdate();
            System.out.println("=== TestTypeDao: Updated test id: " + t.getId());
        } catch (Exception e) {
            System.out.println("=== TestTypeDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteTest(int id) {
        String sql = "DELETE FROM test_types WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("=== TestTypeDao: Deleted test id: " + id);
        } catch (Exception e) {
            System.out.println("=== TestTypeDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }
}