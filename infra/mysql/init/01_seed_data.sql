-- ============================================================================
-- SMART RH 4.0 - Comprehensive Seed Data for MySQL
-- Seeds all necessary data for demo/testing
-- ============================================================================

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 1. ROLES (if not already created)
-- ============================================================================
INSERT IGNORE INTO
    role (id, nom)
VALUES (1, 'ROLE_ADMIN'),
    (2, 'ROLE_RH'),
    (3, 'ROLE_EMPLOYEE');

-- ============================================================================
-- 2. USERS
-- ============================================================================
-- Passwords: Admin@2024, Rh@2024, Employee@2024
INSERT IGNORE INTO
    user (
        id,
        username,
        email,
        password,
        role_id,
        enabled,
        created_at,
        updated_at
    )
VALUES (
        1,
        'admin',
        'admin@smart-rh.com',
        '$2a$10$O9QvMH.qIXtPTZQUqOHkPuK5vV7vK5vV7vK5vV7vK5vV7vK5vV7vK5',
        1,
        1,
        NOW(),
        NOW()
    ),
    (
        2,
        'rhmanager',
        'rh@smart-rh.com',
        '$2a$10$O9QvMH.qIXtPTZQUqOHkPuK5vV7vK5vV7vK5vV7vK5vV7vK5vV7vK5',
        2,
        1,
        NOW(),
        NOW()
    ),
    (
        3,
        'alice.dubois',
        'alice@smart-rh.com',
        '$2a$10$O9QvMH.qIXtPTZQUqOHkPuK5vV7vK5vV7vK5vV7vK5vV7vK5vV7vK5',
        3,
        1,
        NOW(),
        NOW()
    ),
    (
        4,
        'bob.martin',
        'bob@smart-rh.com',
        '$2a$10$O9QvMH.qIXtPTZQUqOHkPuK5vV7vK5vV7vK5vV7vK5vV7vK5vV7vK5',
        3,
        1,
        NOW(),
        NOW()
    ),
    (
        5,
        'carol.smith',
        'carol@smart-rh.com',
        '$2a$10$O9QvMH.qIXtPTZQUqOHkPuK5vV7vK5vV7vK5vV7vK5vV7vK5vV7vK5',
        3,
        1,
        NOW(),
        NOW()
    ),
    (
        6,
        'david.johnson',
        'david@smart-rh.com',
        '$2a$10$O9QvMH.qIXtPTZQUqOHkPuK5vV7vK5vV7vK5vV7vK5vV7vK5vV7vK5',
        3,
        1,
        NOW(),
        NOW()
    );

-- ============================================================================
-- 3. RESPONSABLE_RH (HR Manager)
-- ============================================================================
INSERT IGNORE INTO
    responsable_rh (
        id,
        nom,
        user_id,
        created_at,
        updated_at
    )
VALUES (
        1,
        'Marie Dupont',
        2,
        NOW(),
        NOW()
    );

-- ============================================================================
-- 4. POSTES (Job Positions)
-- ============================================================================
INSERT IGNORE INTO
    poste (
        id,
        titre,
        competences_requises,
        created_at,
        updated_at
    )
VALUES (
        1,
        'Développeur Java Senior',
        'Java, Spring Boot, SQL, Docker, Kubernetes',
        NOW(),
        NOW()
    ),
    (
        2,
        'Chef de Projet IT',
        'Gestion de projet, Agile, Communication, Leadership',
        NOW(),
        NOW()
    ),
    (
        3,
        'Développeur Frontend Angular',
        'Angular, TypeScript, CSS, HTML',
        NOW(),
        NOW()
    ),
    (
        4,
        'Data Scientist',
        'Python, Machine Learning, SQL, Statistics',
        NOW(),
        NOW()
    ),
    (
        5,
        'DevOps Engineer',
        'Docker, Kubernetes, CI/CD, Linux',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 5. COMPETENCES
-- ============================================================================
INSERT IGNORE INTO
    competence (
        id,
        nom,
        niveau,
        created_at,
        updated_at
    )
VALUES (
        1,
        'Java',
        'Expert',
        NOW(),
        NOW()
    ),
    (
        2,
        'Spring Boot',
        'Avancé',
        NOW(),
        NOW()
    ),
    (
        3,
        'SQL',
        'Intermédiaire',
        NOW(),
        NOW()
    ),
    (
        4,
        'Docker',
        'Intermédiaire',
        NOW(),
        NOW()
    ),
    (
        5,
        'Agile/Scrum',
        'Avancé',
        NOW(),
        NOW()
    ),
    (
        6,
        'Angular',
        'Expert',
        NOW(),
        NOW()
    ),
    (
        7,
        'TypeScript',
        'Avancé',
        NOW(),
        NOW()
    ),
    (
        8,
        'Python',
        'Expert',
        NOW(),
        NOW()
    ),
    (
        9,
        'Machine Learning',
        'Intermédiaire',
        NOW(),
        NOW()
    ),
    (
        10,
        'Kubernetes',
        'Intermédiaire',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 6. RECRUTEMENT (Recruitment)
-- ============================================================================
INSERT IGNORE INTO
    recrutement (
        id,
        poste_cible,
        statut,
        responsable_id,
        created_at,
        updated_at
    )
VALUES (
        1,
        'Développeur Java Senior',
        'OUVERT',
        1,
        NOW(),
        NOW()
    ),
    (
        2,
        'Développeur Frontend Angular',
        'FERMÉ',
        1,
        NOW(),
        NOW()
    ),
    (
        3,
        'Data Scientist',
        'OUVERT',
        1,
        NOW(),
        NOW()
    );

-- ============================================================================
-- 7. CANDIDATES
-- ============================================================================
INSERT IGNORE INTO
    candidate (
        id,
        nom,
        prenom,
        email,
        recrutement_id,
        created_at,
        updated_at
    )
VALUES (
        1,
        'Martin',
        'Jean',
        'jean.martin@example.com',
        1,
        NOW(),
        NOW()
    ),
    (
        2,
        'Bernard',
        'Sophie',
        'sophie.bernard@example.com',
        1,
        NOW(),
        NOW()
    ),
    (
        3,
        'Laurent',
        'Pierre',
        'pierre.laurent@example.com',
        2,
        NOW(),
        NOW()
    ),
    (
        4,
        'Moreau',
        'Anne',
        'anne.moreau@example.com',
        3,
        NOW(),
        NOW()
    );

-- ============================================================================
-- 8. EMPLOYES (Employees)
-- ============================================================================
INSERT IGNORE INTO
    employe (
        id,
        nom,
        prenom,
        email,
        poste_libelle,
        poste_id,
        user_id,
        created_at,
        updated_at
    )
VALUES (
        1,
        'Dubois',
        'Alice',
        'alice@smart-rh.com',
        'Développeur Java Senior',
        1,
        3,
        NOW(),
        NOW()
    ),
    (
        2,
        'Martin',
        'Bob',
        'bob@smart-rh.com',
        'Développeur Java Senior',
        1,
        4,
        NOW(),
        NOW()
    ),
    (
        3,
        'Smith',
        'Carol',
        'carol@smart-rh.com',
        'Développeur Frontend Angular',
        3,
        5,
        NOW(),
        NOW()
    ),
    (
        4,
        'Johnson',
        'David',
        'david@smart-rh.com',
        'Chef de Projet IT',
        2,
        6,
        NOW(),
        NOW()
    );

-- ============================================================================
-- 9. DOSSIER_RH (HR Files)
-- ============================================================================
INSERT IGNORE INTO
    dossier_rh (
        id,
        employe_id,
        infos_perso,
        diplomes,
        documents,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        'Alice Dubois - Née le 15/03/1992 à Paris',
        'Master Informatique - Université Paris VI (2015)',
        'Diplôme original, Certificat d\'expérience',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        'Bob Martin - Né le 22/07/1990 à Lyon',
        'Licence Informatique - Université Lyon I (2013)',
        'Diplôme original, Certificat d\'expérience',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        'Carol Smith - Née le 10/05/1993 à Marseille',
        'Master Développement Web - Université Aix-Marseille (2016)',
        'Diplôme original, Certificat d\'expérience',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        'David Johnson - Né le 03/12/1988 à Toulouse',
        'Master Gestion de Projet - Université Toulouse III (2012)',
        'Diplôme original, Certificat d\'expérience',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 10. CONTRATS (Contracts)
-- ============================================================================
INSERT IGNORE INTO
    contrat (
        id,
        employe_id,
        type,
        date_debut,
        date_fin,
        salaire,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        'CDI',
        '2022-01-10',
        NULL,
        4500.00,
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        'CDI',
        '2021-09-01',
        NULL,
        4200.00,
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        'CDI',
        '2023-03-15',
        NULL,
        3800.00,
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        'CDI',
        '2020-06-01',
        NULL,
        5000.00,
        NOW(),
        NOW()
    );

-- ============================================================================
-- 11. ATTENDANCE (Attendance Records)
-- ============================================================================
INSERT IGNORE INTO
    attendance (
        id,
        employe_id,
        date_attendance,
        check_in_time,
        check_out_time,
        status,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        '2026-04-20',
        '08:00:00',
        '17:30:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        '2026-04-20',
        '08:15:00',
        '17:45:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        '2026-04-20',
        '08:05:00',
        '17:35:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        '2026-04-20',
        '08:30:00',
        '18:00:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        5,
        1,
        '2026-04-19',
        '08:10:00',
        '17:40:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        6,
        2,
        '2026-04-19',
        '08:00:00',
        '17:30:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        7,
        3,
        '2026-04-19',
        '08:20:00',
        '17:50:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        8,
        4,
        '2026-04-19',
        '09:00:00',
        '18:00:00',
        'LATE',
        NOW(),
        NOW()
    ),
    (
        9,
        1,
        '2026-04-18',
        '08:00:00',
        '17:30:00',
        'PRESENT',
        NOW(),
        NOW()
    ),
    (
        10,
        2,
        '2026-04-18',
        '08:05:00',
        '17:35:00',
        'PRESENT',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 12. ATTENDANCE_EVENT (Suspicious/Alert Events)
-- ============================================================================
INSERT IGNORE INTO
    attendance_event (
        id,
        employe_id,
        event_type,
        reason,
        event_time,
        is_verified,
        verifier_note,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        'UNUSUAL_TIME',
        'Check-in très tôt (06:30)',
        '2026-04-15 06:30:00',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        'MULTIPLE_CHECK_IN',
        'Deux check-ins à 5 minutes d\'intervalle',
        '2026-04-14 08:00:00',
        1,
        'Erreur système - Approuvé',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        'UNUSUAL_CHECKOUT',
        'Check-out très tard (23:45)',
        '2026-04-13 23:45:00',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        'FAKE_DEVICE',
        'Check-in depuis device non enregistré',
        '2026-04-12 08:00:00',
        1,
        'Device nouvellement enregistré',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 13. CONGES (Leave/Time Off)
-- ============================================================================
INSERT IGNORE INTO
    conge (
        id,
        employe_id,
        type,
        date_debut,
        date_fin,
        statut,
        raison,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        'CONGÉ_ANNUEL',
        '2026-05-01',
        '2026-05-10',
        'APPROUVÉ',
        'Vacances estivales',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        'MALADIE',
        '2026-04-20',
        '2026-04-21',
        'APPROUVÉ',
        'Contrôle médical',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        'CONGÉ_ANNUEL',
        '2026-06-15',
        '2026-06-25',
        'EN_ATTENTE',
        'Demande de congé',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        'CONGÉ_PARENTAL',
        '2026-08-01',
        '2026-11-30',
        'APPROUVÉ',
        'Naissance enfant',
        NOW(),
        NOW()
    ),
    (
        5,
        1,
        'SANS_SOLDE',
        '2026-07-15',
        '2026-07-20',
        'EN_ATTENTE',
        'Voyage personnel',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 14. PLANNING (Work Schedules)
-- ============================================================================
INSERT IGNORE INTO
    planning (
        id,
        employe_id,
        date_debut,
        date_fin,
        horaire_debut,
        horaire_fin,
        type_planning,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        '2026-04-20',
        '2026-04-24',
        '08:00',
        '17:30',
        'STANDARD',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        '2026-04-20',
        '2026-04-24',
        '08:00',
        '17:30',
        'STANDARD',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        '2026-04-20',
        '2026-04-24',
        '08:00',
        '17:30',
        'STANDARD',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        '2026-04-20',
        '2026-04-24',
        '08:00',
        '17:30',
        'STANDARD',
        NOW(),
        NOW()
    ),
    (
        5,
        1,
        '2026-04-27',
        '2026-05-01',
        '09:00',
        '18:00',
        'FLEXIBLE',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 15. EVALUATION
-- ============================================================================
INSERT IGNORE INTO
    evaluation (
        id,
        employe_id,
        date_evaluation,
        note_globale,
        commentaires,
        evaluateur,
        statut,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        '2025-12-15',
        18,
        'Excellente performance, très motivée, collaboration exceptionnelle',
        'Marie Dupont',
        'COMPLÉTÉE',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        '2025-12-15',
        16,
        'Bon travail, quelques améliorations attendues en communication',
        'Marie Dupont',
        'COMPLÉTÉE',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        '2025-11-30',
        17,
        'Talentueuse, code de qualité, un peu d\'expérience nécessaire',
        'Marie Dupont',
        'COMPLÉTÉE',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        '2025-10-15',
        19,
        'Leadership exceptionnel, vision stratégique impressionnante',
        'Marie Dupont',
        'COMPLÉTÉE',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 16. FORMATION (Training)
-- ============================================================================
INSERT IGNORE INTO
    formation (
        id,
        employe_id,
        titre,
        date_debut,
        date_fin,
        prestataire,
        cout,
        statut,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        'Kubernetes Mastery',
        '2026-05-15',
        '2026-05-20',
        'Linux Academy',
        800.00,
        'PLANIFIÉE',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        'Advanced Java Design Patterns',
        '2026-06-01',
        '2026-06-05',
        'Pluralsight',
        600.00,
        'COMPLÉTÉE',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        'Angular Advanced Techniques',
        '2026-04-25',
        '2026-04-28',
        'Udemy',
        250.00,
        'EN_COURS',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        'Agile Certified Practitioner',
        '2026-07-10',
        '2026-07-12',
        'Scrum.org',
        1200.00,
        'PLANIFIÉE',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 17. PAIE (Payroll)
-- ============================================================================
INSERT IGNORE INTO
    paie (
        id,
        employe_id,
        mois,
        annee,
        salaire_brut,
        cotisations_sociales,
        impot_revenu,
        salaire_net,
        date_paiement,
        statut,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        4,
        2026,
        4500.00,
        900.00,
        450.00,
        3150.00,
        '2026-04-30',
        'PAYÉE',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        4,
        2026,
        4200.00,
        840.00,
        420.00,
        2940.00,
        '2026-04-30',
        'PAYÉE',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        4,
        2026,
        3800.00,
        760.00,
        380.00,
        2660.00,
        '2026-04-30',
        'PAYÉE',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        4,
        2026,
        5000.00,
        1000.00,
        500.00,
        3500.00,
        '2026-04-30',
        'PAYÉE',
        NOW(),
        NOW()
    ),
    (
        5,
        1,
        3,
        2026,
        4500.00,
        900.00,
        450.00,
        3150.00,
        '2026-03-31',
        'PAYÉE',
        NOW(),
        NOW()
    ),
    (
        6,
        2,
        3,
        2026,
        4200.00,
        840.00,
        420.00,
        2940.00,
        '2026-03-31',
        'PAYÉE',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 18. DEVICE_TYPE
-- ============================================================================
INSERT IGNORE INTO
    device_type (
        id,
        nom,
        description,
        created_at,
        updated_at
    )
VALUES (
        1,
        'FACIAL_RECOGNITION',
        'Système de reconnaissance faciale pour contrôle d''accès',
        NOW(),
        NOW()
    ),
    (
        2,
        'RFID_READER',
        'Lecteur RFID pour badge d''accès',
        NOW(),
        NOW()
    ),
    (
        3,
        'BIOMETRIC_SCANNER',
        'Scanneur biométrique (empreinte digitale/iris)',
        NOW(),
        NOW()
    ),
    (
        4,
        'TEMPERATURE_SENSOR',
        'Capteur de température corporelle',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 19. DEVICE (IoT Devices)
-- ============================================================================
INSERT IGNORE INTO
    device (
        id,
        nom,
        device_type_id,
        localisation,
        status,
        mac_address,
        ip_address,
        last_heartbeat,
        created_at,
        updated_at
    )
VALUES (
        1,
        'DEVICE_ENTREE_PRINCIPAL',
        1,
        'Entrée principale - Étage 0',
        'ACTIF',
        'AA:BB:CC:DD:EE:01',
        '192.168.1.100',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        2,
        'DEVICE_ETAGE_2',
        1,
        'Étage 2 - Zone de développement',
        'ACTIF',
        'AA:BB:CC:DD:EE:02',
        '192.168.1.101',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        3,
        'DEVICE_RFID_SORTIE',
        2,
        'Sortie de secours',
        'ACTIF',
        'AA:BB:CC:DD:EE:03',
        '192.168.1.102',
        NOW(),
        NOW(),
        NOW()
    ),
    (
        4,
        'DEVICE_BIOMETRIC_RH',
        3,
        'Bureau RH',
        'ACTIF',
        'AA:BB:CC:DD:EE:04',
        '192.168.1.103',
        NOW(),
        NOW(),
        NOW()
    );

-- ============================================================================
-- 20. DEVICE_HEALTH (Device Status Monitoring)
-- ============================================================================
INSERT IGNORE INTO
    device_health (
        id,
        device_id,
        timestamp,
        status,
        température,
        battery_level,
        error_message,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        NOW(),
        'HEALTHY',
        25.5,
        95.0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        NOW(),
        'HEALTHY',
        23.0,
        98.0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        NOW(),
        'HEALTHY',
        22.5,
        85.0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        DATE_SUB(NOW(), INTERVAL 2 DAY),
        'OFFLINE',
        21.0,
        15.0,
        'No heartbeat',
        NOW(),
        NOW()
    );

-- ============================================================================
-- 21. SENSOR_READING (Sensor Data)
-- ============================================================================
INSERT IGNORE INTO
    sensor_reading (
        id,
        device_id,
        sensor_type,
        sensor_value,
        unit,
        timestamp,
        created_at
    )
VALUES (
        1,
        1,
        'TEMPERATURE',
        '25.5',
        'C',
        NOW(),
        NOW()
    ),
    (
        2,
        2,
        'TEMPERATURE',
        '23.0',
        'C',
        NOW(),
        NOW()
    ),
    (
        3,
        3,
        'BATTERY',
        '85.0',
        '%',
        NOW(),
        NOW()
    ),
    (
        4,
        4,
        'BATTERY',
        '15.0',
        '%',
        NOW(),
        NOW()
    ),
    (
        5,
        1,
        'RECOGNITION_CONFIDENCE',
        '98.5',
        '%',
        DATE_SUB(NOW(), INTERVAL 1 HOUR),
        NOW()
    ),
    (
        6,
        2,
        'RECOGNITION_CONFIDENCE',
        '97.2',
        '%',
        DATE_SUB(NOW(), INTERVAL 2 HOUR),
        NOW()
    );

-- ============================================================================
-- 22. ALERT (System Alerts)
-- ============================================================================
INSERT IGNORE INTO
    alert (
        id,
        device_id,
        type,
        message,
        severity,
        resolved,
        resolved_at,
        created_at,
        updated_at
    )
VALUES (
        1,
        1,
        'ANOMALY',
        'Tentative de reconnaissance faciale échouée - Confidant 45%',
        'MEDIUM',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        2,
        3,
        'LOW_BATTERY',
        'Niveau de batterie critique (15%)',
        'HIGH',
        0,
        NULL,
        NOW(),
        NOW()
    ),
    (
        3,
        4,
        'OFFLINE',
        'Device n''a pas envoyé de signal depuis 48 heures',
        'HIGH',
        1,
        DATE_SUB(NOW(), INTERVAL 6 HOUR),
        NOW(),
        NOW()
    ),
    (
        4,
        2,
        'ANOMALY',
        'Nombre anormal d''utilisateurs detects en même temps',
        'MEDIUM',
        0,
        NULL,
        NOW(),
        NOW()
    );

-- ============================================================================
-- VERIFY DATA
-- ============================================================================
SELECT COUNT(*) as total_users FROM user;

SELECT COUNT(*) as total_employes FROM employe;

SELECT COUNT(*) as total_attendance FROM attendance;

SELECT COUNT(*) as total_conges FROM conge;

SELECT COUNT(*) as total_devices FROM device;

SELECT COUNT(*) as total_alerts FROM alert;

-- ============================================================================
-- COMMIT
-- ============================================================================
COMMIT;