package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Session;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.UUID;

@Model
public class SessionRepositoryImpl implements SessionRepository{

    @Inject
    EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Session getById(UUID id) {
        return entityManager.find(Session.class,id);
    }

    public Session save(Session object) {
        if (object == null) throw new IllegalArgumentException();
        if (getById(object.getId()) == null){
            entityManager.persist(object);
            return object;
        } else {
            return entityManager.merge(object);
        }
    }

    @Override
    public Session getByToken(String token) {
        Query query = entityManager.createQuery("SELECT s from Session s where s.token = :param");
        query.setParameter("param",token);
        Object singleResult = query.getSingleResult();
        return (Session) singleResult;
    }
}
