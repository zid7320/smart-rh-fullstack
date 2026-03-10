package com.smart.rh.dto.planning;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PlanningRequest(

    @NotNull
    Long employeId,

    @Size(max = 255)
    String horaires,

    LocalDate dateDebut,

    LocalDate dateFin,

    @Size(max = 100)
    String type
) {}
