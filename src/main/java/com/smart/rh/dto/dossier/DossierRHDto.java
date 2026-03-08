package com.smart.rh.dto.dossier;

import java.time.Instant;

public record DossierRHDto(
    Long id,
    Long employeId,
    String employeNomComplet,
    String infosPerso,
    String diplomes,
    String documents,
    Instant createdAt,
    Instant updatedAt
) {}
