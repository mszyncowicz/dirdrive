package org.fytyny.dirdrive.api.dto;

import lombok.Data;
import org.fytyny.dirdrive.model.Directory;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement
public class DirectoryDTO {
    private String label;
    private String path;

    public static DirectoryDTO getFrom(Directory directory){
        DirectoryDTO directoryDTO = new DirectoryDTO();
        directoryDTO.setPath(directory.getPath());
        directoryDTO.setLabel(directory.getLabel());
        return directoryDTO;
    }

    public static DirectoryDTO fromString(String from){
        return new DirectoryDTO();
    }
}
