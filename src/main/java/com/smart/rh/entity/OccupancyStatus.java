package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Occupancy status from HC-SR501 motion sensor.
 */
@Entity
@Table(name = "occupancy_status", indexes = {
        @Index(name = "idx_sensor_id", columnList = "sensor_id"),
        @Index(name = "idx_is_occupied", columnList = "is_occupied"),
        @Index(name = "idx_status_timestamp", columnList = "status_timestamp"),
        @Index(name = "idx_sensor_timestamp", columnList = "sensor_id, status_timestamp DESC")
})
@Getter
@Setter
public class OccupancyStatus extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private BureauSensor sensor;

    @NotNull
    @Column(name = "is_occupied", nullable = false)
    private Boolean isOccupied;

    @Column(name = "motion_duration")
    private Integer motionDuration;

    @Column(name = "confidence_level", precision = 5, scale = 2)
    private BigDecimal confidenceLevel;

    @NotNull
    @Column(name = "status_timestamp", nullable = false)
    private Instant statusTimestamp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
