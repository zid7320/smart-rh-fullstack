-- ============================================================
-- V2 — Core HR: responsable_rh · poste · competence ·
--              poste_competence · recrutement · candidate · employe
-- ============================================================

-- ── ResponsableRH ────────────────────────────────────────────
CREATE TABLE responsable_rh (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nom         VARCHAR(100) NOT NULL,
    user_id     BIGINT           NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_responsable_user (user_id),
    CONSTRAINT fk_responsable_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Poste ─────────────────────────────────────────────────────
CREATE TABLE poste (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    titre                 VARCHAR(150) NOT NULL,
    competences_requises  TEXT             NULL COMMENT 'Legacy free-text; normalised via poste_competence',
    created_at            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Competence ────────────────────────────────────────────────
CREATE TABLE competence (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nom         VARCHAR(100) NOT NULL,
    niveau      VARCHAR(50)      NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Poste ↔ Competence (Many-to-Many with extra column) ───────
CREATE TABLE poste_competence (
    poste_id        BIGINT      NOT NULL,
    competence_id   BIGINT      NOT NULL,
    niveau_requis   VARCHAR(50)     NULL,
    PRIMARY KEY (poste_id, competence_id),
    CONSTRAINT fk_pc_poste       FOREIGN KEY (poste_id)      REFERENCES poste      (id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_competence  FOREIGN KEY (competence_id) REFERENCES competence (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Recrutement ───────────────────────────────────────────────
CREATE TABLE recrutement (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    poste_cible     VARCHAR(150) NOT NULL,
    statut          VARCHAR(50)  NOT NULL DEFAULT 'OUVERT',
    responsable_id  BIGINT           NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_recrutement_responsable (responsable_id),
    CONSTRAINT fk_recrutement_responsable FOREIGN KEY (responsable_id)
        REFERENCES responsable_rh (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Candidate ─────────────────────────────────────────────────
CREATE TABLE candidate (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    recrutement_id  BIGINT           NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_candidate_recrutement (recrutement_id),
    CONSTRAINT fk_candidate_recrutement FOREIGN KEY (recrutement_id)
        REFERENCES recrutement (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Employe ───────────────────────────────────────────────────
CREATE TABLE employe (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nom             VARCHAR(100) NOT NULL,
    prenom          VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    poste_libelle   VARCHAR(150)     NULL COMMENT 'Legacy free-text; normalised via poste_id',
    poste_id        BIGINT           NULL,
    recrutement_id  BIGINT           NULL,
    user_id         BIGINT           NULL,
    face_encoding   MEDIUMTEXT       NULL COMMENT 'Base64 face embedding for AI recognition',
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_employe_email   (email),
    UNIQUE KEY uk_employe_user    (user_id),
    KEY idx_employe_poste         (poste_id),
    KEY idx_employe_recrutement   (recrutement_id),
    CONSTRAINT fk_employe_poste        FOREIGN KEY (poste_id)       REFERENCES poste       (id) ON DELETE SET NULL,
    CONSTRAINT fk_employe_recrutement  FOREIGN KEY (recrutement_id) REFERENCES recrutement (id) ON DELETE SET NULL,
    CONSTRAINT fk_employe_user         FOREIGN KEY (user_id)        REFERENCES users       (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
