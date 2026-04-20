package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for sensor alert
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorAlertDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("sensorId")
    private Long sensorId;

    @JsonProperty("alertType")
    private String alertType;

    @JsonProperty("thresholdValue")
    private BigDecimal thresholdValue;

    @JsonProperty("actualValue")
    private BigDecimal actualValue;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("triggeredAt")
    private String triggeredAt;

    @JsonProperty("acknowledgedAt")
    private String acknowledgedAt;

    @JsonProperty("acknowledgedBy")
    private String acknowledgedBy;

    @JsonProperty("createdAt")
    private String createdAt;
}
