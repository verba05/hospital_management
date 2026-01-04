package hospital_managment.controller;

import hospital_managment.domain.User;
import hospital_managment.domain.Patient;
import hospital_managment.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserController extends BaseController {
    private UserService userService = new UserService();

    @SuppressWarnings("unchecked")
    public void changePassword(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        if (!isCurrentUser(userId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only change your own password");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, String> data = gson.fromJson(sb.toString(), Map.class);
        String newPassword = data.get("newPassword");
        
        userService.changePassword(userId, newPassword);
        sendSuccess(response, Map.of("message", "Password changed successfully"));
    }

    public void getUser(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        if (!isCurrentUser(userId) && !isDoctor()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only view your own information");
            return;
        }

        User user = userService.getUserById(userId);
        
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("login", user.getLogin());
            userData.put("name", user.getName());
            userData.put("surname", user.getSurname());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole().toString());
            sendSuccess(response, userData);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    public void getPatient(HttpServletRequest request, HttpServletResponse response, int patientId) throws IOException {
        if (!isCurrentUser(patientId) && !isDoctor()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        Patient patient = userService.getPatientById(patientId);
        
        if (patient != null) {
            Map<String, Object> patientData = new HashMap<>();
            patientData.put("id", patient.getId());
            patientData.put("name", patient.getName());
            patientData.put("surname", patient.getSurname());
            patientData.put("email", patient.getEmail());
            patientData.put("login", patient.getLogin());
            sendSuccess(response, patientData);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Patient not found");
        }
    }

    @SuppressWarnings("unchecked")
    public void updatePatient(HttpServletRequest request, HttpServletResponse response, int patientId) throws IOException {
        if (!isPatient() || !isCurrentUser(patientId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only update your own information");
            return;
        }

        Patient patient = userService.getPatientById(patientId);
        if (patient == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Patient not found");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, String> data = gson.fromJson(sb.toString(), Map.class);
        if (data.containsKey("name")) patient.setName(data.get("name"));
        if (data.containsKey("surname")) patient.setSurname(data.get("surname"));
        if (data.containsKey("email")) patient.setEmail(data.get("email"));
        
        userService.updatePatient(patient);
        sendSuccess(response, Map.of("message", "Patient updated successfully"));
    }

    public void deletePatient(HttpServletRequest request, HttpServletResponse response, int patientId) throws IOException {
        if (!isPatient() || !isCurrentUser(patientId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only delete your own account");
            return;
        }

        Patient patient = userService.getPatientById(patientId);
        if (patient == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Patient not found");
            return;
        }
        
        userService.deletePatient(patient);
        sendSuccess(response, Map.of("message", "Patient deleted successfully"));
    }
}
