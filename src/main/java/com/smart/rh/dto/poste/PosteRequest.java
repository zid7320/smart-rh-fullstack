package com.smart.rh.dto.poste;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PosteRequest(

    @NotBlank
    @Size(max = 150)
    String titre,

    String competencesRequises,

    /** IDs of Competence entities to link. */
    List<Long> competenceIds
) {}
