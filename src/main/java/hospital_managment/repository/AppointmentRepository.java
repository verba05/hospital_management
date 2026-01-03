package hospital_managment.repository;

import hospital_managment.domain.Appointment;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;
import java.sql.*;
import java.util.*;

public class AppointmentRepository implements Repository<Appointment> {

    @Override
    public List<Appointment> find(Query query, IdentityMap identityMap, Connection connection) {
        List<Appointment> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM appointment");

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
                
                Appointment existing = identityMap.get(Appointment.class, idLong);
                if (existing != null) {
                    result.add(existing);
                } else {
                    Appointment appointment = mapResultSetToEntity(rs);
                    identityMap.put(Appointment.class, appointment.getId() * 1L, appointment);
                    result.add(appointment);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying appointments", e);
        }

        return result;
    }

    @Override
    public void insert(Appointment entity, Connection connection) {
        Integer patientId = entity.getPatientId();
        if (patientId == null || patientId == 0) {
            throw new IllegalArgumentException("Patient ID is required for appointment");
        }
        
        Integer doctorId = entity.getDoctorId();
        if (doctorId == null || doctorId == 0) {
            throw new IllegalArgumentException("Doctor ID is required for appointment");
        }
        
        if (entity.getDate() == null) {
            throw new IllegalArgumentException("Date is required for appointment");
        }
        
        if (entity.getTime() == null) {
            throw new IllegalArgumentException("Time is required for appointment");
        }
        
        if (entity.getStatus() == null) {
            throw new IllegalArgumentException("Status is required for appointment");
        }
        
        String sql = "INSERT INTO appointment (patient_id, doctor_id, start_time, end_time, status, doctor_notes, version) VALUES (?, ?, ?, ?, ?::appointment_status, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            
            Timestamp startTimestamp = Timestamp.valueOf(entity.getDate().atTime(entity.getTime()));
            stmt.setTimestamp(3, startTimestamp);
            
            Timestamp endTimestamp = Timestamp.valueOf(startTimestamp.toLocalDateTime().plusMinutes(30));
            stmt.setTimestamp(4, endTimestamp);
            
            String statusValue = entity.getStatus().name().toLowerCase();
            stmt.setString(5, statusValue);
            
            stmt.setString(6, entity.getNotes());
            stmt.setLong(7, entity.getVersion());
            
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int generatedId = keys.getInt(1);
                entity.setId(generatedId);
            }
            
            entity.markSaved();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error inserting appointment", e);
        }
    }

    @Override
    public void update(Appointment entity, Connection connection) {
        if (entity.getPatient() == null || entity.getPatient().getId() == 0) {
            throw new IllegalArgumentException("Patient is required for appointment");
        }
        if (entity.getDoctor() == null || entity.getDoctor().getId() == 0) {
            throw new IllegalArgumentException("Doctor is required for appointment");
        }
        if (entity.getDate() == null) {
            throw new IllegalArgumentException("Date is required for appointment");
        }
        if (entity.getTime() == null) {
            throw new IllegalArgumentException("Time is required for appointment");
        }
        if (entity.getStatus() == null) {
            throw new IllegalArgumentException("Status is required for appointment");
        }
        
        String sql = "UPDATE appointment SET patient_id = ?, doctor_id = ?, start_time = ?, end_time = ?, status = ?::appointment_status, doctor_notes = ?, version = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, entity.getPatient().getId());
            stmt.setInt(2, entity.getDoctor().getId());
            
            Timestamp startTimestamp = Timestamp.valueOf(entity.getDate().atTime(entity.getTime()));
            stmt.setTimestamp(3, startTimestamp);
            
            Timestamp endTimestamp = Timestamp.valueOf(startTimestamp.toLocalDateTime().plusMinutes(30));
            stmt.setTimestamp(4, endTimestamp);
            
            stmt.setString(5, entity.getStatus().name().toLowerCase());
            stmt.setString(6, entity.getNotes());
            stmt.setLong(7, entity.getVersion());
            stmt.setInt(8, entity.getId());
            
            stmt.executeUpdate();
            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating appointment", e);
        }
    }

    @Override
    public void delete(Appointment entity, Connection connection) {
        String sql = "DELETE FROM appointment WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting appointment", e);
        }
    }

    @Override
    public Appointment mapResultSetToEntity(ResultSet rs) {
        try {
            Appointment appointment = new Appointment();
            appointment.setId(rs.getInt("id"));
            
            Timestamp startTime = rs.getTimestamp("start_time");
            if (startTime != null) {
                appointment.setDate(startTime.toLocalDateTime().toLocalDate());
                appointment.setTime(startTime.toLocalDateTime().toLocalTime());
            }
            
            String status = rs.getString("status");
            if (status != null) {
                appointment.setStatus(Appointment.AppointmentStatus.valueOf(status.toUpperCase()));
            }
            
            String notes = rs.getString("doctor_notes");
            if (notes != null) {
                appointment.setNotes(notes);
            }
            
            try {
                Integer patientId = rs.getInt("patient_id");
                if (!rs.wasNull()) {
                    appointment.setPatientId(patientId);
                }
            } catch (SQLException e) {
            }
            
            try {
                Integer doctorId = rs.getInt("doctor_id");
                if (!rs.wasNull()) {
                    appointment.setDoctorId(doctorId);
                }
            } catch (SQLException e) {
            }
            
            appointment.setTreatmentByAppointmentId(appointment.getId());
            
            appointment.markSaved();
            return appointment;
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping appointment", e);
        }
    }

    @Override
    public Class<Appointment> getEntityType() {
        return Appointment.class;
    }
}
