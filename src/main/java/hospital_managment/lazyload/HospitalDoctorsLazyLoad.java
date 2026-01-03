package hospital_managment.lazyload;

import hospital_managment.domain.Doctor;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.repository.DoctorRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HospitalDoctorsLazyLoad {
  
  private final Integer hospitalId;
  private List<Doctor> cached;
  
  public HospitalDoctorsLazyLoad(Integer hospitalId) {
    this.hospitalId = hospitalId;
  }
  
  public List<Doctor> get() {
    if (cached == null && hospitalId != null) {
      cached = new ArrayList<>();
      try (PreparedStatement stmt = UnitOfWorkContext.getConnection()
          .prepareStatement("SELECT * FROM doctors WHERE hospital_id = ?")) {
        stmt.setInt(1, hospitalId);
        ResultSet rs = stmt.executeQuery();
        DoctorRepository repo = (DoctorRepository) UnitOfWorkContext.getRegistry()
            .getRepository(Doctor.class);
        while (rs.next()) {
          Doctor doctor = repo.mapResultSetToEntity(rs);
          UnitOfWorkContext.getIdentityMap().put(Doctor.class, doctor.getId() * 1L, doctor);
          cached.add(doctor);
        }
      } catch (SQLException e) {
        throw new RuntimeException("Failed to load doctors for hospital " + hospitalId, e);
      }
    }
    return cached != null ? cached : new ArrayList<>();
  }
}
