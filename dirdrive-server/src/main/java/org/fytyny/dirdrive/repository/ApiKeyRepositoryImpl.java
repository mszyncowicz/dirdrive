package org.fytyny.dirdrive.repository;

import lombok.Getter;
import org.fytyny.dirdrive.model.ApiKey;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.UUID;

@Model
public class ApiKeyRepositoryImpl implements ApiKeyRepository {

    @Getter
    @Inject
    EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ApiKey getById(UUID id) {
        return entityManager.find(ApiKey.class,id);
    }

    public ApiKey save(ApiKey object) {
        if (object == null) throw new IllegalArgumentException();
        if (getById(object.getId()) == null){
            entityManager.persist(object);
            return object;
        } else {
            return entityManager.merge(object);
        }
    }

    @Override
    public ApiKey getByToken(String token) {
        if (token == null) return null;
        Query query = entityManager.createQuery("SELECT s from ApiKey s where s.token = :token");
        query.setParameter("token",token);
        List resultList = query.getResultList();
        return resultList.isEmpty() ? null : (ApiKey) resultList.get(0);
    }
}
