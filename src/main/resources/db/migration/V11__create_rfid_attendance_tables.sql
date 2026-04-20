-- ============================================================================
-- V11 — Create RFID Attendance System Tables
-- Supports ESP32 RFID readers for employee check-in/check-out
-- ============================================================================

-- ============================================================================
-- RFID Readers (devices)
-- ============================================================================
CREATE TABLE IF NOT EXISTS rfid_readers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    mqtt_username VARCHAR(100),
    mqtt_password VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    INDEX idx_device_id (device_id),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- RFID Cards linked to employees
-- ============================================================================
CREATE TABLE IF NOT EXISTS rfid_cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    card_id VARCHAR(100) NOT NULL UNIQUE,
    employee_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT true,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    FOREIGN KEY (employee_id) REFERENCES employe (id) ON DELETE CASCADE,
    INDEX idx_employee_id (employee_id),
    INDEX idx_card_id (card_id),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Attendance Records (pointage events)
-- ============================================================================
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    rfid_reader_id BIGINT,
    rfid_card_id VARCHAR(100),
    event_type ENUM('IN', 'OUT') NOT NULL,
    event_timestamp DATETIME NOT NULL,
    location VARCHAR(100),
    is_verified BOOLEAN DEFAULT false,
    verification_code VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employe (id) ON DELETE CASCADE,
    FOREIGN KEY (rfid_reader_id) REFERENCES rfid_readers (id) ON DELETE SET NULL,
    INDEX idx_employee_id (employee_id),
    INDEX idx_event_timestamp (event_timestamp),
    INDEX idx_reader_id (rfid_reader_id),
    INDEX idx_verification_code (verification_code),
    INDEX idx_employee_timestamp (employee_id, event_timestamp)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Attendance Verification logs (audit trail)
-- ============================================================================
CREATE TABLE IF NOT EXISTS attendance_verifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attendance_record_id BIGINT NOT NULL,
    verified_by VARCHAR(100) NOT NULL,
    verification_status ENUM(
        'PENDING',
        'APPROVED',
        'REJECTED'
    ) DEFAULT 'PENDING',
    rejection_reason VARCHAR(255),
    verified_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attendance_record_id) REFERENCES attendance_records (id) ON DELETE CASCADE,
    INDEX idx_status (verification_status),
    INDEX idx_verified_at (verified_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;