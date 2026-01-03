package hospital_managment.lazyload;

import hospital_managment.domain.Doctor;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.RepositoryRegistry;
import hospital_managment.repository.DoctorRepository;
import hospital_managment.patterns.Query;
import java.util.List;

public class AppointmentDoctorLazyLoad {
  
  private final Integer doctorId;
  private Doctor cached;
  
  public AppointmentDoctorLazyLoad(Integer doctorId) {
    this.doctorId = doctorId;
  }
  
  public Integer getId() {
    return doctorId;
  }
  
  public Doctor get() {
    if (cached == null && doctorId != null) {
      DoctorRepository repo = (DoctorRepository) RepositoryRegistry.getInstance()
          .getRepository(Doctor.class);
      
      Query query = new Query()
          .where("d.doctor_id", Query.Operator.EQUALS, doctorId)
          .limit(1);
      
      List<Doctor> doctors = repo.find(
          query, 
          UnitOfWorkContext.getIdentityMap(), 
          UnitOfWorkContext.getCurrent().getConnection()
      );
      
      if (!doctors.isEmpty()) {
        cached = doctors.get(0);
      }
    }
    return cached;
  }
  
  public void set(Doctor doctor) {
    this.cached = doctor;
  }
}
