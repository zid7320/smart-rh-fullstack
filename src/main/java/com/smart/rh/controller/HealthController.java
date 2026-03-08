package com.smart.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Application health & RBAC smoke-test endpoints")
public class HealthController {

    // ── Public heath check ────────────────────────────────────────────────────

    @GetMapping("/health")
    @Operation(summary = "Health check — public",
               description = "Returns UP when the application is running correctly.")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",      "UP");
        body.put("application", "SMART RH 4.0");
        body.put("version",     "1.0.0-SNAPSHOT");
        body.put("timestamp",   Instant.now().toString());
        return ResponseEntity.ok(body);
    }

    // ── RBAC smoke-test endpoints (used by integration tests) ─────────────────

    /** Requires ROLE_ADMIN — returns 403 for any other role. */
    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Admin-only ping (RBAC test)")
    public ResponseEntity<Map<String, String>> adminPing() {
        return ResponseEntity.ok(Map.of("message", "pong — ADMIN access confirmed"));
    }

    /** Requires ROLE_RH or ROLE_ADMIN. */
    @GetMapping("/rh/ping")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "RH-or-Admin ping (RBAC test)")
    public ResponseEntity<Map<String, String>> rhPing() {
        return ResponseEntity.ok(Map.of("message", "pong — RH access confirmed"));
    }
}
