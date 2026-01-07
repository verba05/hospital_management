package hospital_managment.controller;

import hospital_managment.domain.Appointment;
import hospital_managment.security.AuthContext;
import hospital_managment.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppointmentController extends BaseController {
    private AppointmentService appointmentService = AppointmentService.getInstance();

    public void getAppointment(HttpServletRequest request, HttpServletResponse response, int appointmentId) throws IOException {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        
        if (appointment != null) {
            Map<String, Object> data = mapAppointmentToResponse(appointment);
            sendSuccess(response, data);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
        }
    }

    public void getAppointmentsByPatient(HttpServletRequest request, HttpServletResponse response, int patientId) throws IOException {
        int effectivePatientId = patientId;
        if (isPatient()) {
            effectivePatientId = getCurrentUserId();
            if (effectivePatientId != patientId && patientId > 0) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only view your own appointments");
                return;
            }
        }

        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(effectivePatientId);
        List<Map<String, Object>> data = appointments.stream()
            .map(this::mapAppointmentToResponse)
            .collect(Collectors.toList());
        sendSuccess(response, data);
    }

    public void getAppointmentsByDoctor(HttpServletRequest request, HttpServletResponse response, int doctorId) throws IOException {
        if (!isDoctor() || !isCurrentUser(doctorId)) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only view your own appointments");
            return;
        }
        System.out.println("Fetching appointments for doctor from by doctor");
        List<Appointment> appointments = appointmentService.getAppointmentsByDoctor(doctorId);
        List<Map<String, Object>> data = appointments.stream()
            .map(this::mapAppointmentToResponse)
            .collect(Collectors.toList());
        sendSuccess(response, data);
    }

    public void createAppointment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isPatient()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only patients can create appointments");
            return;
        }

        int patientId = AuthContext.getCurrentUserId();

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        String requestBody = sb.toString();
        
        Map<String, Object> appointmentData = gson.fromJson(requestBody, Map.class);
        
        int doctorId = ((Double) appointmentData.get("doctorId")).intValue();
        String dateStr = appointmentData.containsKey("appointmentDate") 
            ? (String) appointmentData.get("appointmentDate")
            : (String) appointmentData.get("date");
        String timeStr = appointmentData.containsKey("appointmentTime")
            ? (String) appointmentData.get("appointmentTime")
            : (String) appointmentData.get("time");
        String notes = (String) appointmentData.get("notes");

        LocalDate date = LocalDate.parse(dateStr);
        LocalTime time = timeStr != null ? LocalTime.parse(timeStr) : null;
        
        try {
            Appointment appointment = appointmentService.createAppointment(patientId, doctorId, date, time, notes);
            
            Map<String, Object> data = mapAppointmentToResponse(appointment);
            sendSuccess(response, data);
        } catch (IllegalStateException e) {
            sendError(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    public void updateAppointment(HttpServletRequest request, HttpServletResponse response, int appointmentId) throws IOException {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
        if (data.containsKey("date")) {
            String dateStr = (String) data.get("date");
            appointment.setDate(LocalDate.parse(dateStr));
        }
        if (data.containsKey("notes")) {
            appointment.setNotes((String) data.get("notes"));
        }
        
        appointmentService.updateAppointment(appointment);
        sendSuccess(response, Map.of("message", "Appointment updated successfully"));
    }

    public void deleteAppointment(HttpServletRequest request, HttpServletResponse response, int appointmentId) throws IOException {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
            return;
        }
        
        appointmentService.cancelAppointment(appointment);
        sendSuccess(response, Map.of("message", "Appointment cancelled successfully"));
    }

    @SuppressWarnings("unchecked")
    public void updateAppointmentStatus(HttpServletRequest request, HttpServletResponse response, int appointmentId) throws IOException {
        if (!isDoctor()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only doctors can update appointment status");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, String> updateData = gson.fromJson(sb.toString(), Map.class);
        String status = updateData.get("status");
        String notes = updateData.get("notes");

        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
            return;
        }

        if (appointment.getDoctor() == null || !isCurrentUser(appointment.getDoctor().getId())) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only update your own appointments");
            return;
        }

        if (notes != null) {
            appointment.setNotes(notes);
        }

        switch (status != null ? status.toUpperCase() : "") {
            case "COMPLETED":
                appointmentService.completeAppointment(appointment);
                break;
            case "NO_SHOW":
                appointmentService.noShowAppointment(appointment);
                break;
            case "CANCELED":
                appointmentService.cancelAppointment(appointment);
                break;
            case "SCHEDULED":
                appointmentService.scheduleAppointment(appointment);
                break;
            default:
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid status. Use COMPLETED, NO_SHOW, CANCELED, or SCHEDULED");
                return;
        }

        sendSuccess(response, Map.of("message", "Appointment status updated to " + status));
    }

    public void cancelAppointment(HttpServletRequest request, HttpServletResponse response, int appointmentId) throws IOException {
        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Appointment not found");
            return;
        }

        if (!isPatient() || appointment.getPatient() == null || !isCurrentUser(appointment.getPatient().getId())) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only cancel your own appointments");
            return;
        }
        
        appointmentService.cancelAppointment(appointment);
        sendSuccess(response, Map.of("message", "Appointment cancelled successfully"));
    }

    private Map<String, Object> mapAppointmentToResponse(Appointment appointment) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", appointment.getId());
        data.put("date", appointment.getDate().toString());
        data.put("time", appointment.getTime() != null ? appointment.getTime().toString() : null);
        data.put("status", appointment.getStatus().toString());
        data.put("notes", appointment.getNotes());
        
        if (appointment.getPatient() != null) {
            data.put("patientId", appointment.getPatient().getId());
            data.put("patientName", appointment.getPatient().getName() + " " + appointment.getPatient().getSurname());
        }
        
        if (appointment.getDoctor() != null) {
            Map<String, Object> doctorData = new HashMap<>();
            doctorData.put("id", appointment.getDoctor().getId());
            doctorData.put("name", appointment.getDoctor().getName());
            doctorData.put("surname", appointment.getDoctor().getSurname());
            doctorData.put("specialty", appointment.getDoctor().getSpecialty());
            doctorData.put("officeNumber", appointment.getDoctor().getOffice());
            
            if (appointment.getDoctor().getHospital() != null) {
                Map<String, Object> hospitalData = new HashMap<>();
                hospitalData.put("id", appointment.getDoctor().getHospital().getId());
                hospitalData.put("name", appointment.getDoctor().getHospital().getName());
                hospitalData.put("streetAddress", appointment.getDoctor().getHospital().getStreetAddress());
                hospitalData.put("city", appointment.getDoctor().getHospital().getCity());
                hospitalData.put("postalCode", appointment.getDoctor().getHospital().getPostalCode());
                doctorData.put("hospital", hospitalData);
            }
            
            data.put("doctor", doctorData);
        }
        
        return data;
    }
}
