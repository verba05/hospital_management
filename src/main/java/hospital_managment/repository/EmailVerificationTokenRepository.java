package hospital_managment.repository;

import hospital_managment.domain.EmailVerificationToken;
import hospital_managment.domain.User;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmailVerificationTokenRepository implements Repository<EmailVerificationToken> {

    @Override
    public Class<EmailVerificationToken> getEntityType() {
        return EmailVerificationToken.class;
    }

    @Override
    public List<EmailVerificationToken> find(Query query, IdentityMap identityMap, Connection connection) {
        List<EmailVerificationToken> tokens = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM email_verification_tokens");
        
        if (!query.criteria().isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < query.criteria().size(); i++) {
                Query.Criteria c = query.criteria().get(i);
                sql.append(c.field).append(" ").append(c.operator.toSql()).append(" ?");
                if (i < query.criteria().size() - 1) sql.append(" AND ");
            }
        }
        
        if (query.limit() != null) {
            sql.append(" LIMIT ").append(query.limit());
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < query.criteria().size(); i++) {
                stmt.setObject(i + 1, query.criteria().get(i).value);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                Long idLong = (long) id;
                
                EmailVerificationToken existing = identityMap.get(EmailVerificationToken.class, idLong);
                if (existing != null) {
                    tokens.add(existing);
                } else {
                    EmailVerificationToken token = mapResultSetToEntity(rs);
                    tokens.add(token);
                    identityMap.put(EmailVerificationToken.class, token.getId() * 1L, token);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find email verification tokens", e);
        }
        
        return tokens;
    }

    public EmailVerificationToken findByToken(String token, Connection connection) {
        Query query = new Query()
            .where("token", Query.Operator.EQUALS, token)
            .limit(1);
        
        List<EmailVerificationToken> tokens = find(query, new IdentityMap(), connection);
        return tokens.isEmpty() ? null : tokens.get(0);
    }

    public List<EmailVerificationToken> findByUserId(int userId, Connection connection) {
        Query query = new Query()
            .where("user_id", Query.Operator.EQUALS, userId);
        
        return find(query, new IdentityMap(), connection);
    }

    public void markAsUsed(EmailVerificationToken token, Connection connection) {
        String sql = "UPDATE email_verification_tokens SET used = true WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, token.getId());
            stmt.executeUpdate();
            token.setUsed(true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark token as used", e);
        }
    }

    @Override
    public void insert(EmailVerificationToken token, Connection connection) {
        String sql = "INSERT INTO email_verification_tokens (user_id, token, expiry_date, used) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, token.getUser().getId());
            stmt.setString(2, token.getToken());
            stmt.setTimestamp(3, Timestamp.valueOf(token.getExpiryDate()));
            stmt.setBoolean(4, token.isUsed());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                token.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert email verification token", e);
        }
    }

    @Override
    public void update(EmailVerificationToken token, Connection connection) {
        String sql = "UPDATE email_verification_tokens SET user_id = ?, token = ?, expiry_date = ?, used = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, token.getUser().getId());
            stmt.setString(2, token.getToken());
            stmt.setTimestamp(3, Timestamp.valueOf(token.getExpiryDate()));
            stmt.setBoolean(4, token.isUsed());
            stmt.setInt(5, token.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update email verification token", e);
        }
    }

    @Override
    public void delete(EmailVerificationToken token, Connection connection) {
        String sql = "DELETE FROM email_verification_tokens WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, token.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete email verification token", e);
        }
    }

    @Override
    public EmailVerificationToken mapResultSetToEntity(ResultSet rs) {
        try {
            EmailVerificationToken token = new EmailVerificationToken();
            token.setId(rs.getInt("id"));
            token.setToken(rs.getString("token"));
            token.setExpiryDate(rs.getTimestamp("expiry_date").toLocalDateTime());
            token.setUsed(rs.getBoolean("used"));
            
            User user = new User();
            user.setId(rs.getInt("user_id"));
            token.setUser(user);
            
            return token;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to map result set to EmailVerificationToken", e);
        }
    }
}
