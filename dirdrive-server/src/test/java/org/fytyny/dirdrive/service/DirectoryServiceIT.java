package org.fytyny.dirdrive.service;

import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.repository.DirectoryRepository;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DirectoryServiceIT {

    @Mock
    ApiKeyService apiKeyService;

    @Mock
    DirectoryRepository directoryRepository;

    DirectoryServiceImpl directoryService;

    @BeforeEach
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

        when(apiKeyService.existByToken(any())).thenReturn(true);
        when(apiKeyService.containsDirectory(any(),any())).thenAnswer(a->{
            Directory directory = a.getArgument(1,Directory.class);
            ApiKey apiKey1 = a.getArgument(0,ApiKey.class);
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
        Assertions.assertFalse(directoryService.addDirectoryToApiKey(dir,session));
        Assertions.assertTrue(directoryService.addDirectoryToApiKey(dir,session2));
        Assertions.assertTrue(apiKey2.getDirectoryList().size() > size);

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
        Assertions.assertNotNull(filesOfDir);
        List<String> namesOfDir = filesOfDir.stream().map(f -> f.getName()).collect(Collectors.toList());
        Assertions.assertFalse(namesOfDir.isEmpty());
        Assertions.assertTrue(namesOfDir.contains("Eminem - Venom-8CdcCD5V-d8.mp3"));
    }

    @Test
    public void shouldNotGetDirIfDirNoInApiKeyDirList(){
        Assertions.assertThrows(IllegalArgumentException.class, () ->apiKeyWithoutDirGetDir());
    }

    private void apiKeyWithoutDirGetDir() {
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
        Assertions.assertTrue(directoryService.getSingleFile("Eminem - Venom-8CdcCD5V-d8.mp3",directory,session).isPresent());
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
