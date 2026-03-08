-- ============================================================
-- V3 — HR Operations: dossier_rh · contrat · planning ·
--                     conge · paie · evaluation · formation
-- ============================================================

-- ── DossierRH (1-to-1 with Employe) ──────────────────────────
CREATE TABLE dossier_rh (
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    employe_id  BIGINT  NOT NULL,
    infos_perso TEXT        NULL,
    diplomes    TEXT        NULL,
    documents   TEXT        NULL,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_dossier_employe (employe_id),
    CONSTRAINT fk_dossier_employe FOREIGN KEY (employe_id)
        REFERENCES employe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Contrat ───────────────────────────────────────────────────
CREATE TABLE contrat (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    employe_id  BIGINT          NOT NULL,
    type        VARCHAR(50)     NOT NULL COMMENT 'CDI, CDD, STAGE, etc.',
    date_debut  DATE            NOT NULL,
    date_fin    DATE                NULL,
    salaire     DECIMAL(12, 2)  NOT NULL,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)         NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_contrat_employe (employe_id),
    CONSTRAINT fk_contrat_employe FOREIGN KEY (employe_id)
        REFERENCES employe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Planning ──────────────────────────────────────────────────
CREATE TABLE planning (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    employe_id  BIGINT       NOT NULL,
    horaires    VARCHAR(255)     NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_planning_employe (employe_id),
    CONSTRAINT fk_planning_employe FOREIGN KEY (employe_id)
        REFERENCES employe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Conge (Leave) — includes workflow fields ──────────────────
CREATE TABLE conge (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    employe_id    BIGINT      NOT NULL,
    type          VARCHAR(50) NOT NULL COMMENT 'ANNUAL, SICK, MATERNITY, etc.',
    date_debut    DATE        NOT NULL,
    date_fin      DATE        NOT NULL,
    statut        VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | APPROVED | REJECTED',
    requested_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    decided_at    DATETIME(6)     NULL,
    decided_by    BIGINT          NULL COMMENT 'FK → users.id of the approver',
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_conge_employe  (employe_id),
    KEY idx_conge_statut   (statut),
    KEY idx_conge_decided  (decided_by),
    CONSTRAINT fk_conge_employe   FOREIGN KEY (employe_id) REFERENCES employe (id) ON DELETE CASCADE,
    CONSTRAINT fk_conge_decidedby FOREIGN KEY (decided_by) REFERENCES users   (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Paie (Payroll) ────────────────────────────────────────────
CREATE TABLE paie (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    employe_id    BIGINT         NOT NULL,
    montant       DECIMAL(12, 2) NOT NULL,
    mois          TINYINT        NOT NULL COMMENT '1–12',
    annee         SMALLINT       NOT NULL,
    bulletin_pdf  VARCHAR(500)       NULL COMMENT 'Path/URL to generated PDF file',
    created_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)        NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_paie_employe_mois_annee (employe_id, mois, annee),
    KEY idx_paie_annee (annee),
    CONSTRAINT fk_paie_employe FOREIGN KEY (employe_id)
        REFERENCES employe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Evaluation ────────────────────────────────────────────────
CREATE TABLE evaluation (
    id               BIGINT  NOT NULL AUTO_INCREMENT,
    employe_id       BIGINT  NOT NULL,
    objectifs        TEXT        NULL,
    kpi              VARCHAR(500) NULL,
    date_evaluation  DATE        NULL,
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)     NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_evaluation_employe (employe_id),
    CONSTRAINT fk_evaluation_employe FOREIGN KEY (employe_id)
        REFERENCES employe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Formation (Training) ──────────────────────────────────────
CREATE TABLE formation (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    employe_id     BIGINT       NOT NULL,
    titre          VARCHAR(200) NOT NULL,
    certification  VARCHAR(200)     NULL,
    date_debut     DATE             NULL,
    date_fin       DATE             NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_formation_employe (employe_id),
    CONSTRAINT fk_formation_employe FOREIGN KEY (employe_id)
        REFERENCES employe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
