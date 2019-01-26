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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.transaction.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
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
            Assert.assertTrue(a.getArguments()[0].equals(apiKeyService.getByToken(API_TOKEN)));
            return generated;
        });
        doAnswer( a ->{
            Assert.assertEquals(generated,a.getArguments()[0]);
            return null;
        }).when(responseService).success(any());

        Response session = sessionController.createSession(API_TOKEN);

        Assert.assertNull(session);
        verify(sessionService).createSession(apiKeyService.getByToken(API_TOKEN));
        verify(apiKeyService,times(4)).getByToken(API_TOKEN);
        verify(responseService).success(generated);
    }

    @Test
    public void shouldReturnErrorWhenApiKeyWrong() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {

        doAnswer( a ->{
            GeneralResponseDTO argumentAt = a.getArgumentAt(0, GeneralResponseDTO.class);
            Assert.assertEquals(GeneralResponseDTO.authenticationFailed(),argumentAt);
            return null;
        }).when(responseService).error(any(),eq(401));

        Response response = sessionController.createSession(API_TOKEN + "sgeg");

        Assert.assertNull(response);
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
