package com.smart.rh.dto.recrutement;

import jakarta.validation.constraints.NotNull;

/**
 * Payload for the POST /api/recruitments/{id}/hire endpoint.
 * Converts a specific Candidate into an Employee.
 */
public record HireRequest(

    @NotNull
    Long candidateId,

    /** Optional poste to assign to the new employee. */
    Long posteId,

    /** Optional email override (defaults to candidate's email). */
    String email
) {}
