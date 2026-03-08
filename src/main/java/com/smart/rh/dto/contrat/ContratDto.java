package com.smart.rh.dto.contrat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ContratDto(
    Long id,
    Long employeId,
    String employeNomComplet,
    String type,
    LocalDate dateDebut,
    LocalDate dateFin,
    BigDecimal salaire,
    Instant createdAt,
    Instant updatedAt
) {}
