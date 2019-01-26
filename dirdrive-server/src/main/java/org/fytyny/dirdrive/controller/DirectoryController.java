package org.fytyny.dirdrive.controller;


import org.fytyny.dirdrive.api.dto.*;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.service.ApiKeyService;
import org.fytyny.dirdrive.service.DirectoryService;
import org.fytyny.dirdrive.service.ResponseService;
import org.fytyny.dirdrive.service.SessionService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.transaction.*;
import javax.transaction.NotSupportedException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/dir")
public class DirectoryController {

    @Any
    @Inject
    ResponseService responseService;

    @Any
    @Inject
    SessionService sessionService;

    @Any
    @Inject
    ApiKeyService apiKeyService;

    @Any
    @Inject
    DirectoryService directoryService;

    @Inject
    UserTransaction userTransaction;

    public static final String X_SESSION_TOKEN = "X-session-token";
    public static final String X_API_KEY = "X-api-key";

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDir(@HeaderParam(X_API_KEY) String apiKeyToken, @HeaderParam(X_SESSION_TOKEN) String sessionToken, DirectoryDTO directory) throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException {
        if (apiKeyToken == null || sessionToken == null) {
            return responseService.error(GeneralResponseDTO.authenticationFailed(), 401);
        }

        userTransaction.begin();
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(sessionToken);
        if (apiKeyBySession == null || !apiKeyBySession.getToken().equals(apiKeyToken)) {
            userTransaction.rollback();
            return responseService.error(GeneralResponseDTO.authenticationFailed(), 401);
        }

        Optional<Directory> any = apiKeyBySession.getDirectoryList().stream().filter(d -> d.getPath().equals(directory.getPath()) || d.getLabel().equals(directory.getLabel())).findAny();
        if (any.isPresent()){
            userTransaction.rollback();
            return responseService.error(new GeneralResponseDTO("Directory with that label or that path already exists",400),400);
        }
        Directory newDirectory = new Directory();
        newDirectory.setId(UUID.randomUUID());
        newDirectory.setLabel(directory.getLabel());
        newDirectory.setPath(directory.getPath());
        directoryService.addDirectoryToApiKey(newDirectory,sessionService.getSessionByToken(sessionToken));
        userTransaction.commit();
        return responseService.success(new GeneralResponseDTO("Directory added successfully",200));
    }

    @GET
    @Path("/listAll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDirs(@HeaderParam(X_SESSION_TOKEN) String sessionToken) throws SystemException, NotSupportedException {
        if (sessionToken == null) {
            return responseService.error(GeneralResponseDTO.authenticationFailed(), 401);
        }

        userTransaction.begin();
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(sessionToken);
        if (apiKeyBySession == null){
            userTransaction.rollback();
            return responseService.error(GeneralResponseDTO.authenticationFailed(),401);
        }
        DirectoryListDTO directoryListDTO = new DirectoryListDTO();
        List<DirectoryDTO> directoryDTOS = new LinkedList<>();
        directoryListDTO.setDirectoryList(directoryDTOS);

        for (Directory directory : apiKeyBySession.getDirectoryList()){
            directoryDTOS.add(DirectoryDTO.getFrom(directory));
        }

        userTransaction.rollback();
        return responseService.success(directoryListDTO);
    }

    @POST
    @Path("/get")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDir(@HeaderParam(X_SESSION_TOKEN) String sessionToken, DirectoryDTO directoryDTO) throws SystemException, NotSupportedException {
        if (sessionToken == null) {
            return responseService.error(GeneralResponseDTO.authenticationFailed(), 401);
        }
        userTransaction.begin();
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(sessionToken);
        if (apiKeyBySession == null){
            userTransaction.rollback();
            return responseService.error(GeneralResponseDTO.authenticationFailed(),401);
        }

        Optional<Directory> any = apiKeyBySession.getDirectoryList().stream().filter(d -> d.getLabel().equals(directoryDTO.getLabel()) && d.getPath().equals(directoryDTO.getPath())).findAny();
        if (!any.isPresent()){
            userTransaction.rollback();
            return responseService.error(GeneralResponseDTO.directoryNotFound(),400);
        }
        List<File> filesOfDir = directoryService.getFilesOfDir(any.get(), sessionService.getSessionByToken(sessionToken));
        FileListResponseDTO fileListResponseDTO = fromFileList(filesOfDir);
        userTransaction.rollback();
        return responseService.success(fileListResponseDTO);
    }

    FileListResponseDTO fromFileList(List<File> files){
        FileListResponseDTO fileListResponseDTO = new FileListResponseDTO();
        List<FileDTO> fileDTOS = new LinkedList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        for (File f : files){
            Instant temporal = Instant.ofEpochMilli(f.lastModified());
            LocalDateTime localDateTime = LocalDateTime.ofInstant(temporal, ZoneId.systemDefault());
            String format = dateTimeFormatter.format(localDateTime);
            fileDTOS.add(new FileDTO(f.getName(),format));
        }
        fileListResponseDTO.setFileDTOList(fileDTOS);
        return fileListResponseDTO;
    }

    String getDateFromModDate(long modDate){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        Instant temporal = Instant.ofEpochMilli(modDate);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(temporal, ZoneId.systemDefault());
        return dateTimeFormatter.format(localDateTime);
    }

    @POST
    @Path("/get/file")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDirFile(@HeaderParam(X_SESSION_TOKEN) String sessionToken, FileRequest fileRequest) throws SystemException, NotSupportedException {
        DirectoryDTO directoryDTO = fileRequest.getDirectoryDTO();
        FileDTO fileDTO = fileRequest.getFileDTO();
        if (sessionToken == null) {
            return responseService.error(GeneralResponseDTO.authenticationFailed(), 401);
        }
        if (directoryDTO == null || fileDTO == null){
            return responseService.error(GeneralResponseDTO.fileIsNull(), 400);

        }
        userTransaction.begin();
        ApiKey apiKeyBySession = apiKeyService.getApiKeyBySession(sessionToken);
        if (apiKeyBySession == null){
            userTransaction.rollback();
            return responseService.error(GeneralResponseDTO.authenticationFailed(),401);
        }
        Optional<Directory> any = apiKeyBySession.getDirectoryList().stream().filter(d -> d.getLabel().equals(directoryDTO.getLabel()) && d.getPath().equals(directoryDTO.getPath())).findAny();
        if (!any.isPresent()){
            userTransaction.rollback();
            return responseService.error(GeneralResponseDTO.directoryNotFound(),400);
        }

        Optional<File> singleFile = directoryService.getSingleFile(fileDTO.getName(), any.get(), sessionService.getSessionByToken(sessionToken));
        if (!singleFile.isPresent() || !singleFile.get().getName().equals(fileDTO.getName()) || !getDateFromModDate(singleFile.get().lastModified()).equals(fileDTO.getModifyDate())){
            userTransaction.rollback();
            return responseService.error(new GeneralResponseDTO("Could not find file",400),400);
        }
        userTransaction.rollback();
        InputStream targetStream = null;
        try {
            targetStream = new FileInputStream(singleFile.get());
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }

        return responseService.success(targetStream);
    }

}
