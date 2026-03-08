package com.smart.rh.dto.evaluation;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EvaluationRequest(

    @NotNull
    Long employeId,

    String objectifs,

    String kpi,

    LocalDate dateEvaluation
) {}
