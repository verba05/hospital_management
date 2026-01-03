package hospital_managment.lazyload;

import hospital_managment.domain.Hospital;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.RepositoryRegistry;
import hospital_managment.repository.HospitalRepository;
import hospital_managment.patterns.Query;
import java.util.List;

public class DoctorHospitalLazyLoad {
  
  private final Integer hospitalId;
  private Hospital cached;
  
  public DoctorHospitalLazyLoad(Integer hospitalId) {
    this.hospitalId = hospitalId;
  }
  
  public Hospital get() {
    if (cached == null && hospitalId != null) {
      HospitalRepository repo = (HospitalRepository) RepositoryRegistry.getInstance()
          .getRepository(Hospital.class);
      
      Query query = new Query()
          .where("id", Query.Operator.EQUALS, hospitalId)
          .limit(1);
      
      List<Hospital> hospitals = repo.find(
          query, 
          UnitOfWorkContext.getIdentityMap(), 
          UnitOfWorkContext.getCurrent().getConnection()
      );
      
      if (!hospitals.isEmpty()) {
        cached = hospitals.get(0);
      }
    }
    return cached;
  }
  
  public void set(Hospital hospital) {
    this.cached = hospital;
  }
}
