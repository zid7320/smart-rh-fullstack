package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * CO2 level reading from MQ-3/MQ-135 sensor.
 */
@Entity
@Table(name = "co2_readings", indexes = {
        @Index(name = "idx_sensor_id", columnList = "sensor_id"),
        @Index(name = "idx_reading_timestamp", columnList = "reading_timestamp"),
        @Index(name = "idx_alarm_triggered", columnList = "alarm_triggered"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_sensor_timestamp", columnList = "sensor_id, reading_timestamp DESC")
})
@Getter
@Setter
public class Co2Reading extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private BureauSensor sensor;

    @NotNull
    @Column(name = "co2_level", nullable = false)
    private Integer co2Level;

    @Column(name = "gas_concentration", precision = 5, scale = 2)
    private BigDecimal gasConcentration;

    @NotNull
    @Column(name = "reading_timestamp", nullable = false)
    private Instant readingTimestamp;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid = true;

    @Column(name = "alarm_triggered", nullable = false)
    private Boolean alarmTriggered = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
