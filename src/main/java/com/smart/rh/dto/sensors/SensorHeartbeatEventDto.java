package com.smart.rh.dto.sensors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * MQTT event payload for sensor heartbeat (health checks)
 * Topic: smartrh/devices/{deviceId}/health/heartbeat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorHeartbeatEventDto {
    private Long sensorId;
    private Integer batteryLevel;
    private Integer signalStrength;
    private Instant timestamp;
}
