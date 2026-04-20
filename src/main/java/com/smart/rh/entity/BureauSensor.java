package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a bureau sensor device.
 * Stores device configuration and sensor types.
 */
@Entity
@Table(name = "bureau_sensors", indexes = {
        @Index(name = "idx_device_id", columnList = "device_id"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Getter
@Setter
public class BureauSensor extends BaseEntity {

    @NotBlank
    @Column(name = "device_id", nullable = false, length = 50, unique = true)
    private String deviceId;

    @NotBlank
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank
    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @NotBlank
    @Column(name = "sensor_types", nullable = false, length = 255)
    private String sensorTypes;

    @Column(name = "mqtt_username", length = 100)
    private String mqttUsername;

    @Column(name = "mqtt_password", length = 255)
    private String mqttPassword;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
