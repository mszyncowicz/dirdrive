package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.LinkedList;
import java.util.UUID;

@ExtendWith(SessionFactoryExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiKeyRepositoryIT {

    @InjectEntityMangaer
    ApiKeyRepositoryImpl apiKeyRepository;

    @BeforeAll
    public void init(){
        apiKeyRepository = new ApiKeyRepositoryImpl();
    }

    @Test
    public void saveTest(){
        ApiKey apiKey = generateRandomApiKey();
        apiKeyRepository.save(apiKey);
        Assertions.assertEquals(apiKey,apiKeyRepository.getById(apiKey.getId()));
    }

    @Test
    public void getByTokenTest(){
        ApiKey apiKey = generateRandomApiKey();
        apiKeyRepository.entityManager.getTransaction().begin();
        apiKeyRepository.save(apiKey);
        apiKeyRepository.entityManager.getTransaction().commit();

        ApiKey byToken = apiKeyRepository.getByToken(apiKey.getToken());
        Assertions.assertNotNull(byToken);
        Assertions.assertEquals(apiKey,byToken);
    }

    private ApiKey generateRandomApiKey(){
        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID());
        apiKey.setToken(Session.generateRandom(10));
        apiKey.setDirectoryList(new LinkedList<>());
        return apiKey;
    }
}
