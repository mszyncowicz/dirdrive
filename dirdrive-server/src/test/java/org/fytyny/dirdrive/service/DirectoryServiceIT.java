package org.fytyny.dirdrive.service;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.repository.DirectoryRepository;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryServiceIT {

    @Mock
    ApiKeyService apiKeyService;

    @Mock
    DirectoryRepository directoryRepository;

    DirectoryServiceImpl directoryService;

    @Before
    public void init(){
        directoryService = new DirectoryServiceImpl();
        directoryService.apiKeyService = apiKeyService;
        directoryService.directoryRepository = directoryRepository;

        ApiKey apiKey = new ApiKey();
        apiKey.setDirectoryList(randomDirs());

        ApiKey secondApiKey = new ApiKey();
        secondApiKey.setDirectoryList(randomDirs());

        when(apiKeyService.getApiKeyBySession("session1")).thenReturn(apiKey);

        when(apiKeyService.getApiKeyBySession("session2")).thenReturn(secondApiKey);

        when(apiKeyService.existByToken(anyObject())).thenReturn(true);
        when(apiKeyService.containsDirectory(anyObject(),anyObject())).thenAnswer(a->{
            Directory directory = a.getArgumentAt(1,Directory.class);
            ApiKey apiKey1 = a.getArgumentAt(0,ApiKey.class);
            return apiKeyService.existByToken(apiKey.getToken()) && apiKey1.getDirectoryList().stream().map(d -> d.getPath()).collect(Collectors.toList()).contains(directory.getPath());
        });

    }

    @Test
    public void cantAddTwoSameLabelsForOneApiKey(){
        Directory dir = apiKeyService.getApiKeyBySession("session1").getDirectoryList().get(0);
        Session session = new Session();
        session.setApiKey(apiKeyService.getApiKeyBySession("session1"));
        session.setToken("session1");
        Session session2 = new Session();
        ApiKey apiKey2 = apiKeyService.getApiKeyBySession("session2");
        session2.setApiKey(apiKey2);
        session2.setToken("session2");
        int size = apiKey2.getDirectoryList().size();
        Assert.assertFalse(directoryService.addDirectoryToApiKey(dir,session));
        Assert.assertTrue(directoryService.addDirectoryToApiKey(dir,session2));
        Assert.assertTrue(apiKey2.getDirectoryList().size() > size);

        verify(apiKeyService).save(apiKey2);
    }

    @Test
    public void shouldReturnFileList(){
        Directory directory = new Directory();
        directory.setPath("E:\\Muzyka\\Yt-Music");
        ApiKey session1 = apiKeyService.getApiKeyBySession("session1");
        session1.setDirectoryList(Arrays.asList(directory));
        Session session = createSession("session1", session1);
        List<File> filesOfDir = directoryService.getFilesOfDir(directory, session);
        Assert.assertNotNull(filesOfDir);
        List<String> namesOfDir = filesOfDir.stream().map(f -> f.getName()).collect(Collectors.toList());
        Assert.assertFalse(namesOfDir.isEmpty());
        Assert.assertTrue(namesOfDir.contains("Eminem - Venom-8CdcCD5V-d8.mp3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotGetDirIfDirNoInApiKeyDirList(){
        Directory directory = new Directory();
        directory.setPath("E:\\Muzyka\\Yt-Music");
        ApiKey session1 = apiKeyService.getApiKeyBySession("session1");
        Session session = createSession("session1", session1);
        directoryService.getFilesOfDir(directory, session);
    }

    @Test
    public void getSingleFileTest(){
        Directory directory = new Directory();
        directory.setPath("E:\\Muzyka\\Yt-Music");
        ApiKey session1 = apiKeyService.getApiKeyBySession("session1");
        session1.setDirectoryList(Arrays.asList(directory));
        Session session = createSession("session1", session1);
        Assert.assertTrue(directoryService.getSingleFile("Eminem - Venom-8CdcCD5V-d8.mp3",directory,session).isPresent());
    }
    private Session createSession(String token, ApiKey apiKey){
        Session session = new Session();
        session.setToken("session1");
        session.setId(UUID.randomUUID());
        session.setApiKey(apiKey);
        return session;
    }



    List<Directory> randomDirs(){
       Random random = new Random();
       int num = random.nextInt(10)+5;
       List<Directory> directoryList = new LinkedList<>();
       for (int i = 0; i< num; i++){
           Directory directory = new Directory();
           directory.setPath(Session.generateRandom(10));
           directory.setLabel(Session.generateRandom(10));
           directoryList.add(directory);
       }
       return directoryList;
    }
}
