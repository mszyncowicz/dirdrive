package org.fytyny.dirdrive.api.dto;

import lombok.Data;

@Data
public class GeneralResponseDTO {
    private String message;
    private Integer code;

    public GeneralResponseDTO(){

    }

    public GeneralResponseDTO(String message, Integer code){
        this.message = message;
        this.code = code;
    }
    public static GeneralResponseDTO authenticationFailed() {
        GeneralResponseDTO generalResponseDTO = new GeneralResponseDTO();
        generalResponseDTO.message = "Authentication failed";
        generalResponseDTO.setCode(401);
        return generalResponseDTO;
    }

    public static GeneralResponseDTO fileIsNull() {
        GeneralResponseDTO generalResponseDTO = new GeneralResponseDTO();
        generalResponseDTO.message = "File is null";
        generalResponseDTO.setCode(400);
        return generalResponseDTO;
    }

    public static GeneralResponseDTO directoryNotFound() {
        return new GeneralResponseDTO("Could not find directory", 400);
    }
}
