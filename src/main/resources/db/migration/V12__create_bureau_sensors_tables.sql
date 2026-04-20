-- ============================================================================
-- V12 — Create Bureau Sensors Tables
-- Supports ESP32 sensors for temperature, CO2, and occupancy monitoring
-- ============================================================================

-- ============================================================================
-- Bureau Sensor Devices
-- ============================================================================
CREATE TABLE IF NOT EXISTS bureau_sensors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    sensor_types VARCHAR(255) NOT NULL,
    mqtt_username VARCHAR(100),
    mqtt_password VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_is_active (is_active)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Temperature readings (DHT11)
-- ============================================================================
CREATE TABLE IF NOT EXISTS temperature_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sensor_id BIGINT NOT NULL,
    temperature DECIMAL(5, 2) NOT NULL,
    humidity DECIMAL(5, 2),
    reading_timestamp DATETIME NOT NULL,
    is_valid BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES bureau_sensors (id) ON DELETE CASCADE,
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_reading_timestamp (reading_timestamp),
    INDEX idx_created_at (created_at),
    INDEX idx_sensor_timestamp (
        sensor_id,
        reading_timestamp DESC
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- CO2 readings (MQ-3/MQ-135)
-- ============================================================================
CREATE TABLE IF NOT EXISTS co2_readings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sensor_id BIGINT NOT NULL,
    co2_level INT NOT NULL,
    gas_concentration DECIMAL(5, 2),
    reading_timestamp DATETIME NOT NULL,
    is_valid BOOLEAN DEFAULT true,
    alarm_triggered BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES bureau_sensors (id) ON DELETE CASCADE,
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_reading_timestamp (reading_timestamp),
    INDEX idx_alarm_triggered (alarm_triggered),
    INDEX idx_created_at (created_at),
    INDEX idx_sensor_timestamp (
        sensor_id,
        reading_timestamp DESC
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Occupancy status (HC-SR501 Motion Sensor)
-- ============================================================================
CREATE TABLE IF NOT EXISTS occupancy_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sensor_id BIGINT NOT NULL,
    is_occupied BOOLEAN NOT NULL,
    motion_duration INT,
    confidence_level DECIMAL(5, 2),
    status_timestamp DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES bureau_sensors (id) ON DELETE CASCADE,
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_is_occupied (is_occupied),
    INDEX idx_status_timestamp (status_timestamp),
    INDEX idx_sensor_timestamp (
        sensor_id,
        status_timestamp DESC
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Sensor health/heartbeat
-- ============================================================================
CREATE TABLE IF NOT EXISTS sensor_health (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sensor_id BIGINT NOT NULL,
    uptime_seconds INT,
    battery_level INT,
    signal_strength INT,
    error_count INT DEFAULT 0,
    last_error_message VARCHAR(255),
    health_timestamp DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES bureau_sensors (id) ON DELETE CASCADE,
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_health_timestamp (health_timestamp),
    UNIQUE KEY unique_sensor_latest (sensor_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Sensor alerts/thresholds
-- ============================================================================
CREATE TABLE IF NOT EXISTS sensor_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sensor_id BIGINT NOT NULL,
    alert_type ENUM(
        'TEMPERATURE_HIGH',
        'TEMPERATURE_LOW',
        'CO2_HIGH',
        'OCCUPANCY_CHANGE',
        'OFFLINE'
    ) NOT NULL,
    threshold_value DECIMAL(10, 2),
    actual_value DECIMAL(10, 2),
    is_active BOOLEAN DEFAULT true,
    triggered_at TIMESTAMP NULL,
    acknowledged_at TIMESTAMP NULL,
    acknowledged_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES bureau_sensors (id) ON DELETE CASCADE,
    INDEX idx_alert_type (alert_type),
    INDEX idx_is_active (is_active),
    INDEX idx_sensor_id (sensor_id),
    INDEX idx_triggered_at (triggered_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;