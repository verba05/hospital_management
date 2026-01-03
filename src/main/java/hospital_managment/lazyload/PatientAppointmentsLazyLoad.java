package hospital_managment.lazyload;

import hospital_managment.domain.Appointment;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.repository.AppointmentRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientAppointmentsLazyLoad {
  
  private final Integer patientId;
  private List<Appointment> cached;
  
  public PatientAppointmentsLazyLoad(Integer patientId) {
    this.patientId = patientId;
  }
  
  public List<Appointment> get() {
    if (cached == null && patientId != null) {
      cached = new ArrayList<>();
      try (PreparedStatement stmt = UnitOfWorkContext.getConnection()
          .prepareStatement("SELECT * FROM appoinment WHERE patient_id = ?")) {
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        AppointmentRepository repo = (AppointmentRepository) UnitOfWorkContext.getRegistry()
            .getRepository(Appointment.class);
        while (rs.next()) {
          Appointment app = repo.mapResultSetToEntity(rs);
          UnitOfWorkContext.getIdentityMap().put(Appointment.class, app.getId() * 1L, app);
          cached.add(app);
        }
      } catch (SQLException e) {
        throw new RuntimeException("Failed to load appointments for patient " + patientId, e);
      }
    }
    return cached != null ? cached : new ArrayList<>();
  }
}
