package org.fytyny.dirdrive.service;

import lombok.Data;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.repository.SessionRepository;

import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionServiceTest {

    @Spy
    SessionServiceImpl sessionService;

    @Mock
    SessionRepository sessionRepository;

    @Before
    public void init(){
        sessionService.setSessionRepository(sessionRepository);
    }

    @Test
    public void createSessionTest(){

        ApiKey apiKey = new ApiKey();
        apiKey.setToken(Session.generateRandom(20));
        apiKey.setId(UUID.randomUUID());

        SessionContainer sessionContainer = new SessionContainer();
        when(sessionRepository.save(anyObject())).then(a ->{
            Session argumentAt = a.getArgumentAt(0,Session.class);
            Assert.assertEquals(argumentAt.getApiKey(),apiKey);
            sessionContainer.setSession(argumentAt);
            return argumentAt;
        });

        Session session = sessionService.createSession(apiKey);

        Assert.assertNotNull(sessionContainer.getSession());
        Assert.assertEquals(session,sessionContainer.getSession());

    }
    @Data
    private static class SessionContainer{
        private Session session;
    }

}
