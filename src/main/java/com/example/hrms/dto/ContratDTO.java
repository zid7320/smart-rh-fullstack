package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratDTO {
    private Integer idContrat;
    
    @NotBlank(message = "Type de contrat is mandatory")
    private String type;
    
    @NotNull(message = "Date debut is mandatory")
    private Date dateDebut;
    
    @NotNull(message = "Date fin is mandatory")
    private Date dateFin;
    
    @Positive(message = "Salaire must be positive")
    @NotNull(message = "Salaire is mandatory")
    private Double salaire;
}
