package hospital_managment.repository;

import hospital_managment.domain.Hospital;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HospitalRepository implements Repository<Hospital> {

    @Override
    public List<Hospital> find(Query query, IdentityMap identityMap, Connection connection) {
        List<Hospital> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM hospitals");

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
                
                Hospital existing = identityMap.get(Hospital.class, idLong);
                if (existing != null) {
                    result.add(existing);
                } else {
                    Hospital hospital = mapResultSetToEntity(rs);
                    identityMap.put(Hospital.class, hospital.getId() * 1L, hospital);
                    result.add(hospital);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying hospitals", e);
        }

        return result;
    }

    @Override
    public void insert(Hospital entity, Connection connection) {
        String sql = "INSERT INTO hospitals (hospital_name, street_address, city, state_province, postal_code, country, appointment_interval, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getStreetAddress());
            stmt.setString(3, entity.getCity());
            stmt.setString(4, entity.getStateProvince());
            stmt.setString(5, entity.getPostalCode());
            stmt.setString(6, entity.getCountry());
            stmt.setInt(7, entity.getAppointmentIntervalMinutes());
            stmt.setLong(8, entity.getVersion());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                entity.setId(keys.getInt(1));
            }

            entity.markSaved();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error inserting hospital: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Hospital entity, Connection connection) {
        String sql = "UPDATE hospitals SET hospital_name = ?, street_address = ?, city = ?, state_province = ?, postal_code = ?, country = ?, appointment_interval = ?, version = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getStreetAddress());
            stmt.setString(3, entity.getCity());
            stmt.setString(4, entity.getStateProvince());
            stmt.setString(5, entity.getPostalCode());
            stmt.setString(6, entity.getCountry());
            stmt.setInt(7, entity.getAppointmentIntervalMinutes());
            stmt.setLong(8, entity.getVersion());
            stmt.setInt(9, entity.getId());
            stmt.executeUpdate();

            entity.markSaved();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating hospital: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Hospital entity, Connection connection) {
        String sql = "DELETE FROM hospitals WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting hospital", e);
        }
    }

    @Override
    public Hospital mapResultSetToEntity(ResultSet rs) {
        try {
            Hospital hospital = new Hospital();
            hospital.setId(rs.getInt("id"));
            hospital.setName(rs.getString("hospital_name"));
            hospital.setStreetAddress(rs.getString("street_address"));
            hospital.setCity(rs.getString("city"));
            hospital.setStateProvince(rs.getString("state_province"));
            hospital.setPostalCode(rs.getString("postal_code"));
            hospital.setCountry(rs.getString("country"));
            hospital.setAppointmentIntervalMinutes(rs.getInt("appointment_interval"));
            
            hospital.markSaved();
            return hospital;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error mapping hospital: " + e.getMessage(), e);
        }
    }

    @Override
    public Class<Hospital> getEntityType() {
        return Hospital.class;
    }
}
