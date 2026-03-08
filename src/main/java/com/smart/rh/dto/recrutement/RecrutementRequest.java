package com.smart.rh.dto.recrutement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecrutementRequest(

    @NotBlank
    @Size(max = 150)
    String posteCible,

    @Size(max = 50)
    String statut,

    Long responsableId
) {}
