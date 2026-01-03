package hospital_managment.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import hospital_managment.domain.BaseEntity;
import hospital_managment.patterns.IdentityMap;
import hospital_managment.patterns.Query;

public interface Repository<T extends BaseEntity> {
    List<T> find(Query query, IdentityMap identityMap, Connection connection);
    void insert(T entity, Connection connection);
    void update(T entity, Connection connection);
    void delete(T entity, Connection connection);
    T mapResultSetToEntity(ResultSet rs);
    Class<T> getEntityType();
}
