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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.transaction.*;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    File gitignoreFile;

    @BeforeEach
    public void before(){
        directoryController.sessionService =sessionService;
        directoryController.apiKeyService =apiKeyService;
        directoryController.directoryService = directoryService;
        ApiKey apiKey = new ApiKey();
        apiKey.setToken(API_TOKEN);
        gitignoreFile = new File("..//.gitignore");

        Directory realDirectory = new Directory();
        realDirectory.setLabel("main");
        realDirectory.setPath(new File(new File("").getAbsolutePath()).getParentFile().getAbsolutePath());

        apiKey.setDirectoryList(List.of(realDirectory));

        when(apiKeyService.getApiKeyBySession(SESSION_TOKEN)).thenReturn(apiKey);

        Session session = getSession(SESSION_TOKEN, apiKeyService.getApiKeyBySession(SESSION_TOKEN));

        when(sessionService.getSessionByToken(SESSION_TOKEN)).thenReturn(session);
        responseService = mock(ResponseService.class, a ->{
            throw new IllegalStateException();
        });
        directoryController.responseService = responseService;
        directoryController.userTransaction = userTransaction;


    }

    @Test
    public void shouldNotAddDirectoryWhenWrongSession() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));
        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir(API_TOKEN, "wrong", directoryDTO);
        Assertions.assertNull(wrong);

    }

    @Test
    public void shouldNotAddDirectoryWhenNullSession() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir(API_TOKEN, null, directoryDTO);
        Assertions.assertNull(wrong);
    }

    @Test
    public void shouldNotAddDirectoryWhenWrongApiKey() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir("wrong", SESSION_TOKEN, directoryDTO);
        Assertions.assertNull(wrong);
    }
    @Test
    public void shouldNotAddDirectoryWhenNullApiKey() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response wrong = directoryController.addDir(null, SESSION_TOKEN, directoryDTO);
        Assertions.assertNull(wrong);
    }

    @Test
    public void shouldAddDitectoryWhenSessionAndKeyOk() throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).success(any());

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath("c:/windows");

        Response right = directoryController.addDir(API_TOKEN, SESSION_TOKEN, directoryDTO);
        Assertions.assertNull(right);

        verify(directoryService).addDirectoryToApiKey(argThat(o->o.getPath().equals(directoryDTO.getPath())),any());
    }

    @Test
    public void shouldReturnDirectoryList() throws NotSupportedException, SystemException {
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(SESSION_TOKEN);
        List<Directory> directoryList = generateRandomDirList();
        Assertions.assertFalse(directoryList.isEmpty());
        apiKeyBySession.setDirectoryList(directoryList);

        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof DirectoryListDTO);
            DirectoryListDTO directoryListDTO = (DirectoryListDTO) a.getArguments()[0];
            List<DirectoryDTO> collect = directoryList.stream().map(DirectoryDTO::getFrom)
                    .collect(Collectors.toList());
            Assertions.assertTrue(directoryListDTO.getDirectoryList().containsAll(collect));
            return null;
        }).when(responseService).success(any());
        directoryController.responseService = responseService;

        Assertions.assertNull(directoryController.getAllDirs(SESSION_TOKEN));
    }

    @Test
    public void shouldNotReturnDirectoryList() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));
        directoryController.responseService = responseService;

        Assertions.assertNull(directoryController.getAllDirs(API_TOKEN));
    }

    @Test
    public void shouldReturnReturnFileListResponse() throws NotSupportedException, SystemException {
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(SESSION_TOKEN);
        Directory directory = apiKeyBySession.getDirectoryList().get(0);
        Assertions.assertNotNull(directory);
        doAnswer(a->{
            Assertions.assertTrue(a.getArguments()[0] instanceof FileListResponseDTO);
            FileListResponseDTO fileListResponseDTO = (FileListResponseDTO) a.getArguments()[0];
            Set<FileDTO> fileDTOList = fileListResponseDTO.getFileDTOSet();
            Pattern pattern = Pattern.compile("[0-3][0-9]-[0-1][0-9]-[0-9]{4} [0-2][0-9]:[0-5][0-9]:[0-5][0-9]");
            fileDTOList.forEach(e->{
                Assertions.assertNotNull(e.getModifyDate());
                Assertions.assertNotNull(e.getName());
                Assertions.assertFalse(e.getName().isEmpty());
                Assertions.assertTrue(e.getModifyDate().matches(pattern.pattern()));
            });
            Assertions.assertTrue(fileDTOList.containsAll(fileDTOS(directory.getPath())));
            return null;
        }).when(responseService).success(any());
        when(directoryService.getFilesOfDir(directory,sessionService.getSessionByToken(SESSION_TOKEN))).thenReturn(
                Arrays.stream(Objects.requireNonNull(new File(directory.getPath()).listFiles())).filter(f->!f.isDirectory()).collect(Collectors.toList())
        );

        Response response = directoryController.getDir(SESSION_TOKEN, DirectoryDTO.getFrom(apiKeyBySession.getDirectoryList().get(0)));
        Assertions.assertNull(response);
    }

    @Test
    public void shouldNotReturnReturnFileListWhenDirectoryIsNotInApiKey() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assertions.assertTrue(generalResponseDTO.getMessage().contains("Could not find directory"));
            return null;
        }).when(responseService).error(any(),eq(400));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setLabel("main");
        directoryDTO.setPath("C:\\adawd");

        Response response = directoryController.getDir(SESSION_TOKEN, directoryDTO);
        Assertions.assertNull(response);
    }

    @Test
    public void shouldNotReturnReturnFileListWhenDirectoryIsNotInApiKey2() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assertions.assertTrue(generalResponseDTO.getMessage().contains("Could not find directory"));
            return null;
        }).when(responseService).error(any(),eq(400));

        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setLabel("main");
        directoryDTO.setPath(null);

        Response response = directoryController.getDir(SESSION_TOKEN, directoryDTO);
        Assertions.assertNull(response);
    }
    @Test
    public void shouldNotReturnReturnFileListWhenDirectoryIsNotInApiKey3() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assertions.assertTrue(generalResponseDTO.getMessage().contains("Could not find directory"));
            return null;
        }).when(responseService).error(any(),eq(400));
        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setLabel(null);
        directoryDTO.setPath(new File("").getAbsolutePath());

        Response response = directoryController.getDir(SESSION_TOKEN, directoryDTO);
        Assertions.assertNull(response);
    }
    @Test
    public void shouldNotReturnFileListSessionWrong() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        DirectoryDTO directoryDTO = DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0));
        Response response = directoryController.getDir(SESSION_TOKEN + "sfsf", directoryDTO);
        Assertions.assertNull(response);
    }


    @Test
    public void shouldGetSingleFile() throws NotSupportedException, SystemException {
        doAnswer(a->{
            Assertions.assertTrue(a.getArguments()[0] instanceof FileInputStream);
            return null;
        }).when(responseService).success(any());
        FileDTO fileDTO = new FileDTO(gitignoreFile.getName(),directoryController.getDateFromModDate(gitignoreFile.lastModified()));
        when(directoryService.getSingleFile(any(),any(),any())).thenAnswer(a->{
            Directory argumentAt = a.getArgument(1, Directory.class);
            return Optional.of(new File(argumentAt.getPath() + File.separator + a.getArgument(0,String.class)));
        });
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileDTO(fileDTO);
        fileRequest.setDirectoryDTO(DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0)));
        directoryController.getDirFile(SESSION_TOKEN,fileRequest);
    }

    @Test
    public void shouldNotGetSingleIfFileModDateWrong() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            GeneralResponseDTO generalResponseDTO = (GeneralResponseDTO) a.getArguments()[0];
            Assertions.assertTrue(generalResponseDTO.getMessage().contains("Could not find file"));
            return null;
        }).when(responseService).error(any(),eq(400));

        FileDTO fileDTO = new FileDTO(gitignoreFile.getName(),directoryController.getDateFromModDate(0));
        when(directoryService.getSingleFile(any(),any(),any())).thenAnswer(a->{
            Directory argumentAt = a.getArgument(1, Directory.class);
            return Optional.of(new File(argumentAt.getPath() + File.separator + a.getArgument(0,String.class)));
        });
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileDTO(fileDTO);
        fileRequest.setDirectoryDTO(DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0)));
        directoryController.getDirFile(SESSION_TOKEN,fileRequest);

    }
    @Test
    public void shouldNotGetSingleFileSessionWrong() throws NotSupportedException, SystemException {
        doAnswer(a ->{
            Assertions.assertTrue(a.getArguments()[0] instanceof GeneralResponseDTO);
            return null;
        }).when(responseService).error(any(),eq(401));

        FileDTO fileDTO = new FileDTO(gitignoreFile.getName(),directoryController.getDateFromModDate(gitignoreFile.lastModified()));
        FileRequest fileRequest = new FileRequest();
        fileRequest.setFileDTO(fileDTO);
        fileRequest.setDirectoryDTO(DirectoryDTO.getFrom(apiKeyService.getApiKeyBySession(SESSION_TOKEN).getDirectoryList().get(0)));
        directoryController.getDirFile(SESSION_TOKEN + "sfsf",fileRequest);

    }

    private List<FileDTO> fileDTOS(String path){
        File dir = new File(path).getAbsoluteFile();
        Assertions.assertTrue(dir.exists());
        File[] files = dir.listFiles();
        List<FileDTO> fileDTOS = new LinkedList<>();
        assert files != null;
        addAll(files,fileDTOS);
        System.out.println("Created file list dto: " + fileDTOS);
        return fileDTOS;
    }

    private void addAll(File[] files, List<FileDTO> fileDTOS){
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
    }

    private List<Directory> generateRandomDirList(){
        List<Directory> directoryList = new LinkedList<>();
        Random random = new Random();
        int count = random.nextInt(5);
        count += 10;
        IntStream.range(0,count).forEach( p ->{
            Directory directory = new Directory();
            directory.setPath(RandomStringUtils.randomAlphanumeric(random.nextInt(5) + 10));
            directory.setLabel(RandomStringUtils.randomAlphabetic(random.nextInt(5) + 10));
            directoryList.add(directory);
        });
        return directoryList;
    }
    private Session getSession(String token, ApiKey apiKey){
        Session session = new Session();
        session.setToken(token);
        session.setApiKey(apiKey);
        return session;
    }

}
