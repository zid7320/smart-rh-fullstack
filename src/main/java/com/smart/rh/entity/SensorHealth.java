package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Sensor health and heartbeat information.
 */
@Entity
@Table(name = "sensor_health", indexes = {
        @Index(name = "idx_sensor_id", columnList = "sensor_id"),
        @Index(name = "idx_health_timestamp", columnList = "health_timestamp")
})
@Getter
@Setter
public class SensorHealth extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private BureauSensor sensor;

    @Column(name = "uptime_seconds")
    private Integer uptimeSeconds;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "signal_strength")
    private Integer signalStrength;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(name = "last_error_message", length = 255)
    private String lastErrorMessage;

    @NotNull
    @Column(name = "health_timestamp", nullable = false)
    private Instant healthTimestamp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
