package org.fytyny.dirdrive.service;

import lombok.Data;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.repository.SessionRepository;

import java.util.UUID;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SessionServiceTest {

    @Spy
    SessionServiceImpl sessionService;

    @Mock
    SessionRepository sessionRepository;

    @BeforeEach
    public void init(){
        sessionService.setSessionRepository(sessionRepository);
    }

    @Test
    public void createSessionTest(){

        ApiKey apiKey = new ApiKey();
        apiKey.setToken(Session.generateRandom(20));
        apiKey.setId(UUID.randomUUID());

        SessionContainer sessionContainer = new SessionContainer();
        when(sessionRepository.save(any())).then(a ->{
            Session argumentAt = a.getArgument(0,Session.class);
            Assertions.assertEquals(argumentAt.getApiKey(),apiKey);
            sessionContainer.setSession(argumentAt);
            return argumentAt;
        });

        Session session = sessionService.createSession(apiKey);

        Assertions.assertNotNull(sessionContainer.getSession());
        Assertions.assertEquals(session,sessionContainer.getSession());

    }
    @Data
    private static class SessionContainer{
        private Session session;
    }

}
