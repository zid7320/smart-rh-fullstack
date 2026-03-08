package com.smart.rh.dto.employe;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeRequest(

    @NotBlank
    @Size(max = 100)
    String nom,

    @NotBlank
    @Size(max = 100)
    String prenom,

    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    /** Optional free-text job title. */
    @Size(max = 150)
    String posteLibelle,

    /** FK to Poste entity (optional). */
    Long posteId
) {}
