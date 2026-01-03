package hospital_managment.repository;

import hospital_managment.domain.Treatment;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TreatmentRepository implements Repository<Treatment> {

    @Override
    public List<Treatment> find(Query query, IdentityMap identityMap, Connection connection) {
        List<Treatment> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM treatment");

        if (!query.criteria().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < query.criteria().size(); i++) {
                Query.Criteria c = query.criteria().get(i);
                sql.append(c.field).append(" ").append(c.operator.toSql()).append(" ?");
                if (i < query.criteria().size() - 1) sql.append(" AND ");
            }
        }

        if (!query.orders().isEmpty()) {
            sql.append(" ORDER BY ").append(String.join(", ", query.orders()));
        }

        if (query.limit() != null) sql.append(" LIMIT ").append(query.limit());
        if (query.offset() != null) sql.append(" OFFSET ").append(query.offset());

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < query.criteria().size(); i++) {
                stmt.setObject(i + 1, query.criteria().get(i).value);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                Long idLong = (long) id;
                
                // Check if entity already exists in IdentityMap
                Treatment existing = identityMap.get(Treatment.class, idLong);
                if (existing != null) {
                    result.add(existing);
                } else {
                    Treatment treatment = mapResultSetToEntity(rs);
                    identityMap.put(Treatment.class, treatment.getId() * 1L, treatment);
                    result.add(treatment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying treatments", e);
        }

        return result;
    }

    @Override
    public void insert(Treatment entity, Connection connection) {
        String sql = "INSERT INTO treatment (patient_id, doctor_id, instructions, appointment_id, medication_names, created_at, version) VALUES (?, ?, ?, ?, ?::text[], ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int patientId = entity.getAppointment().getPatient().getId();
            stmt.setInt(1, patientId);
            
            Integer doctorId = entity.getAppointment().getDoctor() != null ? 
                entity.getAppointment().getDoctor().getId() : null;
            stmt.setObject(2, doctorId);
            
            stmt.setString(3, entity.getInstructions());
            stmt.setInt(4, entity.getAppointment().getId());
            
            String medications = null;
            if (entity.getMedications() != null && !entity.getMedications().isEmpty()) {
                medications = "{" + String.join(",", entity.getMedications()) + "}";
            }
            stmt.setString(5, medications);
            
            stmt.setTimestamp(6, entity.getCreatedAt() != null ? Timestamp.valueOf(entity.getCreatedAt()) : null);
            stmt.setLong(7, entity.getVersion());
            
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                entity.setId(keys.getInt(1));
            }
            
            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting treatment", e);
        }
    }

    @Override
    public void update(Treatment entity, Connection connection) {
        String sql = "UPDATE treatment SET patient_id = ?, instructions = ?, appointment_id = ?, medication_names = ?::text[], created_at = ?, version = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int patientId = entity.getAppointment().getPatient().getId();
            stmt.setInt(1, patientId);
            
            stmt.setString(2, entity.getInstructions());
            stmt.setInt(3, entity.getAppointment().getId());
            
            String medications = null;
            if (entity.getMedications() != null && !entity.getMedications().isEmpty()) {
                medications = "{" + String.join(",", entity.getMedications()) + "}";
            }
            stmt.setString(4, medications);
            
            stmt.setTimestamp(5, entity.getCreatedAt() != null ? Timestamp.valueOf(entity.getCreatedAt()) : null);
            stmt.setLong(6, entity.getVersion());
            stmt.setInt(7, entity.getId());
            
            stmt.executeUpdate();
            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating treatment", e);
        }
    }

    @Override
    public void delete(Treatment entity, Connection connection) {
        String sql = "DELETE FROM treatment WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting treatment", e);
        }
    }

    @Override
    public Treatment mapResultSetToEntity(ResultSet rs) {
        try {
            Treatment treatment = new Treatment();
            treatment.setId(rs.getInt("id"));
            treatment.setInstructions(rs.getString("instructions"));
            
            try {
                Integer patientId = rs.getInt("patient_id");
                if (!rs.wasNull()) {
                    treatment.setPatientId(patientId);
                }
            } catch (SQLException e) {
            }
            
            try {
                Integer appointmentId = rs.getInt("appointment_id");
                if (!rs.wasNull()) {
                    treatment.setAppointmentId(appointmentId);
                }
            } catch (SQLException e) {
            }
            
            String medications = rs.getString("medication_names");
            if (medications != null && !medications.isEmpty()) {
                String cleaned = medications;
                if (cleaned.startsWith("{")) {
                    cleaned = cleaned.substring(1);
                }
                if (cleaned.endsWith("}")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 1);
                }
                
                for (String med : cleaned.split(",")) {
                    String trimmed = med.trim();
                    if (!trimmed.isEmpty()) {
                        treatment.addMedication(trimmed);
                    }
                }
            }
            
            try {
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    treatment.setCreatedAt(createdAt.toLocalDateTime());
                }
            } catch (SQLException e) {
            }
            
            treatment.markSaved();
            return treatment;
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping treatment", e);
        }
    }

    @Override
    public Class<Treatment> getEntityType() {
        return Treatment.class;
    }
}
