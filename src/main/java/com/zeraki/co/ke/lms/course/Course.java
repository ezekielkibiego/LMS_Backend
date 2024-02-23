package com.zeraki.co.ke.lms.course;

import org.json.JSONException;
import org.json.JSONObject;

public class Course {
    private int id;
    private String name;
    private int institutionId;

    public Course(int id, String name, int institutionId) {
        this.id = id;
        this.name = name;
        this.institutionId = institutionId;
    }

    // Getters and setters
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

    public int getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(int institutionId) {
        this.institutionId = institutionId;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", institutionId=" + institutionId +
                '}';
    }
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("institutionId", institutionId);
        } catch (JSONException e) {
            e.printStackTrace(); // Handle or log the exception as needed
        }
        return json;
    }

}
