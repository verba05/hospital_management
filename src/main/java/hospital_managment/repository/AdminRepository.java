package hospital_managment.repository;

import hospital_managment.domain.Admin;
import hospital_managment.domain.UserRole;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminRepository implements Repository<Admin> {

    @Override
    public List<Admin> find(Query query, IdentityMap identityMap, Connection connection) {
        List<Admin> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT u.id, u.name, u.surname, u.email, u.login, u.password_hash, u.role, u.email_verified, u.version, " +
            "h.id as hospital_id, h.hospital_name, h.street_address, h.city, h.postal_code, h.state_province, h.country, h.appointment_interval " +
            "FROM admins a " +
            "INNER JOIN users u ON a.admin_id = u.id " +
            "INNER JOIN hospitals h ON a.hospital = h.id"
        );

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
                
                Admin existing = identityMap.get(Admin.class, idLong);
                if (existing != null) {
                    result.add(existing);
                } else {
                    Admin admin = mapResultSetToEntity(rs);
                    identityMap.put(Admin.class, admin.getId() * 1L, admin);
                    result.add(admin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error querying users", e);
        }

        return result;
    }
    @Override
    public void insert(Admin entity, Connection connection) {
        String sqlUser = "INSERT INTO users (name, surname, email, login, password_hash, role, email_verified, version) VALUES (?, ?, ?, ?, ?, ?::role, ?, ?) RETURNING id";
        String sqlAdmin = "INSERT INTO admins (admin_id, hospital) VALUES (?, ?)";

        try {
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
                    entity.setId(rs.getInt(1));
                }

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }

            try (PreparedStatement stmt = connection.prepareStatement(sqlAdmin)) {
                stmt.setInt(1, entity.getId());
                stmt.setInt(2, entity.getHospital().getId());
                stmt.executeUpdate();
            }

            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting admin", e);
        }
    }

    @Override
    public void update(Admin entity, Connection connection) {
        String sqlUser = "UPDATE users SET name = ?, surname = ?, email = ?, login = ?, password_hash = ?, email_verified = ?, version = ? WHERE id = ?";
        String sqlAdmin = "UPDATE admins SET hospital = ? WHERE admin_id = ?";

        try {
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
            }

            try (PreparedStatement stmt = connection.prepareStatement(sqlAdmin)) {
                stmt.setInt(1, entity.getHospital().getId());
                stmt.setInt(2, entity.getId());
                stmt.executeUpdate();
            }

            entity.markSaved();

            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating admin", e);
        }
    }

    @Override
    public void delete(Admin entity, Connection connection) {
        String sqlAdmin = "DELETE FROM admins WHERE admin_id = ?";
        String sqlUser = "DELETE FROM users WHERE id = ?";

        try {
            try (PreparedStatement stmt = connection.prepareStatement(sqlAdmin)) {
                stmt.setInt(1, entity.getId());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = connection.prepareStatement(sqlUser)) {
                stmt.setInt(1, entity.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting admin", e);
        }
    }

    @Override
    public Admin mapResultSetToEntity(ResultSet rs) {
        try {
            Admin admin = new Admin();
            admin.setId(rs.getInt("id"));
            admin.setName(rs.getString("name"));
            admin.setSurname(rs.getString("surname"));
            admin.setEmail(rs.getString("email"));
            admin.setLogin(rs.getString("login"));
            admin.setPassword(rs.getString("password_hash"));
            admin.setRole(UserRole.valueOf(rs.getString("role")));
            
            try {
                Integer hospitalId = rs.getInt("hospital_id");
                if (!rs.wasNull()) {
                    admin.setHospitalId(hospitalId);
                }
            } catch (SQLException e) {
            }
            
            admin.markSaved();
            return admin;
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping admin", e);
        }
    }

    @Override
    public Class<Admin> getEntityType() {
        return Admin.class;
    }
    
}