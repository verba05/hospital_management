package hospital_managment.lazyload;

import hospital_managment.domain.Patient;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.RepositoryRegistry;
import hospital_managment.repository.PatientRepository;
import hospital_managment.patterns.Query;
import java.util.List;

public class AppointmentPatientLazyLoad {
  
  private final Integer patientId;
  private Patient cached;
  
  public AppointmentPatientLazyLoad(Integer patientId) {
    this.patientId = patientId;
  }
  
  public Integer getId() {
    return patientId;
  }
  
  public Patient get() {
    if (cached == null && patientId != null) {
      PatientRepository repo = (PatientRepository) RepositoryRegistry.getInstance()
          .getRepository(Patient.class);
      
      Query query = new Query()
          .where("id", Query.Operator.EQUALS, patientId)
          .limit(1);
      
      List<Patient> patients = repo.find(
          query, 
          UnitOfWorkContext.getIdentityMap(), 
          UnitOfWorkContext.getCurrent().getConnection()
      );
      
      if (!patients.isEmpty()) {
        cached = patients.get(0);
      }
    }
    return cached;
  }
  
  public void set(Patient patient) {
    this.cached = patient;
  }
}
