package hospital_managment.service;

import hospital_managment.domain.Doctor;
import hospital_managment.domain.Hospital;
import hospital_managment.domain.Appointment;
import hospital_managment.domain.UserRole;
import hospital_managment.domain.TimeRange;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.Query;
import hospital_managment.repository.DoctorRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class DoctorService {
    private static DoctorService instance;
    
    private DoctorService() {
    }
    
    public static DoctorService getInstance() {
        if (instance == null) {
            synchronized (DoctorService.class) {
                if (instance == null) {
                    instance = new DoctorService();
                }
            }
        }
        return instance;
    }

    public Doctor getDoctorById(int id) {
        DoctorRepository doctorRepo = (DoctorRepository) UnitOfWorkContext.getRegistry().getRepository(Doctor.class);
        Query query = new Query()
            .where("d.doctor_id", Query.Operator.EQUALS, id)
            .limit(1);
        
        List<Doctor> doctors = doctorRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        if (!doctors.isEmpty()) {
            Doctor doctor = doctors.get(0);
            doctorRepo.loadDoctorSchedule(doctor, UnitOfWorkContext.getCurrent().getConnection());
            return doctor;
        }
        return null;
    }

    public List<Doctor> getDoctorsByHospital(int hospitalId) {
        DoctorRepository doctorRepo = (DoctorRepository) UnitOfWorkContext.getRegistry().getRepository(Doctor.class);
        Query query = new Query()
            .where("d.hospital_id", Query.Operator.EQUALS, hospitalId);
        
        return doctorRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
    }

    public List<Doctor> getDoctorsBySpecialty(String specialty) {
        DoctorRepository doctorRepo = (DoctorRepository) UnitOfWorkContext.getRegistry().getRepository(Doctor.class);
        Query query = new Query()
            .where("d.specialty", Query.Operator.EQUALS, specialty);
        
        return doctorRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
    }

    public Doctor createDoctor(String name, String surname, String email, String login, 
                               String passwordHash, Hospital hospital, String specialty, int office,
                               String startTime, String endTime, List<Integer> workingDays) {
        Doctor doctor = new Doctor();
        try{
        doctor.setName(name);
        doctor.setSurname(surname);
        doctor.setEmail(email);
        doctor.setLogin(login);
        doctor.setPassword(passwordHash);
        doctor.setRole(UserRole.DOCTOR);
        doctor.setEmailVerified(false); // New doctors must verify email
        doctor.setHospital(hospital);
        doctor.setSpecialty(specialty);
        doctor.setOffice(office);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        doctor.scheduleStartTime = startTime;
        doctor.scheduleEndTime = endTime;
        doctor.scheduleWorkingDays = workingDays;
        
        UnitOfWorkContext.getCurrent().registerNew(doctor);
        return doctor;
    }

    public void createDoctorSchedule(int doctorId, String startTime, String endTime, List<Integer> workingDays) {
        DoctorRepository doctorRepo = (DoctorRepository) UnitOfWorkContext.getRegistry().getRepository(Doctor.class);
        doctorRepo.createDoctorSchedule(doctorId, startTime, endTime, workingDays, UnitOfWorkContext.getCurrent().getConnection());
    }

    public void updateDoctor(Doctor doctor) {
        UnitOfWorkContext.getCurrent().registerDirty(doctor);
    }

    public void deleteDoctor(Doctor doctor) {
        UnitOfWorkContext.getCurrent().registerRemoved(doctor);
    }

    public List<Appointment> getDoctorAppointments(int doctorId) {
        Doctor doctor = getDoctorById(doctorId);
        return doctor != null ? doctor.getAppointments() : List.of();
    }

    public List<String> getAllSpecialties() {
        DoctorRepository doctorRepo = (DoctorRepository) UnitOfWorkContext.getRegistry().getRepository(Doctor.class);
        return doctorRepo.getAllSpecialties(UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
    }

    /**
     * Get available appointment slots for a doctor on a specific date
     * @param doctor The doctor
     * @param date The date to check
     * @return List of available time slots (each slot is a Map with startTime and endTime)
     */
    public List<Map<String, String>> getAvailableSlots(Doctor doctor, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // Check if doctor has a schedule
        if (doctor.getWeekSchedule() == null) {
            return new ArrayList<>();
        }
        
        // Get the time range for this day of the week
        TimeRange dayTimeRange = doctor.getWeekSchedule().get(dayOfWeek);
        if (dayTimeRange == null) {
            return new ArrayList<>();  // Doctor doesn't work on this day
        }
        
        LocalTime startTime = dayTimeRange.getStartTime();
        LocalTime endTime = dayTimeRange.getEndTime();
        
        // Get hospital's appointment interval
        int intervalMinutes = doctor.getHospital().getAppointmentIntervalMinutes();
        
        // Get existing SCHEDULED appointments for this doctor on this date
        AppointmentService appointmentService = AppointmentService.getInstance();
        List<Appointment> allAppointments = appointmentService.getAppointmentsByDoctor(doctor.getId());
        
        List<Appointment> existingAppointments = allAppointments.stream()
            .filter(apt -> apt.getDate() != null && apt.getDate().equals(date))
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
            .toList();
        
        List<Map<String, String>> availableSlots = new ArrayList<>();
        LocalTime currentTime = startTime;
        
        while (!currentTime.isAfter(endTime.minusMinutes(intervalMinutes))) {
            LocalTime slotStartTime = currentTime;
            LocalTime slotEndTime = currentTime.plusMinutes(intervalMinutes);
            
            boolean hasConflict = existingAppointments.stream()
                .anyMatch(apt -> {
                    LocalTime aptTime = apt.getTime();
                    if (aptTime == null) return false;
                    
                    LocalTime aptEndTime = aptTime.plusMinutes(intervalMinutes);
                    
                    return slotStartTime.isBefore(aptEndTime) && aptTime.isBefore(slotEndTime);
                });
            
            if (!hasConflict) {
                Map<String, String> slot = new HashMap<>();
                slot.put("startTime", slotStartTime.toString());
                slot.put("endTime", slotEndTime.toString());
                availableSlots.add(slot);
            }
            
            currentTime = currentTime.plusMinutes(intervalMinutes);
        }
        
        return availableSlots;
    }
}

