package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecrutementDTO {
    private Integer idRecrutement;
    
    @NotBlank(message = "Poste cible is mandatory")
    private String posteCible;
    
    @NotBlank(message = "Statut is mandatory")
    private String statut;
}
