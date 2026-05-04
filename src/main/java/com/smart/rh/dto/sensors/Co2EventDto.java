package com.smart.rh.dto.sensors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MQTT event payload for CO2 sensor readings
 * Topic: smartrh/devices/{deviceId}/environment/co2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Co2EventDto {
    // Can be numeric sensor ID or device ID string (e.g., "1" or "sensor-co2-01")
    private String sensorId;
    private Integer co2Level;
    private BigDecimal gasConcentration;
    private Instant timestamp;
}
