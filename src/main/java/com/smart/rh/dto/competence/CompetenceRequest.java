package com.smart.rh.dto.competence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompetenceRequest(

    @NotBlank
    @Size(max = 100)
    String nom,

    @Size(max = 50)
    String niveau
) {}
