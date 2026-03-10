package com.smart.rh.dto.planning;

import java.time.Instant;
import java.time.LocalDate;

public record PlanningDto(
    Long id,
    Long employeId,
    String employeNomComplet,
    String horaires,
    LocalDate dateDebut,
    LocalDate dateFin,
    String type,
    Instant createdAt,
    Instant updatedAt
) {}
