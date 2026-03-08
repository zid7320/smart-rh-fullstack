package com.smart.rh.dto.conge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CongeRequest(

    @NotNull
    Long employeId,

    @NotBlank
    @Size(max = 50)
    String type,

    @NotNull
    LocalDate dateDebut,

    @NotNull
    LocalDate dateFin
) {}
