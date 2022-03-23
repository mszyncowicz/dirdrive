package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

@ExtendWith(SessionFactoryExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DirectoryRepositoryIT {

    @InjectEntityMangaer
    DirectoryRepositoryImpl  directoryRepository;

    @BeforeAll
    public void init(){
        directoryRepository = new DirectoryRepositoryImpl();
    }

    @Test
    public void saveTest(){
        Directory directory = generateRandomDir();
        Directory save = directoryRepository.save(directory);
        Assertions.assertEquals(directory,save);
    }

    @Test
    public void getByLabelTest(){
        directoryRepository.entityManager.getTransaction().begin();
        Directory directory = generateRandomDir();
        directoryRepository.save(directory);
        directoryRepository.entityManager.getTransaction().commit();

        Directory byLabel = directoryRepository.getByLabel(directory.getLabel());
        Assertions.assertEquals(directory,byLabel);
    }

    public static Directory generateRandomDir(){
        Directory directory = new Directory();
        directory.setId(UUID.randomUUID());
        directory.setLabel(Session.generateRandom(20));
        directory.setPath(Session.generateRandom(30));
        return directory;
    }
}
