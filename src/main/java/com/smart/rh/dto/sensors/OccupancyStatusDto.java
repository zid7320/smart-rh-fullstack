package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for occupancy status from HC-SR501 motion sensor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyStatusDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("sensorId")
    private Long sensorId;

    @JsonProperty("isOccupied")
    private Boolean isOccupied;

    @JsonProperty("motionDuration")
    private Integer motionDuration;

    @JsonProperty("confidenceLevel")
    private BigDecimal confidenceLevel;

    @JsonProperty("timestamp")
    private String timestamp;
}
