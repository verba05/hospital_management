package hospital_managment.lazyload;

import hospital_managment.domain.Treatment;
import hospital_managment.patterns.UnitOfWorkContext;
import hospital_managment.repository.TreatmentRepository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientTreatmentsLazyLoad {
  
  private final Integer patientId;
  private List<Treatment> cached;
  
  public PatientTreatmentsLazyLoad(Integer patientId) {
    this.patientId = patientId;
  }
  
  public List<Treatment> get() {
    if (cached == null && patientId != null) {
      cached = new ArrayList<>();
      try (PreparedStatement stmt = UnitOfWorkContext.getConnection()
          .prepareStatement("SELECT * FROM treatment WHERE patient_id = ?")) {
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        TreatmentRepository repo = (TreatmentRepository) UnitOfWorkContext.getRegistry()
            .getRepository(Treatment.class);
        while (rs.next()) {
          Treatment treatment = repo.mapResultSetToEntity(rs);
          UnitOfWorkContext.getIdentityMap().put(Treatment.class, treatment.getId() * 1L, treatment);
          cached.add(treatment);
        }
      } catch (SQLException e) {
        throw new RuntimeException("Failed to load treatments for patient " + patientId, e);
      }
    }
    return cached != null ? cached : new ArrayList<>();
  }
}
