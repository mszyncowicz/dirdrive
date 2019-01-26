package org.fytyny.dirdrive.api.dto;


import lombok.Data;

import java.util.List;

@Data
public class FileListResponseDTO {
    List<FileDTO> fileDTOList;
}
