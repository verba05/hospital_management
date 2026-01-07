package hospital_managment.controller;

import hospital_managment.domain.Hospital;
import hospital_managment.domain.Admin;
import hospital_managment.service.HospitalService;
import hospital_managment.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HospitalController extends BaseController {
    private HospitalService hospitalService = new HospitalService();
    private UserService userService = new UserService();

    public void getHospital(HttpServletRequest request, HttpServletResponse response, int hospitalId) throws IOException {
        Hospital hospital = hospitalService.getHospitalById(hospitalId);
        
        if (hospital != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", hospital.getId());
            data.put("name", hospital.getName());
            data.put("address", hospital.getFullAddress());
            data.put("streetAddress", hospital.getStreetAddress());
            data.put("city", hospital.getCity());
            data.put("postalCode", hospital.getPostalCode());
            data.put("stateProvince", hospital.getStateProvince());
            data.put("country", hospital.getCountry());
            sendSuccess(response, data);
        } else {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Hospital not found");
        }
    }

    public void searchHospitals(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String searchQuery = request.getParameter("search");
        String city = request.getParameter("city");
        String country = request.getParameter("country");
        String name = request.getParameter("name");
        String streetAddress = request.getParameter("streetAddress");
        String stateProvince = request.getParameter("stateProvince");
        
        List<Hospital> hospitals;
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            hospitals = hospitalService.searchHospitals(searchQuery);
        }

        else if ((name != null && !name.trim().isEmpty()) || 
                 (city != null && !city.trim().isEmpty()) ||
                 (country != null && !country.trim().isEmpty()) ||
                 (streetAddress != null && !streetAddress.trim().isEmpty()) ||
                 (stateProvince != null && !stateProvince.trim().isEmpty())) {
            hospitals = hospitalService.searchHospitalsByMultipleCriteria(name, city, streetAddress, stateProvince, country);
        } else {
            hospitals = hospitalService.getAllHospitals();
        }
        
        List<Map<String, Object>> hospitalData = hospitals.stream()
            .map(h -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", h.getId());
                data.put("name", h.getName());
                data.put("address", h.getFullAddress());
                data.put("streetAddress", h.getStreetAddress());
                data.put("city", h.getCity());
                data.put("postalCode", h.getPostalCode());
                data.put("stateProvince", h.getStateProvince());
                data.put("country", h.getCountry());
                return data;
            })
            .collect(Collectors.toList());
        
        sendSuccess(response, hospitalData);
    }

    public void updateHospital(HttpServletRequest request, HttpServletResponse response, int hospitalId) throws IOException {
         if (!isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only admin can edit hospital");
            return;
        }
        Hospital hospital = hospitalService.getHospitalById(hospitalId);
        if (hospital == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Hospital not found");
            return;
        }

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
        if (data.containsKey("name")) {
            hospital.setName((String) data.get("name"));
        }
        if (data.containsKey("streetAddress")) {
            hospital.setStreetAddress((String) data.get("streetAddress"));
        }
        if (data.containsKey("city")) {
            hospital.setCity((String) data.get("city"));
        }
        if (data.containsKey("postalCode")) {
            hospital.setPostalCode((String) data.get("postalCode"));
        }
        if (data.containsKey("stateProvince")) {
            hospital.setStateProvince((String) data.get("stateProvince"));
        }
        if (data.containsKey("country")) {
            hospital.setCountry((String) data.get("country"));
        }
        if (data.containsKey("appointmentIntervalMinutes")) {
            hospital.setAppointmentIntervalMinutes(((Double) data.get("appointmentIntervalMinutes")).intValue());
        }

        hospitalService.updateHospital(hospital);
        sendSuccess(response, Map.of("message", "Hospital updated successfully"));
    }

    public void getAdminHospital(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only admins can access this endpoint");
            return;
        }

        Integer adminId = getCurrentUserId();
        
        if (adminId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        Admin admin = userService.getAdminById(adminId);
        
        if (admin == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Admin not found");
            return;
        }

        Hospital hospital = admin.getHospital();
        
        if (hospital == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "No hospital assigned to this admin");
            return;
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", hospital.getId());
        data.put("name", hospital.getName());
        data.put("address", hospital.getFullAddress());
        data.put("streetAddress", hospital.getStreetAddress());
        data.put("city", hospital.getCity());
        data.put("postalCode", hospital.getPostalCode());
        data.put("stateProvince", hospital.getStateProvince());
        data.put("country", hospital.getCountry());
        data.put("appointmentIntervalMinutes", hospital.getAppointmentIntervalMinutes());
        
        sendSuccess(response, data);
    }
}
