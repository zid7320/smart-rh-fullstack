-- ============================================================================
-- V10 — Comprehensive Seed Data for All Modules
-- Populates test data for empty tables to make all app pages show data
-- ============================================================================

-- ============================================================================
-- 1. ADD MORE EMPLOYEES & USERS (for various roles)
-- ============================================================================
INSERT IGNORE INTO users (id, username, email, password, role, enabled) VALUES
(5, 'carol.smith', 'carol@smartrh.com', '$2a$10$/lmj7oxiyY9VgeEhqnlXyenGwZk.iipiR9c.nV9yIQQ90n3D/7/Mq', 'ROLE_EMPLOYEE', 1),
(6, 'david.johnson', 'david@smartrh.com', '$2a$10$/lmj7oxiyY9VgeEhqnlXyenGwZk.iipiR9c.nV9yIQQ90n3D/7/Mq', 'ROLE_EMPLOYEE', 1);

-- Insert more employees
INSERT IGNORE INTO employe (nom, prenom, email, poste_libelle, poste_id, user_id) VALUES
('Smith', 'Carol', 'carol@smartrh.com', 'Chef de Projet IT', 2, 5),
('Johnson', 'David', 'david@smartrh.com', 'Développeur Java Senior', 1, 6);

-- Add dossiers for new employees
INSERT IGNORE INTO dossier_rh (employe_id, infos_perso, diplomes, documents) VALUES
(3, 'Carol Smith - Née le 10/05/1993 à Marseille', 'Master Gestion de Projet - Université Aix-Marseille (2016)', ''),
(4, 'David Johnson - Né le 03/12/1988 à Toulouse', 'Master Informatique - Université Toulouse III (2012)', '');

-- Add contracts for new employees
INSERT IGNORE INTO contrat (employe_id, type, date_debut, salaire) VALUES
(3, 'CDI', '2023-03-15', 3800.00),
(4, 'CDI', '2020-06-01', 5000.00);

-- ============================================================================
-- 2. CONGES (Leave Requests) - Populate for all employees
-- ============================================================================
INSERT INTO conge (employe_id, type, date_debut, date_fin, statut) VALUES
(1, 'CONGÉ_ANNUEL', '2026-05-01', '2026-05-10', 'PENDING'),
(2, 'MALADIE', '2026-04-20', '2026-04-21', 'APPROVED'),
(3, 'CONGÉ_ANNUEL', '2026-06-15', '2026-06-25', 'PENDING'),
(4, 'CONGÉ_PARENTAL', '2026-08-01', '2026-11-30', 'APPROVED'),
(1, 'SANS_SOLDE', '2026-07-15', '2026-07-20', 'PENDING'),
(2, 'CONGÉ_ANNUEL', '2026-09-01', '2026-09-15', 'REJECTED'),
(3, 'MALADIE', '2026-04-22', '2026-04-23', 'APPROVED');

-- ============================================================================
-- 3. PLANNING (Work Schedules)
-- ============================================================================
INSERT INTO planning (employe_id, date_debut, date_fin, horaires, type) VALUES
(1, '2026-04-20', '2026-04-24', '08:00-17:30', 'STANDARD'),
(2, '2026-04-20', '2026-04-24', '08:00-17:30', 'STANDARD'),
(3, '2026-04-20', '2026-04-24', '09:00-18:00', 'FLEXIBLE'),
(4, '2026-04-20', '2026-04-24', '08:00-17:30', 'STANDARD'),
(1, '2026-04-27', '2026-05-01', '09:00-18:00', 'FLEXIBLE'),
(2, '2026-04-27', '2026-05-01', '08:00-17:30', 'STANDARD'),
(3, '2026-04-27', '2026-05-01', '09:00-18:00', 'FLEXIBLE'),
(4, '2026-04-27', '2026-05-01', '08:00-17:30', 'STANDARD');

-- ============================================================================
-- 4. EVALUATION (Performance Reviews)
-- ============================================================================
INSERT INTO evaluation (employe_id, date_evaluation, score, objectifs, kpi) VALUES
(1, '2025-12-15', 18, 'Excellente performance, très motivée, collaboration exceptionnelle', 'KPI 1: +20% productivity'),
(2, '2025-12-15', 16, 'Bon travail, quelques améliorations attendues en communication', 'KPI 2: +15% quality'),
(3, '2025-11-30', 17, 'Excellente gestion de projet, leadership reconnu', 'KPI 3: Project delivery 100%'),
(4, '2025-10-15', 19, 'Leadership exceptionnel, vision stratégique impressionnante', 'KPI 4: Team satisfaction +25%'),
(1, '2024-12-10', 17, 'Bonne progression, investissement en formation louable', 'KPI 5: Learning hours +30%'),
(2, '2024-12-10', 15, 'Performance satisfaisante, développement technique en cours', 'KPI 6: Code quality +10%');

-- ============================================================================
-- 5. FORMATION (Training Programs)
-- ============================================================================
INSERT INTO formation (employe_id, titre, date_debut, date_fin, certification, organisme) VALUES
(1, 'Kubernetes Mastery', '2026-05-15', '2026-05-20', 'CKA', 'Linux Academy'),
(2, 'Advanced Java Design Patterns', '2026-06-01', '2026-06-05', 'Oracle Certified Associate', 'Pluralsight'),
(3, 'Agile Certified Practitioner', '2026-04-25', '2026-04-28', 'ACP', 'Scrum.org'),
(4, 'Spring Boot Advanced', '2026-07-10', '2026-07-15', 'Spring Boot Certified', 'Pluralsight'),
(1, 'Docker & Container Orchestration', '2026-03-01', '2026-03-05', 'Docker Certified', 'Linux Academy'),
(3, 'Leadership et Management', '2026-05-20', '2026-05-22', 'PMP', 'HRplus Academy');

-- ============================================================================
-- 6. PAIE (Payroll Records)
-- ============================================================================
INSERT IGNORE INTO paie (employe_id, mois, annee, montant) VALUES
-- April 2026 - already exists, so IGNORE will skip
(1, 4, 2026, 3150.00),
(2, 4, 2026, 2940.00),
(3, 4, 2026, 2660.00),
(4, 4, 2026, 3500.00),
-- May 2026 (new)
(1, 5, 2026, 3150.00),
(2, 5, 2026, 2940.00),
(3, 5, 2026, 2660.00),
(4, 5, 2026, 3500.00),
-- June 2026 (new)
(1, 6, 2026, 3150.00),
(2, 6, 2026, 2940.00),
(3, 6, 2026, 2660.00),
(4, 6, 2026, 3500.00);

-- ============================================================================
-- 7. ATTENDANCE (Facial Recognition Attendance Records)
-- ============================================================================
INSERT INTO attendance (employe_id, type, clocked_at, confidence, camera_id, site_id) VALUES
-- Today (2026-04-20)
(1, 'IN', '2026-04-20 08:00:00', 98.5, 'CAM-001', 'MAIN'),
(2, 'IN', '2026-04-20 08:15:00', 97.2, 'CAM-001', 'MAIN'),
(3, 'IN', '2026-04-20 09:00:00', 99.1, 'CAM-001', 'MAIN'),
(4, 'IN', '2026-04-20 08:30:00', 96.8, 'CAM-001', 'MAIN'),
(1, 'OUT', '2026-04-20 17:30:00', 98.3, 'CAM-001', 'MAIN'),
-- Yesterday (2026-04-19)
(1, 'IN', '2026-04-19 08:10:00', 98.7, 'CAM-001', 'MAIN'),
(2, 'IN', '2026-04-19 08:00:00', 97.5, 'CAM-001', 'MAIN'),
(3, 'IN', '2026-04-19 08:20:00', 98.9, 'CAM-001', 'MAIN'),
(4, 'IN', '2026-04-19 09:00:00', 95.2, 'CAM-001', 'MAIN'),
(1, 'OUT', '2026-04-19 17:40:00', 98.1, 'CAM-001', 'MAIN'),
(2, 'OUT', '2026-04-19 17:30:00', 97.3, 'CAM-001', 'MAIN'),
-- 2026-04-18
(1, 'IN', '2026-04-18 08:00:00', 98.6, 'CAM-001', 'MAIN'),
(2, 'IN', '2026-04-18 08:05:00', 97.4, 'CAM-001', 'MAIN'),
(3, 'IN', '2026-04-18 08:15:00', 99.0, 'CAM-001', 'MAIN'),
(4, 'IN', '2026-04-18 08:00:00', 96.9, 'CAM-001', 'MAIN'),
(1, 'OUT', '2026-04-18 17:30:00', 98.2, 'CAM-001', 'MAIN'),
-- 2026-04-17
(2, 'IN', '2026-04-17 08:00:00', 97.6, 'CAM-001', 'MAIN'),
(3, 'IN', '2026-04-17 08:10:00', 98.8, 'CAM-001', 'MAIN'),
(4, 'IN', '2026-04-17 08:00:00', 97.0, 'CAM-001', 'MAIN'),
-- 2026-04-16
(1, 'IN', '2026-04-16 08:00:00', 98.5, 'CAM-001', 'MAIN'),
(2, 'IN', '2026-04-16 08:00:00', 97.5, 'CAM-001', 'MAIN'),
(3, 'IN', '2026-04-16 08:05:00', 99.2, 'CAM-001', 'MAIN'),
(4, 'IN', '2026-04-16 08:00:00', 96.8, 'CAM-001', 'MAIN');

-- ============================================================================
-- 8. RECRUTEMENT & CANDIDATES (Additional recruitment data)
-- ============================================================================
INSERT IGNORE INTO recrutement (poste_cible, statut, responsable_id) VALUES
('Développeur Java Senior', 'OUVERT', 1),
('Chef de Projet IT', 'FERMÉ', 1),
('Développeur Frontend Angular', 'OUVERT', 1);

INSERT IGNORE INTO candidate (nom, prenom, email, recrutement_id) VALUES
('Martin', 'Jean', 'jean.martin@example.com', 1),
('Bernard', 'Sophie', 'sophie.bernard@example.com', 1),
('Laurent', 'Pierre', 'pierre.laurent@example.com', 2),
('Moreau', 'Anne', 'anne.moreau@example.com', 3),
('Garcia', 'Miguel', 'miguel.garcia@example.com', 1),
('Chen', 'Wei', 'wei.chen@example.com', 3);

-- ============================================================================
-- VERIFY POPULATION
-- ============================================================================
SELECT CONCAT('Users: ', COUNT(*)) FROM users
UNION ALL
SELECT CONCAT('Employes: ', COUNT(*)) FROM employe
UNION ALL
SELECT CONCAT('Conges: ', COUNT(*)) FROM conge
UNION ALL
SELECT CONCAT('Planning: ', COUNT(*)) FROM planning
UNION ALL
SELECT CONCAT('Evaluation: ', COUNT(*)) FROM evaluation
UNION ALL
SELECT CONCAT('Formation: ', COUNT(*)) FROM formation
UNION ALL
SELECT CONCAT('Paie: ', COUNT(*)) FROM paie
UNION ALL
SELECT CONCAT('Attendance: ', COUNT(*)) FROM attendance
UNION ALL
SELECT CONCAT('Recrutement: ', COUNT(*)) FROM recrutement
UNION ALL
SELECT CONCAT('Candidates: ', COUNT(*)) FROM candidate;
