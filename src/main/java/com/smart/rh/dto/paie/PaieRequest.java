package com.smart.rh.dto.paie;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body for creating/updating a single payroll record.
 * Used when overriding a generated bulletin or creating manually.
 */
public record PaieRequest(

    @NotNull
    Long employeId,

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal montant,

    @NotNull
    @Min(1) @Max(12)
    Integer mois,

    @NotNull
    @Min(2000)
    Integer annee
) {}
