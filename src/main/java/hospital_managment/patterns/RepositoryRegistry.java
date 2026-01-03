package hospital_managment.patterns;

import java.util.HashMap;
import java.util.Map;

import hospital_managment.domain.BaseEntity;
import hospital_managment.repository.Repository;

public class RepositoryRegistry {

    private static final RepositoryRegistry INSTANCE = new RepositoryRegistry();
    private final Map<Class<?>, Repository<?>> repos = new HashMap<>();

    private RepositoryRegistry() {
    }

    public static RepositoryRegistry getInstance() {
        return INSTANCE;
    }

    public <T extends BaseEntity> void register(Repository<T> repo) {
        Class<T> entityType = repo.getEntityType();
        repos.put(entityType, repo);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> Repository<T> getRepository(Class<T> entityType) {
        return (Repository<T>) repos.get(entityType);
    }
}
