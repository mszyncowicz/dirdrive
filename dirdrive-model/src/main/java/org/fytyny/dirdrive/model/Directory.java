package org.fytyny.dirdrive.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;


@Data
@Entity
public class Directory {

    @JsonIgnore
    @Id
    UUID id;

    String path;

    String label;

}
