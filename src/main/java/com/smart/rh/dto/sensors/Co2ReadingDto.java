package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for CO2 reading from MQ-3/MQ-135 sensor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Co2ReadingDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("sensorId")
    private String sensorId;

    @JsonProperty("co2Level")
    private Integer co2Level;

    @JsonProperty("gasConcentration")
    private BigDecimal gasConcentration;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("isValid")
    private Boolean isValid;

    @JsonProperty("alarmTriggered")
    private Boolean alarmTriggered;

    @JsonProperty("status")
    private String status;
}
