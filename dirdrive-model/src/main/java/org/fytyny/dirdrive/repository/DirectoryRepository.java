package org.fytyny.dirdrive.repository;

import org.fytyny.dirdrive.model.Directory;

public interface DirectoryRepository extends Repository<Directory> {

    Directory getByLabel(String label);
    
}
