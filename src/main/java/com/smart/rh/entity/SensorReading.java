package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Stores sensor readings from IoT devices (e.g., access swipes, motion events, lock status).
 * Supports time-series data for analytics and historical queries.
 */
@Entity
@Table(
    name = "sensor_reading",
    indexes = {
        @Index(name = "idx_device_ts", columnList = "device_id, timestamp DESC"),
        @Index(name = "idx_reading_type", columnList = "reading_type"),
        @Index(name = "idx_timestamp", columnList = "timestamp DESC")
    }
)
@Getter
@Setter
public class SensorReading extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @NotBlank
    @Column(name = "reading_type", nullable = false, length = 50)
    private String readingType;

    /**
     * The sensor reading value (can be numeric, string, or JSON)
     */
    @Column(name = "value", columnDefinition = "VARCHAR(1000)")
    private String value;

    @NotNull
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * Additional metadata stored as JSON (e.g., location, confidence, additional_data)
     */
    @Column(columnDefinition = "JSON")
    private String metadata;

    /**
     * Processing status: PENDING, PROCESSED, ERROR
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProcessingStatus processingStatus = ProcessingStatus.PROCESSED;

    public enum ProcessingStatus {
        PENDING,    // Not yet processed
        PROCESSED,  // Successfully processed
        ERROR       // Processing failed
    }
}
