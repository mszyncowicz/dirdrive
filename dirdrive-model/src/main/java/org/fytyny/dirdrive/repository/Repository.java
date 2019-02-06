package org.fytyny.dirdrive.repository;

import javax.persistence.EntityManager;
import java.util.UUID;

public interface Repository<T> {
    void setEntityManager(EntityManager entityManager);

    T getById(UUID id);

    T save(T object);

}
