package com.zeraki.co.ke.lms.student;

import com.zeraki.co.ke.lms.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentManager {

    // Add a student and assign them a course
    public boolean addStudent(String name, int courseId, int institutionId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO students (name, course_id, institution_id) VALUES (?, ?, ?)")) {
            statement.setString(1, name);
            statement.setInt(2, courseId);
            statement.setInt(3, institutionId);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a student
    public boolean deleteStudent(int studentId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            statement.setInt(1, studentId);
            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Edit the name of a student
    public boolean editStudentName(int studentId, String newName) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE students SET name = ? WHERE id = ?")) {
            statement.setString(1, newName);
            statement.setInt(2, studentId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Change the course the student is doing within the same institution
    public boolean changeStudentCourse(int studentId, int newCourseId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE students SET course_id = ? WHERE id = ?")) {
            statement.setInt(1, newCourseId);
            statement.setInt(2, studentId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Transfer a student to another institution and assign them a course
    public boolean transferStudent(int studentId, int newInstitutionId, int newCourseId) {
        try (Connection connection = DatabaseManager.getConnection()) {
            // Check if the new course exists in the new institution
            if (isCourseInInstitution(newCourseId, newInstitutionId)) {
                // Transfer the student to the new institution and course
                return updateStudentInstitutionAndCourse(studentId, newInstitutionId, newCourseId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateStudentInstitutionAndCourse(int studentId, int newInstitutionId, int newCourseId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE students SET institution_id = ?, course_id = ? WHERE id = ?")) {
            statement.setInt(1, newInstitutionId);
            statement.setInt(2, newCourseId);
            statement.setInt(3, studentId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    // Check if a course exists in an institution
    private boolean isCourseInInstitution(int courseId, int institutionId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM courses WHERE id = ? AND institution_id = ?")) {
            statement.setInt(1, courseId);
            statement.setInt(2, institutionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    // Update the course of a student
    private boolean updateStudentCourse(int studentId, int newCourseId) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE students SET course_id = ? WHERE id = ?")) {
            statement.setInt(1, newCourseId);
            statement.setInt(2, studentId);
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public List<Student> listStudentsByInstitution(int institutionId, String searchQuery) {
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM students WHERE institution_id = ? AND name LIKE ?")) {
            statement.setInt(1, institutionId);
            statement.setString(2, "%" + searchQuery + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int courseId = resultSet.getInt("course_id");
                    // Assuming Student class has appropriate constructor
                    Student student = new Student(id, name, courseId, institutionId);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // List students by course
    public List<Student> listStudentsByCourse(int courseId, String searchQuery) {
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM students WHERE course_id = ? AND name LIKE ?")) {
            statement.setInt(1, courseId);
            statement.setString(2, "%" + searchQuery + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int institutionId = resultSet.getInt("institution_id");
                    // Assuming Student class has appropriate constructor
                    Student student = new Student(id, name, courseId, institutionId);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // Method to limit the number of students listed (e.g., show 10 students at a time)
    public List<Student> listStudentsPaged(int offset, int limit, int institutionId) {
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM students WHERE institution_id = ? LIMIT ? OFFSET ?")) {
            statement.setInt(1, institutionId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int courseId = resultSet.getInt("course_id");
                    // Assuming Student class has appropriate constructor
                    Student student = new Student(id, name, courseId, institutionId);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // Method to retrieve all students
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM students")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    int courseId = resultSet.getInt("course_id");
                    int institutionId = resultSet.getInt("institution_id");
                    // Assuming Student class has appropriate constructor
                    Student student = new Student(id, name, courseId, institutionId);
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }
}
