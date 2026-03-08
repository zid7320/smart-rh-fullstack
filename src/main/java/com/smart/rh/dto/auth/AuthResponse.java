package com.smart.rh.dto.auth;

/**
 * Successful authentication response carrying the issued JWT.
 */
public record AuthResponse(

        String token,
        String tokenType,

        /** Token lifetime in milliseconds. */
        long expiresIn,

        Long userId,
        String username,
        String email,
        String role
) {
    /** Convenience constructor that hard-codes tokenType to "Bearer". */
    public AuthResponse(String token, long expiresIn,
                        Long userId, String username, String email, String role) {
        this(token, "Bearer", expiresIn, userId, username, email, role);
    }
}
