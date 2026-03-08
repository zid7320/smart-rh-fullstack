package com.smart.rh.dto.evaluation;

import java.time.Instant;
import java.time.LocalDate;

public record EvaluationDto(
    Long id,
    Long employeId,
    String employeNomComplet,
    String objectifs,
    String kpi,
    LocalDate dateEvaluation,
    Instant createdAt,
    Instant updatedAt
) {}
