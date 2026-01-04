package hospital_managment.controller;

import com.google.gson.Gson;
import hospital_managment.security.AuthContext;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseController {
    protected Gson gson = new Gson();

    protected void sendJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    protected void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        sendJson(response, result);
    }

    protected void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        sendJson(response, result);
    }

    protected <T> T parseJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    protected boolean isAuthenticated() {
        return AuthContext.getCurrentUserId() != null;
    }

    protected boolean hasRole(String role) {
        String currentRole = AuthContext.getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }

    protected boolean isPatient() {
        return hasRole("PATIENT");
    }

    protected boolean isDoctor() {
        return hasRole("DOCTOR");
    }

    protected boolean isAdmin() {
        return hasRole("ADMIN");
    }

    protected Integer getCurrentUserId() {
        return AuthContext.getCurrentUserId();
    }

    protected boolean isCurrentUser(int userId) {
        Integer currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId == userId;
    }
}
