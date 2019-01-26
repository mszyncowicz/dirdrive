package org.fytyny.dirdrive.service;

import org.fytyny.dirdrive.model.Directory;
import org.fytyny.dirdrive.model.Session;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface DirectoryService {

    List<String> getAllAvaiblePaths(Session session);

    Optional<Directory> getDirByLabel(String label, Session session);

    boolean addDirectoryToApiKey(Directory directory, Session session);

    List<File> getFilesOfDir(Directory directory, Session session);

    Optional<File> getSingleFile(String fileName, Directory directory, Session session);
}
