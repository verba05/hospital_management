package hospital_managment.repository;

import hospital_managment.domain.Doctor;
import hospital_managment.domain.TimeRange;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class DoctorRepository implements Repository<Doctor> {
    
    @Override
    public List<Doctor> find(Query query, IdentityMap identityMap, Connection connection) {
        List<Doctor> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT d.*, u.name, u.surname, u.email, u.login, u.password_hash, u.role, s.name as specialty_name FROM doctors d JOIN users u ON d.doctor_id = u.id LEFT JOIN specialties s ON d.specialty_id = s.id");

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
                int id = rs.getInt("doctor_id");
                Long idLong = (long) id;
                
                Doctor existing = identityMap.get(Doctor.class, idLong);
                if (existing != null) {
                    result.add(existing);
                } else {
                    Doctor doctor = mapResultSetToEntity(rs);
                    identityMap.put(Doctor.class, doctor.getId() * 1L, doctor);
                    result.add(doctor);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying doctors", e);
        }

        return result;
    }

    @Override
    public void insert(Doctor entity, Connection connection) {
        String userSql = "INSERT INTO users (name, surname, password_hash, role, email, login, email_verified, version) VALUES (?, ?, ?, ?::role, ?, ?, ?, ?) RETURNING id";
        
        try (PreparedStatement userStmt = connection.prepareStatement(userSql)) {
            userStmt.setString(1, entity.getName());
            userStmt.setString(2, entity.getSurname());
            userStmt.setString(3, entity.getPasswordHash());
            userStmt.setString(4, entity.getRole().name());
            userStmt.setString(5, entity.getEmail());
            userStmt.setString(6, entity.getLogin());
            userStmt.setBoolean(7, entity.isEmailVerified());
            userStmt.setLong(8, entity.getVersion());
            
            ResultSet rs = userStmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt(1);
                entity.setId(userId);
                
                Integer specialtyId = null;
                if (entity.getSpecialty() != null) {
                    String specialtyQuery = "SELECT id FROM specialties WHERE name = ?";
                    try (PreparedStatement specialtyStmt = connection.prepareStatement(specialtyQuery)) {
                        specialtyStmt.setString(1, entity.getSpecialty());
                        ResultSet specialtyRs = specialtyStmt.executeQuery();
                        if (specialtyRs.next()) {
                            specialtyId = specialtyRs.getInt("id");
                        }
                    }
                }
                
                String doctorSql = "INSERT INTO doctors (doctor_id, office, specialty_id, hospital_id, version) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement doctorStmt = connection.prepareStatement(doctorSql)) {
                    doctorStmt.setInt(1, userId);
                    doctorStmt.setInt(2, entity.getOffice());
                    if (specialtyId != null) {
                        doctorStmt.setInt(3, specialtyId);
                    } else {
                        doctorStmt.setNull(3, Types.INTEGER);
                    }
                    doctorStmt.setInt(4, entity.getHospital().getId());
                    doctorStmt.setLong(5, entity.getVersion());
                    doctorStmt.executeUpdate();
                }
                
                if (entity.scheduleStartTime != null && entity.scheduleEndTime != null && entity.scheduleWorkingDays != null && !entity.scheduleWorkingDays.isEmpty()) {
                    createDoctorSchedule(userId, entity.scheduleStartTime, entity.scheduleEndTime, entity.scheduleWorkingDays, connection);
                }
            }
            
            entity.markSaved();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error inserting doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Doctor entity, Connection connection) {
        String userSql = "UPDATE users SET name = ?, surname = ?, password_hash = ?, role = ?::role, email = ?, login = ?, email_verified = ?, version = ? WHERE id = ?";
        
        try (PreparedStatement userStmt = connection.prepareStatement(userSql)) {
            userStmt.setString(1, entity.getName());
            userStmt.setString(2, entity.getSurname());
            userStmt.setString(3, entity.getPasswordHash());
            userStmt.setString(4, entity.getRole().name());
            userStmt.setString(5, entity.getEmail());
            userStmt.setString(6, entity.getLogin());
            userStmt.setBoolean(7, entity.isEmailVerified());
            userStmt.setLong(8, entity.getVersion());
            userStmt.setInt(9, entity.getId());
            userStmt.executeUpdate();
            
            Integer specialtyId = null;
            if (entity.getSpecialty() != null) {
                String specialtyQuery = "SELECT id FROM specialties WHERE name = ?";
                try (PreparedStatement specialtyStmt = connection.prepareStatement(specialtyQuery)) {
                    specialtyStmt.setString(1, entity.getSpecialty());
                    ResultSet specialtyRs = specialtyStmt.executeQuery();
                    if (specialtyRs.next()) {
                        specialtyId = specialtyRs.getInt("id");
                    }
                }
            }
            
            String doctorSql = "UPDATE doctors SET office = ?, specialty_id = ?, hospital_id = ?, version = ? WHERE doctor_id = ?";
            try (PreparedStatement doctorStmt = connection.prepareStatement(doctorSql)) {
                doctorStmt.setInt(1, entity.getOffice());
                if (specialtyId != null) {
                    doctorStmt.setInt(2, specialtyId);
                } else {
                    doctorStmt.setNull(2, Types.INTEGER);
                }
                doctorStmt.setInt(3, entity.getHospital().getId());
                doctorStmt.setLong(4, entity.getVersion());
                doctorStmt.setInt(5, entity.getId());
                doctorStmt.executeUpdate();
            }
            
            entity.markSaved();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Doctor entity, Connection connection) {
        String doctorSql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (PreparedStatement doctorStmt = connection.prepareStatement(doctorSql)) {
            doctorStmt.setInt(1, entity.getId());
            doctorStmt.executeUpdate();
            
            String userSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement userStmt = connection.prepareStatement(userSql)) {
                userStmt.setInt(1, entity.getId());
                userStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public Doctor mapResultSetToEntity(ResultSet rs) {
        try {
            Doctor doctor = new Doctor();
            doctor.setId(rs.getInt("doctor_id"));
            doctor.setName(rs.getString("name"));
            doctor.setSurname(rs.getString("surname"));
            doctor.setEmail(rs.getString("email"));
            doctor.setLogin(rs.getString("login"));
            doctor.setPassword(rs.getString("password_hash"));
            doctor.setRole(hospital_managment.domain.UserRole.valueOf(rs.getString("role")));
            doctor.setOffice(rs.getInt("office"));
            
            String specialtyName = rs.getString("specialty_name");
            doctor.setSpecialty(specialtyName);
            
            try {
                doctor.setEmailVerified(rs.getBoolean("email_verified"));
            } catch (SQLException e) {
                doctor.setEmailVerified(false);
            }
            
            try {
                Integer hospitalId = rs.getInt("hospital_id");
                if (!rs.wasNull()) {
                    doctor.setHospitalId(hospitalId);
                }
            } catch (SQLException e) {
            }
            
            doctor.markSaved();
            return doctor;
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping doctor", e);
        }
    }

    @Override
    public Class<Doctor> getEntityType() {
        return Doctor.class;
    }

    public List<String> getAllSpecialties(IdentityMap identityMap, Connection connection) {
        List<String> specialties = new ArrayList<>();
        String sql = "SELECT name FROM specialties ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String specialty = rs.getString("name");
                if (specialty != null && !specialty.trim().isEmpty()) {
                    specialties.add(specialty);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying specialties: " + e.getMessage(), e);
        }
        
        return specialties;
    }

    public void createDoctorSchedule(int doctorId, String startTime, String endTime, List<Integer> workingDays, Connection connection) {
        // Build simple JSON schedule object
        // Format: {"start":"09:00", "end":"17:00", "days":[1,2,3,4,5]}
        StringBuilder scheduleJson = new StringBuilder("{");
        scheduleJson.append("\"start\":\"").append(startTime).append("\",");
        scheduleJson.append("\"end\":\"").append(endTime).append("\",");
        scheduleJson.append("\"days\":[");
        for (int i = 0; i < workingDays.size(); i++) {
            scheduleJson.append(workingDays.get(i));
            if (i < workingDays.size() - 1) {
                scheduleJson.append(",");
            }
        }
        scheduleJson.append("]}");
        
        String sql = "INSERT INTO doctor_schedules (doctor_id, schedule, version) VALUES (?, ?::jsonb, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setString(2, scheduleJson.toString());
            stmt.setLong(3, 0L);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating doctor schedule: " + e.getMessage(), e);
        }
    }
    
    public void loadDoctorSchedule(Doctor doctor, Connection connection) {
        if (doctor == null || doctor.getId() <= 0) {
            return;
        }
        
        String sql = "SELECT schedule FROM doctor_schedules WHERE doctor_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, doctor.getId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String scheduleJson = rs.getString("schedule");
                
                if (scheduleJson != null && !scheduleJson.trim().isEmpty()) {
                    EnumMap<DayOfWeek, TimeRange> weekSchedule = parseScheduleJson(scheduleJson);
                    doctor.setWeekSchedule(weekSchedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private EnumMap<DayOfWeek, TimeRange> parseScheduleJson(String json) {
        EnumMap<DayOfWeek, TimeRange> schedule = new EnumMap<>(DayOfWeek.class);
        
        try {
            String startTime = extractJsonValue(json, "start");
            String endTime = extractJsonValue(json, "end");
            String daysStr = extractJsonArray(json, "days");
            
            if (startTime == null || endTime == null || daysStr == null) {
                return schedule;
            }
            
            LocalTime start = LocalTime.parse(startTime.trim());
            LocalTime end = LocalTime.parse(endTime.trim());
            TimeRange timeRange = new TimeRange(start, end);
            
            String cleanDays = daysStr.replaceAll("[\\[\\]\\s]", "");
            String[] dayNumbers = cleanDays.split(",");
            
            for (String dayStr : dayNumbers) {
                if (dayStr.trim().isEmpty()) continue;
                
                int dayNum = Integer.parseInt(dayStr.trim());
                DayOfWeek day = DayOfWeek.of(dayNum);
                schedule.put(day, timeRange);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return schedule;
    }
    
    private String extractJsonValue(String json, String key) {
        int startIdx = json.indexOf("\"" + key + "\"");
        if (startIdx == -1) {
            return null;
        }
        
        int colonIdx = json.indexOf(":", startIdx);
        if (colonIdx == -1) return null;
        
        // Skip whitespace and opening quote
        int valueStart = colonIdx + 1;
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
            return null;
        }
        valueStart++;
        
        int valueEnd = json.indexOf("\"", valueStart);
        if (valueEnd == -1) return null;
        
        return json.substring(valueStart, valueEnd);
    }
    
    private String extractJsonArray(String json, String key) {
        int startIdx = json.indexOf("\"" + key + "\"");
        if (startIdx == -1) {
            return null;
        }
        
        int colonIdx = json.indexOf(":", startIdx);
        if (colonIdx == -1) return null;
        
        int arrayStart = json.indexOf("[", colonIdx);
        if (arrayStart == -1) return null;
        
        int arrayEnd = json.indexOf("]", arrayStart);
        if (arrayEnd == -1) return null;
        
        return json.substring(arrayStart, arrayEnd + 1);
    }
}

