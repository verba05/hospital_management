package hospital_managment.lazyload;

import hospital_managment.domain.Treatment;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.RepositoryRegistry;
import hospital_managment.repository.TreatmentRepository;
import hospital_managment.patterns.Query;
import java.util.List;

public class AppointmentTreatmentLazyLoad {
  
  private final Integer appointmentId;
  private Treatment cached;
  
  public AppointmentTreatmentLazyLoad(Integer appointmentId) {
    this.appointmentId = appointmentId;
  }
  
  public Treatment get() {
    if (cached == null && appointmentId != null) {
      TreatmentRepository repo = (TreatmentRepository) RepositoryRegistry.getInstance()
          .getRepository(Treatment.class);
      
      Query query = new Query()
          .where("appointment_id", Query.Operator.EQUALS, appointmentId)
          .limit(1);
      
      List<Treatment> treatments = repo.find(
          query, 
          UnitOfWorkContext.getIdentityMap(), 
          UnitOfWorkContext.getCurrent().getConnection()
      );
      
      if (!treatments.isEmpty()) {
        cached = treatments.get(0);
      }
    }
    return cached;
  }
  
  public void set(Treatment treatment) {
    this.cached = treatment;
  }
}
