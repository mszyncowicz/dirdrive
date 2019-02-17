package org.fytyny.dirdrive.controller;

import org.apache.commons.lang.RandomStringUtils;
import org.fytyny.dirdrive.api.dto.*;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.service.ApiKeyService;
import org.fytyny.dirdrive.service.DirectoryService;
import org.fytyny.dirdrive.service.ResponseService;
import org.fytyny.dirdrive.service.SessionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.transaction.*;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryControllerTest {

    private final static String SESSION_TOKEN = "Working token";
    private final static String API_TOKEN = "Working api token";

    ResponseService responseService;

    @Mock
    SessionService sessionService;

    @Mock
    ApiKeyService apiKeyService;

    @Mock
    DirectoryService directoryService;

    @Mock
    UserTransaction userTransaction;

    @Spy
    DirectoryController directoryController;

    @Before
    public void before(){
        directoryController.sessionService =sessionService;
        directoryController.apiKeyService =apiKeyService;
        directoryController.directoryService = directoryService;
        ApiKey apiKey = new ApiKey();
        apiKey.setToken(API_TOKEN);

        Directory realDirectory = new Directory();
        realDirectory.setLabel("main");
        realDirectory.setPath(new File(new File("").getAbsolutePath()).getParentFile().getAbsolutePath());

        apiKey.setDirectoryList(Arrays.asList(realDirectory));

        when(apiKeyService.getApiKeyBySession(SESSION_TOKEN)).thenReturn(apiKey);

        Session session = getSession(SESSION_TOKEN, apiKeyService.getApiKeyBySession(SESSION_TOKEN));

        when(sessionService.getSessionByToken(SESSION_TOKEN)).thenReturn(session);
        responseService = mock(ResponseService.class, (Answer)  a ->{
            throw new IllegalStateException();
        });
        directoryController.responseService = responseService;
        directoryController.userTransaction = userTransaction;
    }

    @Test
    public void shouldNotAddDirectoryWhenWrongSession() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(anyObject(),eq(401));
        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir(API_TOKEN, "wrong", directoryDTO);
        Assert.assertNull(wrong);

    }

    @Test
    public void shouldNotAddDirectoryWhenNullSession() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir(API_TOKEN, null, directoryDTO);
        Assert.assertNull(wrong);
    }

    @Test
    public void shouldNotAddDirectoryWhenWrongApiKey() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir("wrong", SESSION_TOKEN, directoryDTO);
        Assert.assertNull(wrong);
    }
    @Test
    public void shouldNotAddDirectoryWhenNullApiKey() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir(null, SESSION_TOKEN, directoryDTO);
        Assert.assertNull(wrong);
    }

    @Test
    public void shouldAddDitectoryWhenSessionAndKeyOk() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).success(any());

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response right = directoryController.addDir(API_TOKEN, SESSION_TOKEN, directoryDTO);
        Assert.assertNull(right);

        verify(directoryService).addDirectoryToApiKey(argThat(new ArgumentMatcher<Directory>() {
            @Override
            public boolean matches(Object o){
                if (!(o instanceof Directory)) return false;
                return ((Directory) o).getPath().equals(directoryDTO.getPath());
            }
        }),any());
    }

    @Test
    public void shouldReturnDirectoryList() throws NotSupportedException, SystemException {
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(SESSION_TOKEN);
        List<Directory> directoryList = generateRandomDirList();
        Assert.assertFalse(directoryList.isEmpty());
        apiKeyBySession.setDirectoryList(directoryList);

        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof DirectoryListDTO);
            DirectoryListDTO directoryListDTO = (DirectoryListDTO) a.getArguments()[0];
            List<DirectoryDTO> collect = directoryList.stream().map(d -> {
                return DirectoryDTO.getFrom(d);
            }).collect(Collectors.toList());
            Assert.assertTrue(directoryListDTO.getDirectoryList().containsAll(collect));
            return null;
        }).when(responseService).success(any());
        directoryController.responseService = responseService;

        Assert.assertNull(directoryController.getAllDirs(SESSION_TOKEN));
    }

    @Test
    public void shouldNotReturnDirectoryList() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));
        directoryController.responseService = responseService;

        Assert.assertNull(directoryController.getAllDirs(API_TOKEN));
    }

    @Test
    public void shouldReturnReturnFileListResponse() throws NotSupportedException, SystemException {
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(SESSION_TOKEN);
        Directory directory = apiKeyBySession.getDirectoryList().get(0);
        Assert.assertNotNull(directory);
        doAnswer(a->{
            Assert.assertTrue(a.getArguments()[0] instanceof FileListResponseDTO);
            FileListResponseDTO fileListResponseDTO = (FileListResponseDTO) a.getArguments()[0];
            Set<FileDTO> fileDTOList = fileListResponseDTO.getFileDTOSet();
            Pattern pattern = Pattern.compile("[0-3][0-9]-[0-1][0-9]-[0-9]{4} [0-2][0-9]:[0-5][0-9]:[0-5][0-9]");
            fileDTOList.forEach(e->{
                Assert.assertNotNull(e.getModifyDate());
                Assert.assertNotNull(e.getName());
                Assert.assertFalse(e.getName().isEmpty());
                Assert.assertTrue(e.getModifyDate().matches(pattern.pattern()));
            });
            Assert.assertTrue(fileDTOList.containsAll(fileDTOS(directory.getPath())));
            return null;
        }).when(responseService).success(any());
        when(directoryService.getFilesOfDir(directory,sessionService.getSessionByToken(SESSION_TOKEN))).thenReturn(
                Arrays.stream(new File(directory.getPath()).listFiles()).filter(f->!f.isDirectory()).collect(Collectors.toList())
        );

        Response response = directoryController.getDir(SESSION_TOKEN, DirectoryDTO.getFrom(apiKeyBySession.getDirectoryList().get(0)));
        Assert.assertNull(response);
    }

    @Test
    public void shouldNotReturnReturnFileListWhenDirectoryIsNotInApiKey() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assert.assertTrue(generalResponseDTO.getMessage().contains("Could not find directory"));
            return null;
        }).when(responseService).error(any(),eq(400));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setLabel("main");
        directoryDTO.setPath("C:\\Windows");

        Response response = directoryController.getDir(SESSION_TOKEN, directoryDTO);
        Assert.assertNull(response);
    }

    @Test
    public void shouldNotReturnReturnFileListWhenDirectoryIsNotInApiKey2() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assert.assertTrue(generalResponseDTO.getMessage().contains("Could not find directory"));
            return null;
        }).when(responseService).error(any(),eq(400));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setLabel("main");
        directoryDTO.setPath(null);

        Response response = directoryController.getDir(SESSION_TOKEN, directoryDTO);
        Assert.assertNull(response);
    }
    @Test
    public void shouldNotReturnReturnFileListWhenDirectoryIsNotInApiKey3() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assert.assertTrue(generalResponseDTO.getMessage().contains("Could not find directory"));
            return null;
        }).when(responseService).error(any(),eq(400));
        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setLabel(null);
        directoryDTO.setPath(new File("").getAbsolutePath());

        Response response = directoryController.getDir(SESSION_TOKEN, directoryDTO);
        Assert.assertNull(response);
    }
    @Test
    public void shouldNotReturnFileListSessionWrong() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0));
        Response response = directoryController.getDir(SESSION_TOKEN + "sfsf", directoryDTO);
        Assert.assertNull(response);
    }


    @Test
    public void shouldGetSingleFile() throws NotSupportedException, SystemException {
        doAnswer(a->{
            Assert.assertTrue(a.getArguments()[0] instanceof FileInputStream);

            FileInputStream file = (FileInputStream) a.getArguments()[0];

            return null;
        }).when(responseService).success(any());

        FileDTO fileDTO = new FileDTO(".gitignore","26-01-2019 19:27:42");
        when(directoryService.getSingleFile(any(),any(),any())).thenAnswer(a->{
            Directory argumentAt = a.getArgumentAt(1, Directory.class);
            return Optional.of(new File(argumentAt.getPath() + File.separator + a.getArgumentAt(0,String.class)));
        });
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileDTO(fileDTO);
        fileRequest.setDirectoryDTO(DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0)));
        directoryController.getDirFile(SESSION_TOKEN,fileRequest);
    }

    @Test
    public void shouldNotGetSingleFileDateWrong() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assert.assertTrue(generalResponseDTO.getMessage().contains("Could not find file"));
            return null;
        }).when(responseService).error(any(),eq(400));

        FileDTO fileDTO = new FileDTO(".gitignore","28-12-2017 00:15:27");
        when(directoryService.getSingleFile(any(),any(),any())).thenAnswer(a->{
            Directory argumentAt = a.getArgumentAt(1, Directory.class);
            return Optional.of(new File(argumentAt.getPath() + File.separator + a.getArgumentAt(0,String.class)));
        });
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileDTO(fileDTO);
        fileRequest.setDirectoryDTO(DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0)));
        directoryController.getDirFile(SESSION_TOKEN,fileRequest);

    }
    @Test
    public void shouldNotGetSingleFileSessionWrong() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assert.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        FileDTO fileDTO = new FileDTO(".gitignore","28-12-2018 00:15:27");
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileDTO(fileDTO);
        fileRequest.setDirectoryDTO(DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0)));
        directoryController.getDirFile(SESSION_TOKEN + "sfsf",fileRequest);

    }

    private List<FileDTO> fileDTOS(String path){
        File dir = new File(path).getAbsoluteFile();
        Assert.assertTrue(dir.exists());
        File[] files = dir.listFiles();
        List<FileDTO> fileDTOS = new LinkedList<>();
        fileDTOS = addAll(files,fileDTOS);
        System.out.println("Created file list dto: " + fileDTOS.toString());
        return fileDTOS;
    }

    private List<FileDTO> addAll(File[] files, List<FileDTO> fileDTOS){
        for (File file : files){
            if (!file.isDirectory()){
                long lastMod = file.lastModified();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                Instant temporal = Instant.ofEpochMilli(lastMod);
                LocalDateTime localDateTime = LocalDateTime.ofInstant(temporal, ZoneId.systemDefault());
                String format = dateTimeFormatter.format(localDateTime);
                FileDTO fileDTO = new FileDTO(file.getName(), format);
                fileDTOS.add(fileDTO);
            }
        }
        return fileDTOS;
    }

    private List<Directory> generateRandomDirList(){
        List<Directory> directoryList = new LinkedList<>();
        Random random = new Random();
        int count = random.nextInt(5);
        count += 10;
        IntStream.range(0,count).peek( p ->{
            Directory directory = new Directory();
            directory.setPath(RandomStringUtils.randomAlphanumeric(random.nextInt(5) + 10));
            directory.setLabel(RandomStringUtils.randomAlphabetic(random.nextInt(5) + 10));
            directoryList.add(directory);
        }).count();
        return directoryList;
    }
    private Session getSession(String token, ApiKey apiKey){
        Session session = new Session();
        session.setToken(token);
        session.setApiKey(apiKey);
        return session;
    }

}
