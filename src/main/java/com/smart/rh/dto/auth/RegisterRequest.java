package com.smart.rh.dto.auth;

import com.smart.rh.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registration request.
 *
 * <p>The {@code role} field defaults to {@code ROLE_EMPLOYEE} in the service
 * when null, so callers may omit it.  Only an authenticated ADMIN should be
 * able to create accounts with elevated roles (enforced at the service layer).
 */
public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 100, message = "Username must be 3–100 characters")
        String username,

        @Email(message = "Must be a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        /** Optional — defaults to ROLE_EMPLOYEE when null. */
        Role role
) {}
