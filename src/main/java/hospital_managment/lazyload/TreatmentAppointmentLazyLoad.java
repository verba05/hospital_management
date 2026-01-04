package hospital_managment.lazyload;

import hospital_managment.domain.Appointment;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.patterns.RepositoryRegistry;
import hospital_managment.repository.AppointmentRepository;
import hospital_managment.patterns.Query;
import java.util.List;

public class TreatmentAppointmentLazyLoad {
  
  private final Integer appointmentId;
  private Appointment cached;
  
  public TreatmentAppointmentLazyLoad(Integer appointmentId) {
    this.appointmentId = appointmentId;
  }
  
  public Appointment get() {
    if (cached == null && appointmentId != null) {
      AppointmentRepository repo = (AppointmentRepository) RepositoryRegistry.getInstance()
          .getRepository(Appointment.class);
      
      Query query = new Query()
          .where("id", Query.Operator.EQUALS, appointmentId)
          .limit(1);
      
      List<Appointment> appointments = repo.find(
          query, 
          UnitOfWorkContext.getIdentityMap(), 
          UnitOfWorkContext.getCurrent().getConnection()
      );
      
      if (!appointments.isEmpty()) {
        cached = appointments.get(0);
      }
    }
    return cached;
  }
  
  public void set(Appointment appointment) {
    this.cached = appointment;
  }
}
