package com.sante.lims.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    private static final String CONFIG_FILE = "/application.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties properties = loadProperties();

        String url = getConfigValue("DB_URL", properties.getProperty("db.url"));
        String username = getConfigValue("DB_USERNAME", getProperty(properties, "db.username", "db.user"));
        String password = getConfigValue("DB_PASSWORD", properties.getProperty("db.password"));

        return DriverManager.getConnection(url, username, password);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream = DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load database configuration.", exception);
        }

        return properties;
    }

    private static String getConfigValue(String environmentKey, String defaultValue) {
        String environmentValue = System.getenv(environmentKey);
        return environmentValue == null || environmentValue.isBlank() ? defaultValue : environmentValue;
    }

    private static String getProperty(Properties properties, String... keys) {
        for (String key : keys) {
            String value = properties.getProperty(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
