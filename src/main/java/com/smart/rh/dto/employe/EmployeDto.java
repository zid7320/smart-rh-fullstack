package com.smart.rh.dto.employe;

import java.time.Instant;

public record EmployeDto(
    Long id,
    String nom,
    String prenom,
    String email,
    String posteLibelle,
    Long posteId,
    String posteTitle,
    Long recrutementId,
    Long userId,
    Instant createdAt,
    Instant updatedAt
) {}
