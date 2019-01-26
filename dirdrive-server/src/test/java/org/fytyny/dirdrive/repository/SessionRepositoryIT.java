package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SessionRepositoryIT {


    @Rule
    public SessionFactoryRule sessionFactoryRule = new SessionFactoryRule();

    SessionRepositoryImpl sessionRepository;

    ApiKeyRepositoryImpl apiKeyRepository;

    @Before
    public void init(){
        sessionRepository = new SessionRepositoryImpl();
        apiKeyRepository = new ApiKeyRepositoryImpl();
        sessionFactoryRule.injectManager(sessionRepository);
        sessionFactoryRule.injectManager(apiKeyRepository);
    }

    @Test
    public void saveTest(){
        Session session = generateSession();

        Session save = sessionRepository.save(session);

        Assert.assertEquals(session,save);

        Session byId = sessionRepository.getById(session.getId());

        Assert.assertEquals(session,byId);
    }

    @Test(expected = Exception.class)
    public void saveWithNoApiKey(){
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
        Assert.assertEquals(session,byToken);
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
