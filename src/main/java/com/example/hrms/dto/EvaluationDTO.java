package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {
    private Integer idEval;
    
    @NotBlank(message = "Objectives are mandatory")
    private String objectifs;
    
    @NotBlank(message = "KPI is mandatory")
    private String kpi;
    
    private LocalDate dateEvaluation;
}
