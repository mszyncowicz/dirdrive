package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;

@ExtendWith(SessionFactoryExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SessionRepositoryIT {


    @InjectEntityMangaer
    SessionRepositoryImpl sessionRepository;

    @InjectEntityMangaer
    ApiKeyRepositoryImpl apiKeyRepository;

    @BeforeAll
    public void init(){
        sessionRepository = new SessionRepositoryImpl();
        apiKeyRepository = new ApiKeyRepositoryImpl();
    }

    @Test
    public void saveTest(){
        Session session = generateSession();

        Session save = sessionRepository.save(session);

        Assertions.assertEquals(session,save);

        Session byId = sessionRepository.getById(session.getId());

        Assertions.assertEquals(session,byId);
    }

    @Test
    public void saveWithNoApiKey(){
        Assertions.assertThrows( Exception.class,() -> noApiKeySave());
    }

    private void noApiKeySave() {
        Session session = generateSession();
        session.setApiKey(null);
        sessionRepository.save(session);
    }

    @Test
    public void getByTokenTest(){
        Session session = generateSession();

        sessionRepository.entityManager.getTransaction().begin();
        sessionRepository.save(session);
        sessionRepository.entityManager.getTransaction().commit();

        Session byToken = sessionRepository.getByToken(session.getToken());
        Assertions.assertEquals(session,byToken);
    }

    public Session generateSession(){
        Session session = new Session();
        session.setId(UUID.randomUUID());
        session.setToken(Session.generateRandom(20));

        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID());
        apiKey.setToken(Session.generateRandom(24));
        apiKeyRepository.getEntityManager().getTransaction().begin();
        apiKeyRepository.save(apiKey);
        apiKeyRepository.getEntityManager().getTransaction().commit();
        session.setApiKey(apiKey);

        return session;
    }
}
