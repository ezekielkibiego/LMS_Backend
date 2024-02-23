package com.zeraki.co.ke.lms.student;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentHandler implements HttpHandler {

    private StudentManager studentManager;

    public StudentHandler() {
        this.studentManager = new StudentManager();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            try {
                handleGetRequest(exchange);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else if (method.equals("POST")) {
            try {
                handlePostRequest(exchange);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else if (method.equals("PUT")) {
            handlePutRequest(exchange);
        } else if (method.equals("DELETE")) {
            handleDeleteRequest(exchange);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException, JSONException {
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        Map<String, String> queryParams = parseQueryParams(uri.getQuery());

        if (path.equals("/students")) {
            // Endpoint to get all students
            List<Student> students = studentManager.getAllStudents();
            sendResponse(exchange, 200, studentsToJson(students).toString());
        } else if (path.startsWith("/students/institution")) {
            // Endpoint to get students by institution ID
            if (queryParams.containsKey("institutionId")) {
                int institutionId = Integer.parseInt(queryParams.get("institutionId"));
                String searchQuery = queryParams.getOrDefault("search", "");
                List<Student> students = studentManager.listStudentsByInstitution(institutionId, searchQuery);
                sendResponse(exchange, 200, studentsToJson(students).toString());
            } else {
                sendResponse(exchange, 400, "Missing institutionId parameter");
            }
        } else if (path.startsWith("/students/course")) {
            // Endpoint to get students by course ID
            if (queryParams.containsKey("courseId")) {
                int courseId = Integer.parseInt(queryParams.get("courseId"));
                String searchQuery = queryParams.getOrDefault("search", "");
                List<Student> students = studentManager.listStudentsByCourse(courseId, searchQuery);
                sendResponse(exchange, 200, studentsToJson(students).toString());
            } else {
                sendResponse(exchange, 400, "Missing courseId parameter");
            }
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }



    private void handlePostRequest(HttpExchange exchange) throws IOException, JSONException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String body;

        while ((body = br.readLine()) != null) {
            sb.append(body);
        }

        String requestBody = sb.toString();
        JSONObject jsonObject = new JSONObject(requestBody);

        if (jsonObject.has("name") && jsonObject.has("courseId") && jsonObject.has("institutionId")) {
            String name = jsonObject.getString("name");
            int courseId = jsonObject.getInt("courseId");
            int institutionId = jsonObject.getInt("institutionId");

            boolean success = studentManager.addStudent(name, courseId, institutionId);
            if (success) {
                sendResponse(exchange, 201, "Student added successfully");
            } else {
                sendResponse(exchange, 500, "Internal Server Error");
            }
        } else {
            sendResponse(exchange, 400, "Invalid request body");
        }
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        Map<String, String> queryParams = parseQueryParams(uri.getQuery());

        if (path.startsWith("/students")) {
            if (queryParams.containsKey("id") && queryParams.containsKey("name")) {
                int studentId = Integer.parseInt(queryParams.get("id"));
                String newName = queryParams.get("name");

                boolean success = studentManager.editStudentName(studentId, newName);
                if (success) {
                    sendResponse(exchange, 200, "Student name updated successfully");
                } else {
                    sendResponse(exchange, 404, "Student not found");
                }
            } else {
                sendResponse(exchange, 400, "Missing id or name parameter");
            }
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        Map<String, String> queryParams = parseQueryParams(uri.getQuery());

        if (path.startsWith("/students/delete")) {
            if (queryParams.containsKey("studentId")) {
                int studentId = Integer.parseInt(queryParams.get("studentId"));

                boolean success = studentManager.deleteStudent(studentId);
                if (success) {
                    sendResponse(exchange, 200, "Student deleted successfully");
                } else {
                    sendResponse(exchange, 404, "Student not found");
                }
            } else {
                sendResponse(exchange, 400, "Missing studentId parameter");
            }
        } else {
            sendResponse(exchange, 404, "Not Found");
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

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private JSONObject studentsToJson(List<Student> students) throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray institutionsArray = new JSONArray();

        // Group students by institution ID
        Map<Integer, JSONArray> institutionMap = new HashMap<>();
        for (Student student : students) {
            int institutionId = student.getInstitutionId();
            JSONObject studentObject = new JSONObject();
            studentObject.put("id", student.getId());
            studentObject.put("name", student.getName());
            studentObject.put("courseId", student.getCourseId());

            if (!institutionMap.containsKey(institutionId)) {
                JSONArray studentsArray = new JSONArray();
                studentsArray.put(studentObject);
                institutionMap.put(institutionId, studentsArray);
            } else {
                institutionMap.get(institutionId).put(studentObject);
            }
        }

        // Convert grouped students into institutions
        for (Map.Entry<Integer, JSONArray> entry : institutionMap.entrySet()) {
            JSONObject institutionObject = new JSONObject();
            institutionObject.put("id", entry.getKey());
            institutionObject.put("students", entry.getValue());
            institutionsArray.put(institutionObject);
        }

        result.put("institutions", institutionsArray);
        return result;
    }

}
