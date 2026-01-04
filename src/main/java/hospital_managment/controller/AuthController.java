package hospital_managment.controller;

import hospital_managment.domain.User;
import hospital_managment.domain.Patient;
import hospital_managment.domain.EmailVerificationToken;
import hospital_managment.service.UserService;
import hospital_managment.service.EmailService;
import hospital_managment.service.EmailVerificationService;
import hospital_managment.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthController extends BaseController {
    private UserService userService = new UserService();
    private EmailService emailService = new EmailService();
    private EmailVerificationService verificationService = new EmailVerificationService();

    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, String> credentials = gson.fromJson(sb.toString(), Map.class);
        String login = credentials.get("login");
        String passwordHash = credentials.get("password");
        
        User user = userService.authenticate(login, passwordHash);
        
        if (user == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
            return;
        }
        
        String token = JwtUtil.generateToken(user.getId(), user.getRole().toString());
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("login", user.getLogin());
        userData.put("name", user.getName());
        userData.put("surname", user.getSurname());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole().toString());
        userData.put("token", token);
        sendSuccess(response, userData);
    }

    public void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String> data = gson.fromJson(sb.toString(), Map.class);
        String name = data.get("name");
        String surname = data.get("surname");
        String email = data.get("email");
        String login = data.get("login");
        String password = data.get("password");
        
        Patient patient = userService.createPatient(name, surname, email, login, password);
        EmailVerificationToken verificationToken = verificationService.createToken(patient);
        
        try {
            emailService.sendVerificationEmail(patient, verificationToken.getToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Registration successful. Please check your email (or console) to verify your account.");
        sendSuccess(response, result);
    }

    public void verifyEmail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = request.getParameter("token");
        
        if (token == null || token.isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Verification token is required");
            return;
        }
        
        EmailVerificationToken verificationToken = verificationService.findByToken(token);
        
        if (verificationToken == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid verification token");
            return;
        }
        
        if (verificationToken.isUsed()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Token has already been used");
            return;
        }
        
        if (!verificationService.validateAndUseToken(token)) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Token is invalid or expired");
            return;
        }
        
        User user = userService.getUserById(verificationToken.getUser().getId());
        user.setEmailVerified(true);
        userService.updateUser(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Email verified successfully. You can now login.");
        sendSuccess(response, result);
    }
}
