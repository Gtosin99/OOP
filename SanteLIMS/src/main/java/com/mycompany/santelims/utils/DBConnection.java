package com.mycompany.santelims.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/lims_db";
    private static final String USER = "postgres";

    // ⚠️ IMPORTANT: replace with your real postgres password
    private static final String PASSWORD = "void";

    private static Connection connection;

    public static Connection getConnection() {

        try {
            if (connection == null || connection.isClosed()) {

                Class.forName("org.postgresql.Driver");

                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                System.out.println("✔ DATABASE CONNECTED SUCCESSFULLY");
                System.out.println("URL: " + URL);
            }

        } catch (ClassNotFoundException e) {
            System.out.println("❌ PostgreSQL Driver not found");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println("❌ DATABASE CONNECTION FAILED");
            System.out.println("Check username/password/database name");
            e.printStackTrace();
        }

        return connection;
    }
}