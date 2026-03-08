package com.smart.rh.dto.paie;

import java.math.BigDecimal;
import java.time.Instant;

public record PaieDto(
    Long id,
    Long employeId,
    String employeNomComplet,
    BigDecimal montant,
    Integer mois,
    Integer annee,
    String bulletinPdf,
    Instant createdAt,
    Instant updatedAt
) {}
