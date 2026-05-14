-- ============================================================
-- V13 — Resumes (CV Applications)
-- ============================================================

CREATE TABLE resumes (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    full_name         VARCHAR(255)    NOT NULL,
    email             VARCHAR(255)    NOT NULL,
    phone             VARCHAR(20)         NULL,
    skills            LONGTEXT            NULL,
    experience        LONGTEXT            NULL,
    education         LONGTEXT            NULL,
    languages         VARCHAR(500)        NULL,
    sender_email      VARCHAR(255)        NULL,
    email_subject     VARCHAR(500)        NULL,
    received_at       DATETIME(6)         NULL,
    created_at        DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6)         NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_resumes_email (email),
    INDEX idx_resumes_sender_email (sender_email),
    INDEX idx_resumes_received_at (received_at),
    INDEX idx_resumes_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
