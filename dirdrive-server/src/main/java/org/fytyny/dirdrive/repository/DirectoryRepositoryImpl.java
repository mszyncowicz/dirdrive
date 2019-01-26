package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Directory;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.util.List;
import java.util.UUID;

@Model
public class DirectoryRepositoryImpl implements DirectoryRepository {

    @Inject
    EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Directory getById(UUID id) {
        return entityManager.find(Directory.class,id);
    }

    public Directory save(Directory object) {
        if (object == null) throw new IllegalArgumentException();
        if (getById(object.getId()) == null){
            entityManager.persist(object);
            return object;
        } else {
            return entityManager.merge(object);
        }
    }

    @Override
    public Directory getByLabel(String label) {
        Query query = entityManager.createQuery("SELECT d from Directory d where d.label = :label");
        query.setParameter("label",label);
        List resultList = query.getResultList();
        return resultList.isEmpty() ? null : (Directory) resultList.get(0);
    }

}
