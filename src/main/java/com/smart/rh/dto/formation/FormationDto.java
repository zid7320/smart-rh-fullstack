package com.smart.rh.dto.formation;

import java.time.Instant;
import java.time.LocalDate;

public record FormationDto(
    Long id,
    Long employeId,
    String employeNomComplet,
    String titre,
    String certification,
    LocalDate dateDebut,
    LocalDate dateFin,
    Instant createdAt,
    Instant updatedAt
) {}
