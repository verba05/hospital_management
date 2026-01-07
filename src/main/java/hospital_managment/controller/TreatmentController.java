package hospital_managment.controller;

import hospital_managment.domain.Treatment;
import hospital_managment.domain.Appointment;
import hospital_managment.domain.Patient;
import hospital_managment.service.TreatmentService;
import hospital_managment.service.AppointmentService;
import hospital_managment.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

public class TreatmentController extends BaseController {
    private TreatmentService treatmentService = TreatmentService.getInstance();
    private AppointmentService appointmentService = AppointmentService.getInstance();
    private UserService userService = new UserService();

    public void getTreatmentsByPatient(HttpServletRequest request, HttpServletResponse response, int patientId) throws IOException {
        int effectivePatientId = patientId;
        if (isPatient()) {
            effectivePatientId = getCurrentUserId();
            if (effectivePatientId != patientId && patientId > 0) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only view your own treatments");
                return;
            }
        }

        List<Treatment> treatments = treatmentService.getTreatmentsByPatient(effectivePatientId);
        List<Map<String, Object>> data = treatments.stream()
            .map(this::mapTreatmentToResponse)
            .collect(Collectors.toList());
        sendSuccess(response, data);
    }

    public void getTreatmentsByDoctor(HttpServletRequest request, HttpServletResponse response, int doctorId) throws IOException {
        int effectiveDoctorId = doctorId;
        if (isDoctor()) {
            effectiveDoctorId = getCurrentUserId();
            if (effectiveDoctorId != doctorId && doctorId > 0) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only view your own treatments");
                return;
            }
        }

        List<Treatment> treatments = treatmentService.getTreatmentsByDoctor(effectiveDoctorId);
        List<Map<String, Object>> data = treatments.stream()
            .map(this::mapTreatmentToResponse)
            .collect(Collectors.toList());
        sendSuccess(response, data);
    }

    @SuppressWarnings("unchecked")
    public void createTreatment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isDoctor()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only doctors can create treatments");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
        int patientId = ((Double) data.get("patientId")).intValue();
        int appointmentId = ((Double) data.get("appointmentId")).intValue();
        String instructions = (String) data.get("instructions");
        List<String> medications = (List<String>) data.get("medications");

        Appointment appointment = appointmentService.getAppointmentById(appointmentId);
        if (appointment == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid appointment ID");
            return;
        }

        if (appointment.getDoctor() == null || !isCurrentUser(appointment.getDoctor().getId())) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only create treatments for your own appointments");
            return;
        }

        Patient patient = userService.getPatientById(patientId);
        if (patient == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid patient ID");
            return;
        }

        Treatment treatment = treatmentService.createTreatment(patient, appointment, instructions, medications);
        Map<String, Object> result = mapTreatmentToResponse(treatment);
        sendSuccess(response, result);
    }

    public void updateTreatment(HttpServletRequest request, HttpServletResponse response, int treatmentId) throws IOException {
        // Only doctors can update treatments
        if (!isDoctor()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only doctors can update treatments");
            return;
        }

        Treatment treatment = treatmentService.getTreatmentById(treatmentId);
        if (treatment == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Treatment not found");
            return;
        }

        // Verify the doctor is updating their own treatment (check via appointment)
        if (treatment.getAppointment() != null && 
            treatment.getAppointment().getDoctor() != null && 
            !isCurrentUser(treatment.getAppointment().getDoctor().getId())) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "You can only update your own treatments");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
        if (data.containsKey("instructions")) {
            treatment.setInstructions((String) data.get("instructions"));
        }
        if (data.containsKey("medications")) {
            List<String> medications = (List<String>) data.get("medications");
            Vector<String> medVector = new Vector<>(medications);
            treatment.setMedications(medVector);
        }

        treatmentService.updateTreatment(treatment);
        sendSuccess(response, Map.of("message", "Treatment updated successfully"));
    }

    public void deleteTreatment(HttpServletRequest request, HttpServletResponse response, int treatmentId) throws IOException {
        Treatment treatment = treatmentService.getTreatmentById(treatmentId);
        if (treatment == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Treatment not found");
            return;
        }

        treatmentService.deleteTreatment(treatment);
        sendSuccess(response, Map.of("message", "Treatment deleted successfully"));
    }

    private Map<String, Object> mapTreatmentToResponse(Treatment treatment) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", treatment.getId());
        data.put("instructions", treatment.getInstructions());
        data.put("medications", treatment.getMedications());
        data.put("createdAt", treatment.getCreatedAt() != null ? treatment.getCreatedAt().toString() : null);
        
        if (treatment.getPatient() != null) {
            data.put("patientId", treatment.getPatient().getId());
            data.put("patientName", treatment.getPatient().getName() + " " + treatment.getPatient().getSurname());
        }
        
        return data;
    }
}
