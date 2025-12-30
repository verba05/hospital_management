package hospital_managment.patterns;

import hospital_managment.domain.BaseEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IdentityMap {

    private static class Key {
        private final Class<? extends BaseEntity> type;
        private final Long id;

        public Key(Class<? extends BaseEntity> type, Long id) {
            this.type = type;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return type.equals(key.type) && id.equals(key.id);
        }

        @Override
        public int hashCode() {
            return 31 * type.hashCode() + id.hashCode();
        }
    }

    private final Map<Key, BaseEntity> map = new ConcurrentHashMap<>();

    public <T extends BaseEntity> void put(Class<T> type, Long id, T entity) {
        if (!type.isInstance(entity)) {
            throw new IllegalArgumentException(
                "Entity type does not match the class provided. " +
                "Expected: " + type.getName() + ", Actual: " + entity.getClass().getName()
            );
        }
        map.put(new Key(type, id), entity);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> T get(Class<T> type, Long id) {
        return (T) map.get(new Key(type, id));
    }

    public <T extends BaseEntity> boolean contains(Class<T> type, Long id) {
        return map.containsKey(new Key(type, id));
    }

    public <T extends BaseEntity> void remove(Class<T> type, Long id) {
        map.remove(new Key(type, id));
    }

    public void clear() {
        map.clear();
    }
}
