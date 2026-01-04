package hospital_managment.service;

import hospital_managment.domain.Treatment;
import hospital_managment.domain.Patient;
import hospital_managment.domain.Appointment;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.Query;
import hospital_managment.repository.TreatmentRepository;
import java.time.LocalDateTime;
import java.util.List;

public class TreatmentService {
    private static TreatmentService instance;
    
    private TreatmentService() {}
    
    public static TreatmentService getInstance() {
        if (instance == null) {
            synchronized (TreatmentService.class) {
                if (instance == null) {
                    instance = new TreatmentService();
                }
            }
        }
        return instance;
    }

    public Treatment getTreatmentById(int id) {
        TreatmentRepository treatmentRepo = (TreatmentRepository) UnitOfWorkContext.getRegistry().getRepository(Treatment.class);
        Query query = new Query()
            .where("id", Query.Operator.EQUALS, id)
            .limit(1);
        
        List<Treatment> treatments = treatmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        return treatments.isEmpty() ? null : treatments.get(0);
    }

    public List<Treatment> getTreatmentsByPatient(int patientId) {
        TreatmentRepository treatmentRepo = (TreatmentRepository) UnitOfWorkContext.getRegistry().getRepository(Treatment.class);
        Query query = new Query()
            .where("patient_id", Query.Operator.EQUALS, patientId);
        
        List<Treatment> treatments = treatmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        treatments.sort((t1, t2) -> {
            if (t1.getCreatedAt() != null && t2.getCreatedAt() != null) {
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            }
            return 0;
        });
        
        return treatments;
    }

    public List<Treatment> getTreatmentsByDoctor(int doctorId) {
        TreatmentRepository treatmentRepo = (TreatmentRepository) UnitOfWorkContext.getRegistry().getRepository(Treatment.class);
        Query query = new Query()
            .where("doctor_id", Query.Operator.EQUALS, doctorId);
        
        List<Treatment> treatments = treatmentRepo.find(query, UnitOfWorkContext.getIdentityMap(), UnitOfWorkContext.getCurrent().getConnection());
        
        treatments.sort((t1, t2) -> {
            if (t1.getCreatedAt() != null && t2.getCreatedAt() != null) {
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            }
            return 0;
        });
        
        return treatments;
    }

    public Treatment createTreatment(Patient patient, Appointment appointment, String instructions, 
                                      List<String> medications) {
        Treatment treatment = new Treatment();
        treatment.setPatient(patient);
        treatment.setAppointment(appointment);
        treatment.setInstructions(instructions);
        treatment.setCreatedAt(LocalDateTime.now());
        if (medications != null) {
            for (String med : medications) {
                treatment.addMedication(med);
            }
        }
        
        UnitOfWorkContext.getCurrent().registerNew(treatment);
        return treatment;
    }

    public void updateTreatment(Treatment treatment) {
        UnitOfWorkContext.getCurrent().registerDirty(treatment);
    }

    public void deleteTreatment(Treatment treatment) {
        UnitOfWorkContext.getCurrent().registerRemoved(treatment);
    }
}
