package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosteDTO {
    private Integer idPoste;
    
    @NotBlank(message = "Titre is mandatory")
    private String titre;
    
    @NotBlank(message = "Competences requises is mandatory")
    private String competencesRequises;
}
