package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Sensor alert for threshold violations.
 */
@Entity
@Table(name = "sensor_alerts", indexes = {
        @Index(name = "idx_alert_type", columnList = "alert_type"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_sensor_id", columnList = "sensor_id"),
        @Index(name = "idx_triggered_at", columnList = "triggered_at")
})
@Getter
@Setter
public class SensorAlert extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private BureauSensor sensor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType;

    @Column(name = "threshold_value", precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    @Column(name = "actual_value", precision = 10, scale = 2)
    private BigDecimal actualValue;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "triggered_at")
    private Instant triggeredAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public enum AlertType {
        TEMPERATURE_HIGH,
        TEMPERATURE_LOW,
        CO2_HIGH,
        OCCUPANCY_CHANGE,
        OFFLINE
    }
}
