package com.smart.rh.dto.competence;

import java.time.Instant;

public record CompetenceDto(
    Long id,
    String nom,
    String niveau,
    Instant createdAt,
    Instant updatedAt
) {}
