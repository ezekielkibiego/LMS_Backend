package com.zeraki.co.ke.lms.student;

public class Student {
    private int id;
    private String name;
    private int courseId; // ID of the course the student is assigned to
    private int institutionId; // ID of the institution the student belongs to

    // Constructor
    public Student(int id, String name, int courseId, int institutionId) {
        this.id = id;
        this.name = name;
        this.courseId = courseId;
        this.institutionId = institutionId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(int institutionId) {
        this.institutionId = institutionId;
    }

    // toString method
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", courseId=" + courseId +
                ", institutionId=" + institutionId +
                '}';
    }
}
