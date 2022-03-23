package org.fytyny.dirdrive.controller;

import org.apache.commons.lang.RandomStringUtils;
import org.fytyny.dirdrive.api.dto.GeneralResponseDTO;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.service.ApiKeyService;
import org.fytyny.dirdrive.service.ResponseService;
import org.fytyny.dirdrive.service.SessionService;
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
import org.mockito.stubbing.Answer;

import javax.transaction.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Arrays;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SessionControllerTest {

    private final static String API_TOKEN = "Working api token";
    ResponseService responseService;

    @Mock
    SessionService sessionService;

    @Mock
    ApiKeyService apiKeyService;

    @Mock
    UserTransaction userTransaction;

    @Spy
    SessionController sessionController;

    @BeforeEach
    public void before(){
        sessionController.sessionService =sessionService;
        sessionController.apiKeyService =apiKeyService;
        ApiKey apiKey = new ApiKey();
        apiKey.setToken(API_TOKEN);
        responseService = mock(ResponseService.class, (Answer)  a ->{
            throw new IllegalStateException();
        });
        Directory realDirectory = new Directory();
        realDirectory.setLabel("main");
        realDirectory.setPath(new File("").getAbsolutePath());
        when(apiKeyService.existByToken(API_TOKEN)).thenReturn(true);
        when(apiKeyService.getByToken(API_TOKEN)).thenReturn(apiKey);

        apiKey.setDirectoryList(Arrays.asList(realDirectory));

        sessionController.responseService = responseService;
        sessionController.userTransaction = userTransaction;
    }

    @Test
    public void shouldReturnNewSession() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        Session generated = generateRandomSession(apiKeyService.getByToken(API_TOKEN));
        when(sessionService.createSession(any())).then( a -> {
            Assertions.assertTrue(a.getArguments()[0].equals(apiKeyService.getByToken(API_TOKEN)));
            return generated;
        });
        doAnswer( a ->{
            Assertions.assertEquals(generated,a.getArguments()[0]);
            return null;
        }).when(responseService).success(any());

        Response session = sessionController.createSession(API_TOKEN);

        Assertions.assertNull(session);
        verify(sessionService).createSession(apiKeyService.getByToken(API_TOKEN));
        verify(apiKeyService,times(4)).getByToken(API_TOKEN);
        verify(responseService).success(generated);
    }

    @Test
    public void shouldReturnErrorWhenApiKeyWrong() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {

        doAnswer( a ->{
            GeneralResponseDTO argumentAt = a.getArgument(0, GeneralResponseDTO.class);
            Assertions.assertEquals(GeneralResponseDTO.authenticationFailed(),argumentAt);
            return null;
        }).when(responseService).error(any(),eq(401));

        Response response = sessionController.createSession(API_TOKEN + "sgeg");

        Assertions.assertNull(response);
        verify(sessionService,times(0)).createSession(apiKeyService.getByToken(API_TOKEN));
        verify(apiKeyService).getByToken(API_TOKEN);
        verify(responseService).error(any(),eq(401));
    }
    Session generateRandomSession(ApiKey apiKey){
        Session session = new Session();
        session.setApiKey(apiKey);
        session.setToken(RandomStringUtils.randomAlphabetic(10));
        session.setId(UUID.randomUUID());
        return session;
    }
}
