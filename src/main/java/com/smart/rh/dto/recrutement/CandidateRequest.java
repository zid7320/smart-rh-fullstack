package com.smart.rh.dto.recrutement;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CandidateRequest(

    @NotBlank
    @Size(max = 100)
    String nom,

    @NotBlank
    @Size(max = 100)
    String prenom,

    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    Long recrutementId
) {}
