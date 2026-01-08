package hospital_managment.controller;

import hospital_managment.domain.Doctor;
import hospital_managment.domain.Hospital;
import hospital_managment.domain.Admin;
import hospital_managment.domain.EmailVerificationToken;
import hospital_managment.domain.TimeRange;
import hospital_managment.service.DoctorService;
import hospital_managment.service.HospitalService;
import hospital_managment.service.UserService;
import hospital_managment.service.EmailService;
import hospital_managment.service.EmailVerificationService;
import hospital_managment.security.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DoctorController extends BaseController {
    private static final DoctorService doctorService = DoctorService.getInstance();
    private HospitalService hospitalService = new HospitalService();
    private UserService userService = new UserService();
    private EmailService emailService = new EmailService();
    private EmailVerificationService verificationService = new EmailVerificationService();

    public void getDoctor(HttpServletRequest request, HttpServletResponse response, int doctorId) throws IOException {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        
        if (doctor != null) {
            Map<String, Object> data = mapDoctorToResponse(doctor);
            sendSuccess(response, data);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Doctor not found");
        }
    }

    public void getDoctorsByHospital(HttpServletRequest request, HttpServletResponse response, int hospitalId) throws IOException {
        List<Doctor> doctors = doctorService.getDoctorsByHospital(hospitalId);
        List<Map<String, Object>> doctorData = doctors.stream()
            .map(this::mapDoctorToResponse)
            .collect(Collectors.toList());
        
        sendSuccess(response, doctorData);
    }

    public void createDoctor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only hospital admins can create doctors");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
        
        String name = (String) data.get("name");
        String surname = (String) data.get("surname");
        String email = (String) data.get("email");
        String login = (String) data.get("login");
        String password = (String) data.get("password");
        String specialty = (String) data.get("specialty");
        int office = ((Double) data.get("office")).intValue();
        
        Admin currentAdmin = getCurrentAdmin();
        if (currentAdmin == null) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Admin not found");
            return;
        }
        
        if (currentAdmin.getHospital() == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Admin is not assigned to any hospital");
            return;
        }
        
        Hospital hospital = currentAdmin.getHospital();
        
        String startTime = (String) data.get("startTime");
        String endTime = (String) data.get("endTime");
        List<Integer> workingDays = new ArrayList<>();
        if (data.get("workingDays") instanceof List) {
            for (Object day : (List<?>) data.get("workingDays")) {
                if (day instanceof Double) {
                    workingDays.add(((Double) day).intValue());
                } else if (day instanceof Integer) {
                    workingDays.add((Integer) day);
                } else {
                    workingDays.add(Integer.parseInt(day.toString()));
                }
            }
        }
        
        if (startTime == null || endTime == null || workingDays.isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Schedule information (startTime, endTime, workingDays) is required");
            return;
        }

        Doctor doctor = doctorService.createDoctor(name, surname, email, login, password, hospital, specialty, office, startTime, endTime, workingDays);
        
        EmailVerificationToken verificationToken = verificationService.createToken(doctor);
        emailService.sendVerificationEmail(doctor, verificationToken.getToken());
        
        Map<String, Object> result = mapDoctorToResponse(doctor);
        result.put("message", "Doctor created successfully. Verification email sent to " + email);
        sendSuccess(response, result);
    }

    public void updateDoctor(HttpServletRequest request, HttpServletResponse response, int doctorId) throws IOException {
        if (!isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only hospital admins can update doctors");
            return;
        }

        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Doctor not found");
            return;
        }

        if (!canManageDoctor(doctor)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, 
                "You can only update doctors in your own hospital");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
        if (data.containsKey("name")) doctor.setName((String) data.get("name"));
        if (data.containsKey("surname")) doctor.setSurname((String) data.get("surname"));
        if (data.containsKey("email")) doctor.setEmail((String) data.get("email"));
        if (data.containsKey("login")) doctor.setLogin((String) data.get("login"));
        if (data.containsKey("password") && data.get("password") != null && !((String) data.get("password")).isEmpty()) {
            doctor.setPassword((String) data.get("password"));
        }
        if (data.containsKey("specialty")) doctor.setSpecialty((String) data.get("specialty"));
        if (data.containsKey("office")) doctor.setOffice(((Double) data.get("office")).intValue());
        
        doctorService.updateDoctor(doctor);
        sendSuccess(response, Map.of("message", "Doctor updated successfully"));
    }

    public void deleteDoctor(HttpServletRequest request, HttpServletResponse response, int doctorId) throws IOException {
        if (!isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only hospital admins can delete doctors");
            return;
        }

        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Doctor not found");
            return;
        }

        if (!canManageDoctor(doctor)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, 
                "You can only delete doctors from your own hospital");
            return;
        }
        
        doctorService.deleteDoctor(doctor);
        sendSuccess(response, Map.of("message", "Doctor deleted successfully"));
    }

    public void getDoctorSchedule(HttpServletRequest request, HttpServletResponse response, int doctorId) throws IOException {
        int effectiveDoctorId = doctorId;
        if (isDoctor()) {
            effectiveDoctorId = getCurrentUserId();
            if (effectiveDoctorId != doctorId && doctorId > 0) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only view your own schedule");
                return;
            }
        }
        
        Doctor doctor = doctorService.getDoctorById(effectiveDoctorId);
        if (doctor == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Doctor not found");
            return;
        }

        if (isAdmin() && !canManageDoctor(doctor)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only view schedules for doctors in your hospital");
            return;
        }

        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("doctorId", effectiveDoctorId);
        
        if (doctor.getWeekSchedule() != null) {
            Map<String, Map<String, String>> weekSchedule = new HashMap<>();
            doctor.getWeekSchedule().forEach((day, timeRange) -> {
                Map<String, String> timeData = new HashMap<>();
                timeData.put("startTime", timeRange.getStartTime().toString());
                timeData.put("endTime", timeRange.getEndTime().toString());
                weekSchedule.put(day.toString(), timeData);
            });
            scheduleData.put("weekSchedule", weekSchedule);
        }
        
        if (doctor.getWeekSchedule() == null) {
            scheduleData.put("message", "No schedule configured for this doctor");
        }
        
        sendSuccess(response, scheduleData);
    }

    public void getCurrentDoctorSchedule(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isDoctor()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only doctors can access this endpoint");
            return;
        }
        
        int doctorId = getCurrentUserId();
        Doctor doctor = doctorService.getDoctorById(doctorId);
        
        if (doctor == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Doctor not found");
            return;
        }

        List<Map<String, Object>> scheduleArray = new ArrayList<>();
        
        if (doctor.getWeekSchedule() != null && !doctor.getWeekSchedule().isEmpty()) {
            doctor.getWeekSchedule().forEach((day, timeRange) -> {
                Map<String, Object> daySchedule = new HashMap<>();
                daySchedule.put("dayOfWeek", day.toString());
                daySchedule.put("startTime", timeRange.getStartTime().toString());
                daySchedule.put("endTime", timeRange.getEndTime().toString());
                scheduleArray.add(daySchedule);
            });
        }
        
        sendSuccess(response, scheduleArray);
    }

    private Map<String, Object> mapDoctorToResponse(Doctor doctor) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", doctor.getId());
        data.put("name", doctor.getName());
        data.put("surname", doctor.getSurname());
        data.put("email", doctor.getEmail());
        data.put("login", doctor.getLogin());
        data.put("specialty", doctor.getSpecialty());
        data.put("office", doctor.getOffice());
        if (doctor.getHospital() != null) {
            data.put("hospitalId", doctor.getHospital().getId());
            data.put("hospitalName", doctor.getHospital().getName());
        }
        return data;
    }

    private Admin getCurrentAdmin() {
        Integer userId = AuthContext.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userService.getAdminById(userId);
    }

    private boolean canManageDoctor(Doctor doctor) {
        Admin currentAdmin = getCurrentAdmin();
        if (currentAdmin == null || currentAdmin.getHospital() == null) {
            return false;
        }
        if (doctor.getHospital() == null) {
            return false;
        }
        return currentAdmin.getHospital().getId() == doctor.getHospital().getId();
    }

    public void getAvailableAppointmentSlots(HttpServletRequest request, HttpServletResponse response, int doctorId) throws IOException {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Doctor not found");
            return;
        }

        String dateStr = request.getParameter("date");
        if (dateStr == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter: date");
            return;
        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            
            List<Map<String, String>> availableSlots = doctorService.getAvailableSlots(doctor, date);
            
            if (doctor.getWeekSchedule() == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("doctorId", doctorId);
                result.put("doctorName", doctor.getName() + " " + doctor.getSurname());
                result.put("date", dateStr);
                result.put("availableSlots", new ArrayList<String>());
                result.put("message", "Doctor does not have a schedule configured");
                sendSuccess(response, result);
                return;
            }
            
            TimeRange dayTimeRange = doctor.getWeekSchedule().get(date.getDayOfWeek());
            if (dayTimeRange == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("doctorId", doctorId);
                result.put("doctorName", doctor.getName() + " " + doctor.getSurname());
                result.put("date", dateStr);
                result.put("availableSlots", new ArrayList<String>());
                result.put("message", "Doctor does not work on " + date.getDayOfWeek());
                sendSuccess(response, result);
                return;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("doctorId", doctorId);
            result.put("doctorName", doctor.getName() + " " + doctor.getSurname());
            result.put("date", dateStr);
            result.put("availableSlots", availableSlots);
            
            sendSuccess(response, result);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to get available appointment slots: " + e.getMessage());
        }
    }

    public void getSpecialties(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> specialties = doctorService.getAllSpecialties();
        sendSuccess(response, Map.of("specialties", specialties));
    }
}
