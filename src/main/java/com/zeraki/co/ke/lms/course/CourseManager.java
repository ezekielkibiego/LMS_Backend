package com.zeraki.co.ke.lms.course;

import com.zeraki.co.ke.lms.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseManager {
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM courses");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int courseId = resultSet.getInt("id");
                String courseName = resultSet.getString("name");
                int institutionId = resultSet.getInt("institution_id");
                Course course = new Course(courseId, courseName, institutionId);
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public List<Course> getCoursesByInstitutionId(int institutionId) {
        List<Course> courses = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM courses WHERE institution_id = ?");
        ) {
            statement.setInt(1, institutionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int courseId = resultSet.getInt("id");
                    String courseName = resultSet.getString("name");
                    Course course = new Course(courseId, courseName, institutionId);
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public Course getCourseById(int courseId) {
        Course course = null;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM courses WHERE id = ?");
        ) {
            statement.setInt(1, courseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String courseName = resultSet.getString("name");
                    int institutionId = resultSet.getInt("institution_id");
                    course = new Course(courseId, courseName, institutionId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }

    private Course getCourseByName(String name) {
        Course course = null;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM courses WHERE name = ?");
        ) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int courseId = resultSet.getInt("id");
                    int institutionId = resultSet.getInt("institution_id");
                    course = new Course(courseId, name, institutionId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return course;
    }


    public boolean editCourseName(int courseId, String newName) {
        // Check if the new name already exists for the same institution
        Course existingCourse = getCourseByName(newName);
        if (existingCourse != null && existingCourse.getId() != courseId) {
            return false; // Another course with the same name already exists
        }

        // Update the course name in the database
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE courses SET name = ? WHERE id = ?")) {
            statement.setString(1, newName);
            statement.setInt(2, courseId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Error occurred or course name not updated
    }


    public boolean deleteCourse(int courseId) {
        // Check if the course is assigned to any students
        boolean isAssigned = isCourseAssigned(courseId);
        if (isAssigned) {
            return false; // Course is assigned to at least one student, cannot delete
        }

        return deleteCourseFromDatabase(courseId);
    }


    // course is assigned to any students
    private boolean isCourseAssigned(int courseId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM student_courses WHERE course_id = ?")) {
            statement.setInt(1, courseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Error occurred or course not assigned
    }

    public boolean addCourse(String name, int institutionId) {
        // Check if a course with the same name already exists in the institution
        if (isCourseNameExistsInInstitution(name, institutionId)) {
            return false; // Course with the same name already exists in the institution
        }

        // Add the course to the database
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO courses (name, institution_id) VALUES (?, ?)")) {
            statement.setString(1, name);
            statement.setInt(2, institutionId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Error occurred or course not added
    }

    boolean isCourseNameExistsInInstitution(String name, int institutionId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM courses WHERE name = ? AND institution_id = ?")) {
            statement.setString(1, name);
            statement.setInt(2, institutionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Error occurred or course name does not exist in the institution
    }
    // Method to delete the course from the database
    private boolean deleteCourseFromDatabase(int courseId) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM courses WHERE id = ?")) {
            statement.setInt(1, courseId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Error occurred or course not deleted
    }
}
