package com.mycompany.santelims.dao;

import com.mycompany.santelims.models.Sample;
import com.mycompany.santelims.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SampleDao {

    public List<Sample> getAllSamples() {
        List<Sample> samples = new ArrayList<>();
        String sql = "SELECT * FROM samples ORDER BY id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Sample s = new Sample();
                s.setId(rs.getInt("id"));
                s.setPatientName(rs.getString("patient_name"));
                s.setTestName(rs.getString("test_name"));
                s.setCollectionDate(rs.getDate("collection_date").toLocalDate());
                s.setStatus(rs.getString("status"));
                s.setResult(rs.getString("result"));
                samples.add(s);
            }
        } catch (Exception e) {
            System.out.println("=== SampleDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
        return samples;
    }

    public void addSample(Sample s) {
        String sql = "INSERT INTO samples(patient_name, test_name, collection_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, s.getPatientName());
            stmt.setString(2, s.getTestName());
            stmt.setDate(3, Date.valueOf(s.getCollectionDate()));
            stmt.setString(4, "Pending");
            stmt.executeUpdate();
            System.out.println("=== SampleDao: Added sample for: " + s.getPatientName());
        } catch (Exception e) {
            System.out.println("=== SampleDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateStatus(int id, String status) {
        String sql = "UPDATE samples SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println("=== SampleDao: Updated status for sample id: " + id);
        } catch (Exception e) {
            System.out.println("=== SampleDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateResult(int id, String result) {
        String sql = "UPDATE samples SET result = ?, status = 'Completed' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, result);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println("=== SampleDao: Result entered for sample id: " + id);
        } catch (Exception e) {
            System.out.println("=== SampleDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteSample(int id) {
        String sql = "DELETE FROM samples WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("=== SampleDao: Deleted sample id: " + id);
        } catch (Exception e) {
            System.out.println("=== SampleDao EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
