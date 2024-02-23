package com.zeraki.co.ke.lms.institution;

import com.zeraki.co.ke.lms.db.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstitutionManager {
    // Load institutions from the database into memory
    private List<Institution> institutions;

    public InstitutionManager() {
        loadInstitutionsFromDatabase();
    }

    // Load institutions from the database
    private void loadInstitutionsFromDatabase() {
        institutions = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id, name FROM institutions")) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                institutions.add(new Institution(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add a new institution
    public Institution addInstitution(String name) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO institutions (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    Institution newInstitution = new Institution(id, name);
                    institutions.add(newInstitution);
                    return newInstitution;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all institutions
    public List<Institution> getAllInstitutions() {
        return new ArrayList<>(institutions);
    }

    // Update the name of an institution
    public boolean updateInstitutionName(int id, String newName) {
        for (Institution institution : institutions) {
            if (institution.getId() == id) {
                institution.setName(newName);
                try (Connection connection = DatabaseManager.getConnection();
                     PreparedStatement statement = connection.prepareStatement("UPDATE institutions SET name = ? WHERE id = ?")) {
                    statement.setString(1, newName);
                    statement.setInt(2, id);
                    int rowsUpdated = statement.executeUpdate();
                    return rowsUpdated > 0;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // Delete an institution
    public boolean deleteInstitution(int institutionId) {
        try (Connection connection = DatabaseManager.getConnection()) {
            // Check if the institution has associated courses
            if (hasAssociatedCourses(connection, institutionId)) {
                return false; // Institution has associated courses, cannot delete
            }

            // Check if the institution has associated students
            if (hasAssociatedStudents(connection, institutionId)) {
                return false; // Institution has associated students, cannot delete
            }

            // If no associated courses or students, proceed with deletion
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM institutions WHERE id = ?")) {
                statement.setInt(1, institutionId);
                int rowsDeleted = statement.executeUpdate();
                return rowsDeleted > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean hasAssociatedCourses(Connection connection, int institutionId) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM courses WHERE institution_id = ?")) {
            statement.setInt(1, institutionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean hasAssociatedStudents(Connection connection, int institutionId) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM students WHERE institution_id = ?")) {
            statement.setInt(1, institutionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
