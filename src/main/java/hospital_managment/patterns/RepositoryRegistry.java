package hospital_managment.patterns;

import java.util.HashMap;
import java.util.Map;

import hospital_managment.domain.BaseEntity;
import hospital_managment.repository.Repository;

public class RepositoryRegistry {

    private final Map<Class<?>, Repository<?>> repos = new HashMap<>();

    public <T extends BaseEntity> void register(Repository<T> repo) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) repo.getClass();
        repos.put(type, repo);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> Repository<T> getRepository(Class<T> type) {
        return (Repository<T>) repos.get(type);
    }
}
