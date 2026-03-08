package com.smart.rh.dto.contrat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratRequest(

    @NotNull
    Long employeId,

    @NotBlank
    @Size(max = 50)
    String type,

    @NotNull
    LocalDate dateDebut,

    /** Null for CDI. */
    LocalDate dateFin,

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal salaire
) {}
