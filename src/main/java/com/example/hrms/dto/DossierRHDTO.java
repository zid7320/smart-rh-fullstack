package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierRHDTO {
    private Integer idDossier;
    
    @NotBlank(message = "Document type is mandatory")
    private String typeDocument;
    
    @NotBlank(message = "File path is mandatory")
    private String cheminFichier;
}
