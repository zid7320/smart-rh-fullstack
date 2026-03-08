package com.smart.rh.dto.formation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FormationRequest(

    @NotNull
    Long employeId,

    @NotBlank
    @Size(max = 200)
    String titre,

    @Size(max = 200)
    String certification,

    LocalDate dateDebut,

    LocalDate dateFin
) {}
