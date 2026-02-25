package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaieDTO {
    private Integer idPaie;
    
    @NotBlank(message = "Month is mandatory")
    private String mois;
    
    @Positive(message = "Amount must be a positive number")
    private double montant;
    
    private LocalDate datePaie;
}
