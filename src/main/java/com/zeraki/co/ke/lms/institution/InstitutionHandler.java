package com.zeraki.co.ke.lms.institution;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.zeraki.co.ke.lms.db.DatabaseManager;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class InstitutionHandler implements HttpHandler {
    private InstitutionManager institutionManager;

    public InstitutionHandler() {
        this.institutionManager = new InstitutionManager();
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
            try {
                handlePutRequest(exchange);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else if (method.equals("DELETE")) {
            try {
                handleDeleteRequest(exchange);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            sendResponse(exchange, 405, " {\"error\": \"Method Not Allowed \"}");
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException, JSONException {
        List<Institution> institutions = institutionManager.getAllInstitutions();
        JSONObject jsonResponse = new JSONObject();
        for (Institution institution : institutions) {
            jsonResponse.put(String.valueOf(institution.getId()), institutionToJson(institution));
        }
        sendResponse(exchange, 200, jsonResponse.toString());
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException, JSONException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }
        br.close();

        JSONObject jsonRequest = new JSONObject(requestBody.toString());
        String name = jsonRequest.getString("name");

        Institution newInstitution = institutionManager.addInstitution(name);
        if (newInstitution != null) {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put(String.valueOf(newInstitution.getId()), institutionToJson(newInstitution));
            sendResponse(exchange, 201, jsonResponse.toString());
        } else {
            sendResponse(exchange, 400, " {\"error\": \"Failed to add institution. Check if it already exists. \"}");
        }
    }
    private void handlePutRequest(HttpExchange exchange) throws IOException, JSONException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 3) {
            int id;

            try {
                id = Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, " {\"error\": \"Invalid ID format\"}");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            reader.close();

            JSONObject jsonRequest = new JSONObject(requestBody.toString());
            String newName = jsonRequest.optString("name");

            if (newName.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"New name cannot be empty \"}");
                return;
            }

            boolean success = institutionManager.updateInstitutionName(id, newName);
            if (success) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("id", id);
                jsonResponse.put("name", newName);
                sendResponse(exchange, 200, jsonResponse.toString());
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Institution not found or failed to update institution name \"}" );
            }
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Invalid Request \"}");
        }
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException, JSONException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length == 3) {
            int id;
            try {
                id = Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, " {\"error\": \"Invalid ID format\"}");
                return;
            }

            boolean success = institutionManager.deleteInstitution(id);
            if (success) {
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("id", id);
                sendResponse(exchange, 200, jsonResponse.toString());
            } else {
                try (Connection connection = DatabaseManager.getConnection()) {
                    // Check if the institution has associated courses or students
                    if (institutionManager.hasAssociatedCourses(connection, id)) {
                        sendResponse(exchange, 400, "{\"error\": \"Cannot delete institution with associated courses\"}");
                    } else if (institutionManager.hasAssociatedStudents(connection, id)) {
                        sendResponse(exchange, 400, "{\"error\": \"Cannot delete institution with associated students\"}");
                    } else {
                        sendResponse(exchange, 404, "{\"error\": \"Institution not found\"}");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
                }
            }
        } else {
            sendResponse(exchange, 400, "{\"error\": \"Invalid request\"}");
        }
    }


    private JSONObject institutionToJson(Institution institution) throws JSONException {
        JSONObject jsonInstitution = new JSONObject();
        jsonInstitution.put("id", institution.getId());
        jsonInstitution.put("name", institution.getName());
        return jsonInstitution;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
