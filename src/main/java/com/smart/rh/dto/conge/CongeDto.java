package com.smart.rh.dto.conge;

import java.time.Instant;
import java.time.LocalDate;

public record CongeDto(
    Long id,
    Long employeId,
    String employeNomComplet,
    String type,
    LocalDate dateDebut,
    LocalDate dateFin,
    String statut,
    Instant requestedAt,
    Instant decidedAt,
    Long decidedByUserId,
    Instant createdAt,
    Instant updatedAt
) {}
