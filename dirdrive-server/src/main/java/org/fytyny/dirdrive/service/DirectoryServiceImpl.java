package org.fytyny.dirdrive.service;

import lombok.Setter;
import org.fytyny.dirdrive.model.ApiKey;
import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;
import org.fytyny.dirdrive.repository.DirectoryRepository;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class DirectoryServiceImpl implements DirectoryService {

    @Any
    @Inject
    ApiKeyService apiKeyService;

    @Any
    @Inject
    DirectoryRepository directoryRepository;

    @Override
    public List<String> getAllAvaiblePaths(Session session) {
        ApiKey apiKeyBySession = getBySessionToken(session.getToken());
        return apiKeyBySession.getDirectoryList().stream()
                .map(d -> d.getPath()).collect(Collectors.toList());
    }

    @Override
    public Optional<Directory> getDirByLabel(String label, Session session) {
        ApiKey bySessionToken = getBySessionToken(session.getToken());
        return bySessionToken.getDirectoryList().stream()
            .filter(d -> d.getLabel().equals(label)).findAny();
    }

    ApiKey getBySessionToken(String token){
        return apiKeyService.getApiKeyBySession(token);
    }

    @Override
    public boolean addDirectoryToApiKey(Directory directory, Session session) {
        ApiKey bySessionToken = getBySessionToken(session.getToken());
        boolean result = !bySessionToken.getDirectoryList().stream().map(d -> d.getLabel()).collect(Collectors.toList()).contains(directory.getLabel());
        if (result){
            directoryRepository.save(directory);
            bySessionToken.getDirectoryList().add(directory);
            apiKeyService.save(bySessionToken);
        }
        return result;
    }

    @Override
    public List<File> getFilesOfDir(Directory directory, Session session) {
        ApiKey bySessionToken = getBySessionToken(session.getToken());
        if (!apiKeyService.containsDirectory(bySessionToken,directory)){
            throw new IllegalArgumentException();
        }
        File file = new File(directory.getPath());
        return new LinkedList<File>(Arrays.stream(file.listFiles()).filter(f->!f.isDirectory()).collect(Collectors.toList()));
    }

    @Override
    public Optional<File> getSingleFile(String fileName, Directory directory, Session session) {
        List<File> filesOfDir = getFilesOfDir(directory, session);
        return filesOfDir.stream().filter(f -> f.getName().equals(fileName)).findAny();
    }
}
