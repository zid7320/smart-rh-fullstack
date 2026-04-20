package com.smart.rh.dto.sensors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bureau sensor device
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BureauSensorDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("location")
    private String location;

    @JsonProperty("sensorTypes")
    private String sensorTypes;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("lastUpdate")
    private String lastUpdate;
}
