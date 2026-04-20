package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sensor health information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorHealthDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("sensorId")
    private Long sensorId;

    @JsonProperty("uptimeSeconds")
    private Integer uptimeSeconds;

    @JsonProperty("batteryLevel")
    private Integer batteryLevel;

    @JsonProperty("signalStrength")
    private Integer signalStrength;

    @JsonProperty("errorCount")
    private Integer errorCount;

    @JsonProperty("lastErrorMessage")
    private String lastErrorMessage;

    @JsonProperty("healthTimestamp")
    private String healthTimestamp;

    @JsonProperty("isOnline")
    private Boolean isOnline;
}
