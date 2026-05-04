package com.smart.rh.dto.sensors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MQTT event payload for temperature sensor readings
 * Topic: smartrh/devices/{deviceId}/environment/temperature
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureEventDto {
    // Can be numeric sensor ID or device ID string (e.g., "1" or "sensor-temp-01")
    private String sensorId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private Instant timestamp;
}
