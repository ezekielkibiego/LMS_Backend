package com.zeraki.co.ke.lms.db;

// db/DatabaseManager.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/lms";
    private static final String DB_USER = "kib";
    private static final String DB_PASSWORD = "Kibiego22";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

}

