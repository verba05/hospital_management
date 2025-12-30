package hospital_managment.patterns;

import java.util.List;

import hospital_managment.domain.BaseEntity;

public interface LazyLoad<T extends BaseEntity> {
    public List<T> get();
}
