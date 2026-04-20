package com.smart.rh.dto.sensors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MQTT event payload for occupancy sensor readings
 * Topic: smartrh/devices/{deviceId}/occupancy/status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyEventDto {
    private Long sensorId;
    private Boolean isOccupied;
    private Integer motionDuration;
    private BigDecimal confidenceLevel;
    private Instant timestamp;
}
