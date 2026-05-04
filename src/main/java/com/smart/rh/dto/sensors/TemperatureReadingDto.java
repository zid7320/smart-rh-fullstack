package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for temperature reading from DHT11 sensor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemperatureReadingDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("sensorId")
    // Can be numeric sensor ID or device ID string (e.g., "1" or "sensor-temp-01")
    private String sensorId;

    @JsonProperty("temperature")
    private BigDecimal temperature;

    @JsonProperty("humidity")
    private BigDecimal humidity;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("isValid")
    private Boolean isValid;
}
