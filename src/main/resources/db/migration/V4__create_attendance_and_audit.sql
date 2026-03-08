-- ============================================================
-- V4 — Attendance (AI module) & AuditLog
-- ============================================================

-- ── Attendance ────────────────────────────────────────────────
-- Populated by the Python AI recognition service via
-- POST /api/attendance/recognition and/or the MQTT listener.
CREATE TABLE attendance (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    employe_id      BIGINT          NOT NULL,
    type            VARCHAR(10)     NOT NULL COMMENT 'IN | OUT',
    clocked_at      DATETIME(6)     NOT NULL,
    confidence      DOUBLE              NULL COMMENT 'AI confidence score 0.0–1.0',
    camera_id       VARCHAR(100)        NULL,
    site_id         VARCHAR(100)        NULL,
    raw_image_path  VARCHAR(500)        NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)         NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_att_employe_ts (employe_id, clocked_at),
    KEY idx_att_ts         (clocked_at),
    CONSTRAINT fk_attendance_employe FOREIGN KEY (employe_id)
        REFERENCES employe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── AuditLog ──────────────────────────────────────────────────
-- Security & compliance: captures every CREATE/UPDATE/DELETE/LOGIN
-- event with actor identity, target entity, and timestamp.
CREATE TABLE audit_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    actor_user_id   BIGINT           NULL COMMENT 'NULL for system/anonymous actions',
    actor_role      VARCHAR(50)      NULL,
    action          VARCHAR(50)  NOT NULL COMMENT 'LOGIN | LOGOUT | CREATE | UPDATE | DELETE | APPROVE | REJECT | …',
    entity_name     VARCHAR(100)     NULL,
    entity_id       VARCHAR(50)      NULL,
    payload_summary TEXT             NULL COMMENT 'Safe summary — passwords never logged',
    ip_address      VARCHAR(45)      NULL COMMENT 'IPv4 or IPv6',
    ts              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Event timestamp',
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_audit_actor (actor_user_id),
    KEY idx_audit_ts    (ts)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
