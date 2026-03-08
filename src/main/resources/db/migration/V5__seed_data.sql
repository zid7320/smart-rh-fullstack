-- ============================================================
-- V5 — Seed Data
--
-- Demo credentials  (BCrypt rounds=10):
--   admin@smartrh.com    / Admin@2024
--   rh@smartrh.com       / Rh@2024
--   alice@smartrh.com    / Employee@2024
--   bob@smartrh.com      / Employee@2024
-- ============================================================

-- ── Users ─────────────────────────────────────────────────────
INSERT INTO users (username, email, password, role, enabled)
VALUES
    ('admin',
     'admin@smartrh.com',
     '$2a$10$0QXuTZAHtANZC8JIlgI9sudkN/DhFppGfgUMG3LPX9YGJQ3YeM7k6',
     'ROLE_ADMIN',
     1),
    ('rhmanager',
     'rh@smartrh.com',
     '$2a$10$z2Fh7YtFKoFElZkMV9yMT.wBXIAiqifvvIhwU93nozoAdSAsVC/eS',
     'ROLE_RH',
     1),
    ('alice.dubois',
     'alice@smartrh.com',
     '$2a$10$/lmj7oxiyY9VgeEhqnlXyenGwZk.iipiR9c.nV9yIQQ90n3D/7/Mq',
     'ROLE_EMPLOYEE',
     1),
    ('bob.martin',
     'bob@smartrh.com',
     '$2a$10$/lmj7oxiyY9VgeEhqnlXyenGwZk.iipiR9c.nV9yIQQ90n3D/7/Mq',
     'ROLE_EMPLOYEE',
     1);

-- ── ResponsableRH ─────────────────────────────────────────────
INSERT INTO responsable_rh (nom, user_id)
VALUES ('Marie Dupont', 2);  -- linked to rhmanager user

-- ── Poste ─────────────────────────────────────────────────────
INSERT INTO poste (titre, competences_requises)
VALUES
    ('Développeur Java Senior', 'Java, Spring Boot, SQL, Docker'),
    ('Chef de Projet IT',       'Gestion de projet, Agile, Communication');

-- ── Competence ────────────────────────────────────────────────
INSERT INTO competence (nom, niveau)
VALUES
    ('Java',        'Expert'),
    ('Spring Boot', 'Avancé'),
    ('SQL',         'Intermédiaire'),
    ('Docker',      'Intermédiaire'),
    ('Agile/Scrum', 'Avancé');

-- ── Poste ↔ Competence ────────────────────────────────────────
INSERT INTO poste_competence (poste_id, competence_id, niveau_requis)
VALUES
    (1, 1, 'Expert'),
    (1, 2, 'Avancé'),
    (1, 3, 'Intermédiaire'),
    (1, 4, 'Débutant'),
    (2, 5, 'Avancé');

-- ── Recrutement ───────────────────────────────────────────────
INSERT INTO recrutement (poste_cible, statut, responsable_id)
VALUES ('Développeur Java Senior', 'OUVERT', 1);

-- ── Candidate ─────────────────────────────────────────────────
INSERT INTO candidate (nom, prenom, email, recrutement_id)
VALUES
    ('Martin',   'Jean',   'jean.martin@example.com',   1),
    ('Bernard',  'Sophie', 'sophie.bernard@example.com', 1);

-- ── Employe ───────────────────────────────────────────────────
INSERT INTO employe (nom, prenom, email, poste_libelle, poste_id, user_id)
VALUES
    ('Dubois', 'Alice', 'alice@smartrh.com', 'Développeur Java Senior', 1, 3),
    ('Martin', 'Bob',   'bob@smartrh.com',   'Développeur Java Senior', 1, 4);

-- ── DossierRH (one per employe) ───────────────────────────────
INSERT INTO dossier_rh (employe_id, infos_perso, diplomes, documents)
VALUES
    (1, 'Alice Dubois — née le 15/03/1992 à Paris', 'Master Informatique — Université Paris VI (2015)', ''),
    (2, 'Bob Martin — né le 22/07/1990 à Lyon',     'Licence Informatique — Université Lyon I (2013)',  '');

-- ── Contrat (CDI pour Alice et Bob) ───────────────────────────
INSERT INTO contrat (employe_id, type, date_debut, salaire)
VALUES
    (1, 'CDI', '2022-01-10', 4500.00),
    (2, 'CDI', '2021-09-01', 4200.00);

-- ── AuditLog — initial seed event ────────────────────────────
INSERT INTO audit_log (actor_user_id, actor_role, action, entity_name, entity_id, payload_summary, ip_address)
VALUES
    (1, 'ROLE_ADMIN', 'SEED', 'ALL', NULL, 'Initial database seed executed by Flyway V5', '127.0.0.1');
