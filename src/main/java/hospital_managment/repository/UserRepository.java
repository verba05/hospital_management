package hospital_managment.repository;

import hospital_managment.domain.User;
import hospital_managment.domain.UserRole;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements Repository<User> {

    @Override
    public List<User> find(Query query, IdentityMap identityMap, Connection connection) {
        List<User> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users");

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
                
                User existing = identityMap.get(User.class, idLong);
                if (existing != null) {
                    result.add(existing);
                } else {
                    User user = mapResultSetToEntity(rs);
                    identityMap.put(User.class, user.getId() * 1L, user);
                    result.add(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying users", e);
        }

        return result;
    }

    @Override
    public void insert(User entity, Connection connection) {
        String sql = "INSERT INTO users (name, surname, password_hash, role, email, login, email_verified, version) VALUES (?, ?, ?, ?::role, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getSurname());
            stmt.setString(3, entity.getPasswordHash());
            stmt.setString(4, entity.getRole().name());
            stmt.setString(5, entity.getEmail());
            stmt.setString(6, entity.getLogin());
            stmt.setBoolean(7, entity.isEmailVerified());
            stmt.setLong(8, entity.getVersion());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                entity.setId(rs.getInt(1));
            }
            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user", e);
        }
    }

    @Override
    public void update(User entity, Connection connection) {
        String sql = "UPDATE users SET name = ?, surname = ?, password_hash = ?, role = ?::role, email = ?, login = ?, email_verified = ?, version = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getSurname());
            stmt.setString(3, entity.getPasswordHash());
            stmt.setString(4, entity.getRole().name());
            stmt.setString(5, entity.getEmail());
            stmt.setString(6, entity.getLogin());
            stmt.setBoolean(7, entity.isEmailVerified());
            stmt.setLong(8, entity.getVersion());
            stmt.setInt(9, entity.getId());
            stmt.executeUpdate();
            entity.markSaved();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public void delete(User entity, Connection connection) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    public User mapResultSetToEntity(ResultSet rs) {
        try {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setSurname(rs.getString("surname"));
            user.setPassword(rs.getString("password_hash"));
            user.setRole(UserRole.valueOf(rs.getString("role")));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            
            try {
                user.setEmailVerified(rs.getBoolean("email_verified"));
            } catch (SQLException e) {
                user.setEmailVerified(false);
            }
            
            user.markSaved();
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping user from result set", e);
        }
    }

    @Override
    public Class<User> getEntityType() {
        return User.class;
    }
}
