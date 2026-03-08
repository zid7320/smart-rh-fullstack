package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Immutable compliance record: every action that creates, modifies or deletes
 * an HR resource — or a security event (login/logout) — is appended here.
 * Passwords and other secrets must NEVER appear in payloadSummary.
 */
@Entity
@Table(
    name = "audit_log",
    indexes = {
        @Index(name = "idx_audit_actor", columnList = "actor_user_id"),
        @Index(name = "idx_audit_ts",    columnList = "ts")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class AuditLog extends BaseEntity {

    /** Null for anonymous/system events. */
    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_role", length = 50)
    private String actorRole;

    /** LOGIN | LOGOUT | CREATE | UPDATE | DELETE | APPROVE | REJECT | SEED | … */
    @NotBlank
    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "entity_name", length = 100)
    private String entityName;

    @Column(name = "entity_id", length = 50)
    private String entityId;

    @Column(name = "payload_summary", columnDefinition = "TEXT")
    private String payloadSummary;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Business-event timestamp (may differ slightly from createdAt on async paths). */
    @Column(name = "ts", nullable = false)
    private Instant ts = Instant.now();

    // ── Convenience factory ──

    public static AuditLog of(String action, String entityName, Long entityId,
                               Long actorUserId, String actorRole,
                               String payloadSummary, String ipAddress) {
        AuditLog log = new AuditLog();
        log.action          = action;
        log.entityName      = entityName;
        log.entityId        = entityId == null ? null : String.valueOf(entityId);
        log.actorUserId     = actorUserId;
        log.actorRole       = actorRole;
        log.payloadSummary  = payloadSummary;
        log.ipAddress       = ipAddress;
        return log;
    }
}
