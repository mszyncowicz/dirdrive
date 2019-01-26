package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Directory;

import java.io.File;
import java.util.List;

public interface DirectoryRepository extends Repository<Directory> {

    Directory getByLabel(String label);
    
}
