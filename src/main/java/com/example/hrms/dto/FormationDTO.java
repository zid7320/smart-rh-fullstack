package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormationDTO {
    private Integer idFormation;
    
    @NotBlank(message = "Title is mandatory")
    private String titre;
    
    @NotBlank(message = "Certification is mandatory")
    private String certification;
    
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
