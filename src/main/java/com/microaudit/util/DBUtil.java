package com.microaudit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC Utility — raw connection provider for PreparedStatement-based queries.
 * Covers: JDBC + PreparedStatement practical
 */
public class DBUtil {

    private static final String URL = "jdbc:mysql://localhost:3306/micro_audit?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "G@ur@v@10";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    /**
     * Returns a fresh JDBC connection.
     * Always use try-with-resources to ensure closure.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
