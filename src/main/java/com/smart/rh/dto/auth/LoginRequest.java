package com.smart.rh.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request — accepts username OR email in the {@code usernameOrEmail} field.
 */
public record LoginRequest(

        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        String password
) {}
