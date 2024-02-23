package com.zeraki.co.ke.lms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/lms";
    private static final String DB_USER = "kib";
    private static final String DB_PASSWORD = "Kibiego22";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            // Create Institutions table
            String createInstitutionsTableQuery = "CREATE TABLE IF NOT EXISTS institutions ("
                    + "id SERIAL PRIMARY KEY,"
                    + "name VARCHAR(255) UNIQUE NOT NULL)";
            statement.executeUpdate(createInstitutionsTableQuery);

            // Create Courses table
            String createCoursesTableQuery = "CREATE TABLE IF NOT EXISTS courses ("
                    + "id SERIAL PRIMARY KEY,"
                    + "name VARCHAR(255) NOT NULL,"
                    + "institution_id INT,"
                    + "FOREIGN KEY (institution_id) REFERENCES institutions(id))";
            statement.executeUpdate(createCoursesTableQuery);

            // Create Students table
            String createStudentsTableQuery = "CREATE TABLE IF NOT EXISTS students ("
                    + "id SERIAL PRIMARY KEY,"
                    + "name VARCHAR(255) NOT NULL,"
                    + "course_id INT,"
                    + "FOREIGN KEY (course_id) REFERENCES courses(id))";
            statement.executeUpdate(createStudentsTableQuery);

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }
}
