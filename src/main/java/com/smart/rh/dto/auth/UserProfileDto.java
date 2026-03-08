package com.smart.rh.dto.auth;

/**
 * Current-user profile returned by {@code GET /api/auth/me}.
 */
public record UserProfileDto(
        Long id,
        String username,
        String email,
        String role,
        Boolean enabled
) {}
