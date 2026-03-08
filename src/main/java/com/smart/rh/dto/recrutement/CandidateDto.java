package com.smart.rh.dto.recrutement;

import java.time.Instant;

public record CandidateDto(
    Long id,
    String nom,
    String prenom,
    String email,
    Long recrutementId,
    String recrutementPosteCible,
    Instant createdAt,
    Instant updatedAt
) {}
