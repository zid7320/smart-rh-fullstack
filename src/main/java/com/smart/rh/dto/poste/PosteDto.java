package com.smart.rh.dto.poste;

import java.time.Instant;
import java.util.List;

public record PosteDto(
    Long id,
    String titre,
    String competencesRequises,
    List<Long> competenceIds,
    List<String> competenceNoms,
    Instant createdAt,
    Instant updatedAt
) {}
