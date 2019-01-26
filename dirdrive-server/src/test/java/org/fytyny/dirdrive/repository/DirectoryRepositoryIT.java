package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Directory;
import java.util.UUID;

import org.fytyny.dirdrive.model.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DirectoryRepositoryIT {

    DirectoryRepositoryImpl  directoryRepository;
    @Rule
    public SessionFactoryRule sessionFactoryRule = new SessionFactoryRule();
    @Before
    public void init(){
        directoryRepository = new DirectoryRepositoryImpl();
        sessionFactoryRule.injectManager(directoryRepository);
    }

    @Test
    public void saveTest(){
        Directory directory = generateRandomDir();
        Directory save = directoryRepository.save(directory);
        Assert.assertEquals(directory,save);
    }

    @Test
    public void getByLabelTest(){
        directoryRepository.entityManager.getTransaction().begin();
        Directory directory = generateRandomDir();
        directoryRepository.save(directory);
        directoryRepository.entityManager.getTransaction().commit();

        Directory byLabel = directoryRepository.getByLabel(directory.getLabel());
        Assert.assertEquals(directory,byLabel);
    }

    public static Directory generateRandomDir(){
        Directory directory = new Directory();
        directory.setId(UUID.randomUUID());
        directory.setLabel(Session.generateRandom(20));
        directory.setPath(Session.generateRandom(30));
        return directory;
    }
}
