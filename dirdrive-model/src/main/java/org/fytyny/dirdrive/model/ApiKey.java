package org.fytyny.dirdrive.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class ApiKey {

    @Id
    UUID id;

    String token;

    @OneToMany
    List<Directory> directoryList;

}
