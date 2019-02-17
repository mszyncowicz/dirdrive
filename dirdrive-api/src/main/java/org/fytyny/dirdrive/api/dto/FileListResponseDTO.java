package org.fytyny.dirdrive.api.dto;


import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class FileListResponseDTO {
    Set<FileDTO> fileDTOSet;
}
