package com.zeraki.co.ke.lms.course;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zeraki.co.ke.lms.institution.InstitutionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseHandler implements HttpHandler {
    private CourseManager courseManager;
    private InstitutionManager institutionManager;

    public CourseHandler() {
        this.courseManager = new CourseManager();
        this.institutionManager = new InstitutionManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method) {
            case "GET":
                handleGetRequest(exchange);
                break;
            case "DELETE":
                handleDeleteCourseRequest(exchange);
                break;
            case "POST":
                handleAddCourseRequest(exchange);
                break;
            case "PUT":
                handlePutRequest(exchange);
                break;
            default:
                sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
                break;
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        if (queryParams.containsKey("institutionId")) {
            int institutionId = Integer.parseInt(queryParams.get("institutionId"));
            List<Course> courses = courseManager.getCoursesByInstitutionId(institutionId);

            // Filter courses by search query (if provided)
            if (queryParams.containsKey("search")) {
                String searchQuery = queryParams.get("search").toLowerCase();
                courses = courses.stream()
                        .filter(course -> course.getName().toLowerCase().contains(searchQuery))
                        .collect(Collectors.toList());
            }

            JSONArray jsonArray = new JSONArray(courses.stream().map(Course::toJson).collect(Collectors.toList()));
            sendResponse(exchange, 200, jsonArray.toString());
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Missing or invalid institutionId parameter\"}");
        }
    }

    private void handleAddCourseRequest(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("POST")) {
            // Read request body to get course details
            String requestBody = getRequestBody(exchange);

            try {
                JSONObject jsonObject = new JSONObject(requestBody);
                if (jsonObject.has("name") && jsonObject.has("institutionId")) {
                    String name = jsonObject.getString("name");
                    int institutionId = jsonObject.getInt("institutionId");

                    boolean success = courseManager.addCourse(name, institutionId);
                    if (success) {
                        sendResponse(exchange, 201, "{\"message\": \"Course added successfully\"}");
                    } else {
                        sendResponse(exchange, 400, "{\"error\": \"Course with the same name already exists in the institution\"}");
                    }
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid request body. Required fields: name, institutionId\"}");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\": \"Invalid JSON in request body\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }

    private String getRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String body;

        while ((body = br.readLine()) != null) {
            sb.append(body);
        }

        return sb.toString();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = pair.substring(0, idx);
                    String value = pair.substring(idx + 1);
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("PUT")) {
            // Read request body to get course details
            String requestBody = getRequestBody(exchange);

            try {
                JSONObject jsonObject = new JSONObject(requestBody);
                if (jsonObject.has("id") && jsonObject.has("name")) {
                    int courseId = jsonObject.getInt("id");
                    String newName = jsonObject.getString("name");

                    // Check if the course exists
                    Course course = courseManager.getCourseById(courseId);
                    if (course == null) {
                        sendResponse(exchange, 404, "{\"error\": \"Course not found\"}");
                        return;
                    }

                    // Check if the new name already exists for the same institution
                    if (!newName.equals(course.getName())) {
                        if (courseManager.isCourseNameExistsInInstitution(newName, course.getInstitutionId())) {
                            sendResponse(exchange, 400, "{\"error\": \"Course with the same name already exists in the institution\"}");
                            return;
                        }
                    }

                    // Update the name of the course
                    boolean success = courseManager.editCourseName(courseId, newName);
                    if (success) {
                        sendResponse(exchange, 200, "{\"message\": \"Course name updated successfully\"}");
                    } else {
                        sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
                    }
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid request body. Required fields: id, name\"}");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                sendResponse(exchange, 400, "{\"error\": \"Invalid JSON in request body\"}");
            }
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
        }
    }


    private void handleDeleteCourseRequest(HttpExchange exchange) throws IOException {
        // Parse the query parameters to get the course ID
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        if (queryParams.containsKey("id")) {
            int courseId = Integer.parseInt(queryParams.get("id"));

            // Check if the course exists
            Course course = courseManager.getCourseById(courseId);
            if (course == null) {
                sendResponse(exchange, 404, "{\"error\": \"Course not found\"}");
                return;
            }

            // Check if the course is assigned to any institution
            if (course.getInstitutionId() != 0) {
                sendResponse(exchange, 400, "{\"error\": \"Cannot delete course. It is assigned to an institution.\"}");
                return;
            }

            // Attempt to delete the course
            boolean success = courseManager.deleteCourse(courseId);
            if (success) {
                sendResponse(exchange, 200, "{\"message\": \"Course deleted successfully\"}");
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Cannot delete course. It is assigned to at least one student.\"}");
            }
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Missing course ID parameter\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
