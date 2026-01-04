package hospital_managment.service;

import hospital_managment.domain.Appointment;
import hospital_managment.domain.Doctor;
import hospital_managment.domain.Hospital;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.Query;
import hospital_managment.repository.AppointmentRepository;
import hospital_managment.repository.DoctorRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AppointmentService {
    private static AppointmentService instance;
    
    private AppointmentService() {
    }
    
    public static AppointmentService getInstance() {
        if (instance == null) {
            synchronized (AppointmentService.class) {
                if (instance == null) {
                    instance = new AppointmentService();
                }
            }
        }
        return instance;
    }

    public Appointment getAppointmentById(int id) {
        AppointmentRepository appointmentRepo = (AppointmentRepository) UnitOfWorkContext.getRegistry().getRepository(Appointment.class);
        Query query = new Query()
            .where("id", Query.Operator.EQUALS, id)
            .limit(1);
        
        List<Appointment> appointments = appointmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        return appointments.isEmpty() ? null : appointments.get(0);
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        AppointmentRepository appointmentRepo = (AppointmentRepository) UnitOfWorkContext.getRegistry().getRepository(Appointment.class);
        Query query = new Query()
            .where("patient_id", Query.Operator.EQUALS, patientId);
        
        List<Appointment> appointments = appointmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        appointments.sort((a1, a2) -> {
            int priority1 = getStatusPriority(a1.getStatus());
            int priority2 = getStatusPriority(a2.getStatus());
            
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }
            
            if (a1.getDate() != null && a2.getDate() != null) {
                if (priority1 == 1) {
                    int dateCompare = a1.getDate().compareTo(a2.getDate());
                    if (dateCompare != 0) return dateCompare;
                    if (a1.getTime() != null && a2.getTime() != null) {
                        return a1.getTime().compareTo(a2.getTime());
                    }
                } else {
                    int dateCompare = a2.getDate().compareTo(a1.getDate());
                    if (dateCompare != 0) return dateCompare;
                    if (a1.getTime() != null && a2.getTime() != null) {
                        return a2.getTime().compareTo(a1.getTime());
                    }
                }
            }
            
            return 0;
        });
        
        return appointments;
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        AppointmentRepository appointmentRepo = (AppointmentRepository) UnitOfWorkContext.getRegistry().getRepository(Appointment.class);
        Query query = new Query()
            .where("doctor_id", Query.Operator.EQUALS, doctorId);
        
        List<Appointment> appointments = appointmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        appointments.sort((a1, a2) -> {
            int priority1 = getStatusPriority(a1.getStatus());
            int priority2 = getStatusPriority(a2.getStatus());
            
            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2);
            }
            
            if (a1.getDate() != null && a2.getDate() != null) {
                if (priority1 == 1) {
                    int dateCompare = a1.getDate().compareTo(a2.getDate());
                    if (dateCompare != 0) return dateCompare;
                    if (a1.getTime() != null && a2.getTime() != null) {
                        return a1.getTime().compareTo(a2.getTime());
                    }
                } else {
                    int dateCompare = a2.getDate().compareTo(a1.getDate());
                    if (dateCompare != 0) return dateCompare;
                    if (a1.getTime() != null && a2.getTime() != null) {
                        return a2.getTime().compareTo(a1.getTime());
                    }
                }
            }
            
            return 0;
        });
        
        return appointments;
    }
    
    private int getStatusPriority(Appointment.AppointmentStatus status) {
        if (status == null) return 4;
        switch (status) {
            case SCHEDULED:
                return 1;
            case COMPLETED:
                return 2;
            case CANCELED:
            case NO_SHOW:
                return 3;
            default:
                return 4;
        }
    }

    public List<Appointment> getAppointmentsByPatientAndDoctor(int patientId, int doctorId) {
        AppointmentRepository appointmentRepo = (AppointmentRepository) UnitOfWorkContext.getRegistry().getRepository(Appointment.class);
        Query query = new Query()
            .where("patient_id", Query.Operator.EQUALS, patientId)
            .where("doctor_id", Query.Operator.EQUALS, doctorId);
        
        return appointmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
    }

    public Appointment createAppointment(int patientId, int doctorId, LocalDate date, LocalTime time, String notes) {
        AppointmentRepository appointmentRepo = (AppointmentRepository) UnitOfWorkContext.getRegistry().getRepository(Appointment.class);
        Query query = new Query()
            .where("doctor_id", Query.Operator.EQUALS, doctorId);
        
        List<Appointment> doctorAppointments = appointmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        DoctorRepository doctorRepo = (DoctorRepository) UnitOfWorkContext.getRegistry().getRepository(Doctor.class);
        Query doctorQuery = new Query()
            .where("doctor_id", Query.Operator.EQUALS, doctorId);
        List<Doctor> doctors = doctorRepo.find(doctorQuery, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        if (doctors.isEmpty()) {
            throw new IllegalArgumentException("Doctor not found with ID: " + doctorId);
        }
        
        Doctor doctor = doctors.get(0);
        Hospital hospital = doctor.getHospital();
        final int intervalMinutes;
        
        intervalMinutes = hospital.getAppointmentIntervalMinutes();
        
        LocalTime requestedEndTime = time.plusMinutes(intervalMinutes);
        
        boolean hasConflict = doctorAppointments.stream()
            .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
            .filter(apt -> apt.getDate() != null && apt.getDate().equals(date))
            .anyMatch(apt -> {
                LocalTime aptTime = apt.getTime();
                if (aptTime == null) return false;
                LocalTime aptEndTime = aptTime.plusMinutes(intervalMinutes);
                boolean overlaps = time.isBefore(aptEndTime) && aptTime.isBefore(requestedEndTime);
                return overlaps;
            });
        
        if (hasConflict) {
            String errorMsg = "This time slot is already booked. Please choose a different time.";
            throw new IllegalStateException(errorMsg);
        }
        
        Appointment appointment = new Appointment();
        
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setNotes(notes);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        
        UnitOfWorkContext.getCurrent().registerNew(appointment);
        
        return appointment;
    }

    public void scheduleAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        UnitOfWorkContext.getCurrent().registerDirty(appointment);
    }

    public void noShowAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.AppointmentStatus.NO_SHOW);
        UnitOfWorkContext.getCurrent().registerDirty(appointment);
    }

    public void completeAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        UnitOfWorkContext.getCurrent().registerDirty(appointment);
    }

    public void cancelAppointment(Appointment appointment) {
        appointment.setStatus(Appointment.AppointmentStatus.CANCELED);
        UnitOfWorkContext.getCurrent().registerDirty(appointment);
    }

    public void updateAppointment(Appointment appointment) {
        UnitOfWorkContext.getCurrent().registerDirty(appointment);
    }

    public void deleteAppointment(Appointment appointment) {
        UnitOfWorkContext.getCurrent().registerRemoved(appointment);
    }
}
