package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Temperature and humidity reading from DHT11 sensor.
 */
@Entity
@Table(name = "temperature_readings", indexes = {
        @Index(name = "idx_sensor_id", columnList = "sensor_id"),
        @Index(name = "idx_reading_timestamp", columnList = "reading_timestamp"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_sensor_timestamp", columnList = "sensor_id, reading_timestamp DESC")
})
@Getter
@Setter
public class TemperatureReading extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private BureauSensor sensor;

    @NotNull
    @Column(name = "temperature", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "humidity", precision = 5, scale = 2)
    private BigDecimal humidity;

    @NotNull
    @Column(name = "reading_timestamp", nullable = false)
    private Instant readingTimestamp;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
