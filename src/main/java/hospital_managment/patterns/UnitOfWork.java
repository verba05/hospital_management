package hospital_managment.patterns;

import hospital_managment.domain.BaseEntity;
import hospital_managment.repository.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class UnitOfWork {

    private final Connection connection;
    private final RepositoryRegistry registry;
    private final IdentityMap identityMap = new IdentityMap();

    private final Map<Class<? extends BaseEntity>, List<BaseEntity>> newEntities = new HashMap<>();
    private final Map<Class<? extends BaseEntity>, List<BaseEntity>> dirtyEntities = new HashMap<>();
    private final Map<Class<? extends BaseEntity>, List<BaseEntity>> removedEntities = new HashMap<>();

    public UnitOfWork(Connection connection, RepositoryRegistry registry) {
        this.connection = connection;
        this.registry = registry;
        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to start transaction", e);
        }
    }

    public IdentityMap getIdentityMap() {
        return identityMap;
    }

    public RepositoryRegistry getRegistry() {
        return registry;
    }

    public Connection getConnection() {
        return connection;
    }

    public void registerNew(BaseEntity entity) {
        newEntities.computeIfAbsent(entity.getClass(), k -> new ArrayList<>()).add(entity);
    }

    public void registerDirty(BaseEntity entity) {
      //  entity.incrementVersion();
        dirtyEntities.computeIfAbsent(entity.getClass(), k -> new ArrayList<>()).add(entity);
    }

    public void registerRemoved(BaseEntity entity) {
        removedEntities.computeIfAbsent(entity.getClass(), k -> new ArrayList<>()).add(entity);
    }

    public void commit() {
        try {
            insertNew();
            updateDirty();
            deleteRemoved();
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Rollback failed", rollbackEx);
            }
            throw new RuntimeException("UnitOfWork commit failed, rolled back.", e);
        } finally {
            clearAll();
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Rollback failed", e);
        } finally {
            clearAll();
        }
    }

    private void clearAll() {
        newEntities.clear();
        dirtyEntities.clear();
        removedEntities.clear();
        identityMap.clear();
    }

    private <T extends BaseEntity> void insertNew() throws Exception {
        Class<?>[] insertionOrder = {
            Class.forName("hospital_managment.domain.Hospital"),
            Class.forName("hospital_managment.domain.User"),
            Class.forName("hospital_managment.domain.Patient"),
            Class.forName("hospital_managment.domain.Doctor"),
            Class.forName("hospital_managment.domain.Admin"),
            Class.forName("hospital_managment.domain.EmailVerificationToken"),
            Class.forName("hospital_managment.domain.Appointment"),
            Class.forName("hospital_managment.domain.Treatment")
        };
        
        for (Class<?> orderedType : insertionOrder) {
            if (newEntities.containsKey(orderedType)) {
                Class<T> type = (Class<T>) orderedType;
                
                Repository<T> repo = registry.getRepository(type);
                if (repo == null) {
                    throw new RuntimeException("No repository registered for type: " + type.getSimpleName());
                }

                for (BaseEntity e : newEntities.get(type)) {
                    T entity = type.cast(e);
                    repo.insert(entity, connection);
                    entity.markSaved();
                    identityMap.put(type, entity.getId() * 1L, entity);
                }
            }
        }
        
        for (var entry : newEntities.entrySet()) {
            Class<T> type = (Class<T>) entry.getKey();
            boolean alreadyProcessed = false;
            for (Class<?> orderedType : insertionOrder) {
                if (orderedType.equals(type)) {
                    alreadyProcessed = true;
                    break;
                }
            }
            
            if (!alreadyProcessed) {
                Repository<T> repo = registry.getRepository(type);
                if (repo == null) {
                    throw new RuntimeException("No repository registered for type: " + type.getSimpleName());
                }
                
                for (BaseEntity e : entry.getValue()) {
                    T entity = type.cast(e);
                    repo.insert(entity, connection);
                    entity.markSaved();
                    identityMap.put(type, entity.getId() * 1L, entity);
                }
            }
        }
    }

    private <T extends BaseEntity> void updateDirty() throws Exception {
        for (var entry : dirtyEntities.entrySet()) {
            Class<T> type = (Class<T>) entry.getKey();
            Repository<T> repo = registry.getRepository(type);

            for (BaseEntity e : entry.getValue()) {
                T entity = type.cast(e);
                repo.update(entity, connection);
                entity.markSaved();
                identityMap.put(type, entity.getId() * 1L, entity);
            }
        }
    }

    private <T extends BaseEntity> void deleteRemoved() throws Exception {
        for (var entry : removedEntities.entrySet()) {
            Class<T> type = (Class<T>) entry.getKey();
            Repository<T> repo = registry.getRepository(type);

            for (BaseEntity e : entry.getValue()) {
                T entity = type.cast(e);
                repo.delete(entity, connection);
                identityMap.remove(type, entity.getId() * 1L);
            }
        }
    }
}
