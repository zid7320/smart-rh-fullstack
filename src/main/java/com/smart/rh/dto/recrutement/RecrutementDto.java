package com.smart.rh.dto.recrutement;

import java.time.Instant;

public record RecrutementDto(
    Long id,
    String posteCible,
    String statut,
    Long responsableId,
    String responsableNom,
    Instant createdAt,
    Instant updatedAt
) {}
