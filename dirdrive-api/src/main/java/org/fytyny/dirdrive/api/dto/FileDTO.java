package org.fytyny.dirdrive.api.dto;

import lombok.Data;

@Data
public class FileDTO {

    String name;

    String modifyDate;

    public FileDTO(){

    }

    public FileDTO(String name, String modifyDate){
     this.name= name;
     this.modifyDate = modifyDate;
    }

    public static FileDTO fromString(String a){
        return new FileDTO("sfasf","efasf");
    }
}
