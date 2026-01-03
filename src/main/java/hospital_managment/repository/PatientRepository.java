package hospital_managment.repository;

import hospital_managment.domain.Patient;
import hospital_managment.domain.UserRole;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientRepository implements Repository<Patient> {

    @Override
    public List<Patient> find(Query query, IdentityMap identityMap, Connection connection) {
        List<Patient> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE role = 'PATIENT'");

        if (!query.criteria().isEmpty()) {
            sql.append(" AND ");
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
                
                Patient existing = identityMap.get(Patient.class, idLong);
                if (existing != null) {
                    result.add(existing);
                } else {
                    Patient patient = mapResultSetToEntity(rs);
                    identityMap.put(Patient.class, patient.getId() * 1L, patient);
                    result.add(patient);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying patients", e);
        }

        return result;
    }

    @Override
    public void insert(Patient entity, Connection connection) {
        String sqlUser = "INSERT INTO users (name, surname, email, login, password_hash, role, email_verified, version) VALUES (?, ?, ?, ?, ?, ?::role, ?, ?) RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sqlUser)) {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getSurname());
            stmt.setString(3, entity.getEmail());
            stmt.setString(4, entity.getLogin());
            stmt.setString(5, entity.getPasswordHash());
            stmt.setString(6, entity.getRole().toString());
            stmt.setBoolean(7, entity.isEmailVerified());
            stmt.setLong(8, entity.getVersion());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt(1);
                entity.setId(userId);
            }

            entity.markSaved();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error inserting patient", e);
        }
    }

    @Override
    public void update(Patient entity, Connection connection) {
        String sqlUser = "UPDATE users SET name = ?, surname = ?, email = ?, login = ?, password_hash = ?, email_verified = ?, version = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sqlUser)) {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getSurname());
            stmt.setString(3, entity.getEmail());
            stmt.setString(4, entity.getLogin());
            stmt.setString(5, entity.getPasswordHash());
            stmt.setBoolean(6, entity.isEmailVerified());
            stmt.setLong(7, entity.getVersion());
            stmt.setInt(8, entity.getId());
            stmt.executeUpdate();

            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating patient", e);
        }
    }

    @Override
    public void delete(Patient entity, Connection connection) {
        String sqlUser = "DELETE FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sqlUser)) {
            stmt.setInt(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting patient", e);
        }
    }

    @Override
    public Patient mapResultSetToEntity(ResultSet rs) {
        try {
            UserRole role = UserRole.valueOf(rs.getString("role"));
            Patient patient = new Patient();
            patient.setId(rs.getInt("id"));
            patient.setName(rs.getString("name"));
            patient.setSurname(rs.getString("surname"));
            patient.setEmail(rs.getString("email"));
            patient.setLogin(rs.getString("login"));
            patient.setPassword(rs.getString("password_hash"));
            patient.setRole(role);
            
            try {
                patient.setEmailVerified(rs.getBoolean("email_verified"));
            } catch (SQLException e) {
                patient.setEmailVerified(false);
            }
            
            patient.markSaved();
            return patient;
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping patient", e);
        }
    }

    @Override
    public Class<Patient> getEntityType() {
        return Patient.class;
    }
}
