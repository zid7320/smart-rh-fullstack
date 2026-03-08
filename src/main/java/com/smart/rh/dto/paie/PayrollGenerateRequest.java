package com.smart.rh.dto.paie;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/payroll/generate.
 * Generates payroll records for ALL active employees for the given month/year.
 */
public record PayrollGenerateRequest(

    @NotNull
    @Min(1) @Max(12)
    Integer mois,

    @NotNull
    @Min(2000)
    Integer annee
) {}
