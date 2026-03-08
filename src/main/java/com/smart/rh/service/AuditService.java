package com.smart.rh.service;

import com.smart.rh.entity.AuditLog;
import com.smart.rh.repository.AuditLogRepository;
import com.smart.rh.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centralised audit service — the single point of contact with {@link AuditLogRepository}.
 *
 * <h3>Propagation strategy</h3>
 * Every {@code log*} method runs in {@link Propagation#REQUIRES_NEW}.
 * This has two important consequences:
 * <ol>
 *   <li>A rollback of the caller's transaction (e.g. a failed payroll run) does
 *       <em>not</em> erase the audit entry — compliance traces survive errors.</li>
 *   <li>An audit-write failure never rolls back the caller's transaction —
 *       a broken audit sink must not stop HR operations.</li>
 * </ol>
 *
 * <h3>Data-safety rules</h3>
 * <ul>
 *   <li>Passwords (hashed or plain), JWT tokens, face-encoding blobs, and raw
 *       image paths are <em>never</em> included in {@code payloadSummary}.</li>
 *   <li>Summaries contain only entity IDs, human-readable names, status labels,
 *       and aggregate counts — never full payload bodies.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    // ── Context-aware overloads (JWT-authenticated requests) ─────────────────

    /**
     * Log an event. Actor is resolved automatically from Spring Security context.
     * Use for all endpoints protected by JWT (i.e. everything except auth endpoints).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityName, Long entityId, String summary) {
        persist(action, entityName, entityId, summary, null,
                resolveActorId(), resolveActorRole());
    }

    /**
     * Same as {@link #log(String, String, Long, String)} but also stores the
     * client IP address (optional, best-effort).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityName, Long entityId,
                    String summary, String ipAddress) {
        persist(action, entityName, entityId, summary, ipAddress,
                resolveActorId(), resolveActorRole());
    }

    // ── Explicit-actor overload (auth events) ─────────────────────────────────

    /**
     * Log an event with an explicitly supplied actor.
     * Use at authentication time (login / register / login-failed) when the
     * Security context is not yet — or is no longer — populated with the actor.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAs(String action, String entityName, Long entityId,
                      Long actorId, String actorRole,
                      String summary, String ipAddress) {
        persist(action, entityName, entityId, summary, ipAddress, actorId, actorRole);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void persist(String action, String entityName, Long entityId,
                         String summary, String ipAddress,
                         Long actorId, String actorRole) {
        try {
            repository.save(AuditLog.of(
                    action, entityName, entityId,
                    actorId, actorRole, summary, ipAddress));
        } catch (Exception e) {
            // Never propagate — a broken audit sink must not disrupt HR operations.
            log.warn("Audit write failed [action={}, entity={}, id={}]: {}",
                    action, entityName, entityId, e.getMessage());
        }
    }

    // ── Security-context helpers ──────────────────────────────────────────────

    private Long resolveActorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return ud.getUser().getId();
        }
        return null;
    }

    private String resolveActorRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return ud.getUser().getRole().name();
        }
        return null;
    }
}
