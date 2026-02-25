package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CongeDTO {
    private Integer idConge;
    
    @NotBlank(message = "Type de conge is mandatory")
    private String type;
    
    @NotNull(message = "Date debut is mandatory")
    @PastOrPresent(message = "Date debut must be in past or present")
    private Date dateDebut;
    
    @NotNull(message = "Date fin is mandatory")
    private Date dateFin;
}
