package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponsableRHDTO {
    private Integer idResponsable;
    
    @NotBlank(message = "Nom is mandatory")
    private String nom;
}
