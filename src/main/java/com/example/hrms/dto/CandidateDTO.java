package com.example.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDTO {
    private Integer idCandidate;
    
    @NotBlank(message = "Nom is mandatory")
    private String nom;
    
    @NotBlank(message = "Prenom is mandatory")
    private String prenom;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;
}
