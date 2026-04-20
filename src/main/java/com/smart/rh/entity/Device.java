package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an IoT device (access control reader, motion sensor, door lock, camera, etc.)
 * Tracks device status, credentials, and connectivity.
 */
@Entity
@Table(
    name = "device",
    indexes = {
        @Index(name = "idx_device_type", columnList = "device_type"),
        @Index(name = "idx_device_status", columnList = "status"),
        @Index(name = "idx_device_last_hb", columnList = "last_heartbeat")
    }
)
@Getter
@Setter
public class Device extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status = DeviceStatus.INACTIVE;

    @Column(length = 255)
    private String location;

    @NotBlank
    @Column(name = "mqtt_username", nullable = false, unique = true, length = 255)
    private String mqttUsername;

    @NotBlank
    @Column(name = "mqtt_password_hash", nullable = false, length = 255)
    private String mqttPasswordHash;

    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Additional device metadata stored as JSON
     */
    @Column(columnDefinition = "JSON")
    private String metadata;

    public enum DeviceStatus {
        INACTIVE,   // Device created but not yet activated
        ACTIVE,     // Device is active and operational
        OFFLINE,    // Device is not responding (heartbeat timeout)
        ERROR       // Device reported an error
    }
}
