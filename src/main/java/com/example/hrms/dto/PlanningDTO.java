package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanningDTO {
    private Integer idPlanning;
    
    @NotBlank(message = "Hour schedule is mandatory")
    private String horaires;
    
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
}
