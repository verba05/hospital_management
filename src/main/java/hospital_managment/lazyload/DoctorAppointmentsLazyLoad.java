package hospital_managment.lazyload;

import hospital_managment.domain.Appointment;
import hospital_managment.domain.Doctor;
import java.util.ArrayList;
import java.util.List;

public class DoctorAppointmentsLazyLoad {
  
  private final Doctor owner;
  private List<Appointment> cached;
  
  public DoctorAppointmentsLazyLoad(Doctor owner) {
    this.owner = owner;
  }
  
  public List<Appointment> get() {
    if (cached == null) {
      // TODO: Query database using owner.getId()
      cached = new ArrayList<>();
    }
    return cached;
  }
}
