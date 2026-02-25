package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetenceDTO {
    private Integer idCompetence;
    
    @NotBlank(message = "Nom is mandatory")
    private String nom;
    
    @NotBlank(message = "Niveau is mandatory")
    private String niveau;
}
