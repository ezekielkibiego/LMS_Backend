package com.zeraki.co.ke.lms.course;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.zeraki.co.ke.lms.institution.InstitutionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
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
        if (method.equals("GET")) {
            handleGetRequest(exchange);
        } else if (method.equals("DELETE")) {
            handleDeleteRequest(exchange);
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
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

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        // Parse the query parameters to get the course ID
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        if (queryParams.containsKey("id")) {
            int courseId = Integer.parseInt(queryParams.get("id"));

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
