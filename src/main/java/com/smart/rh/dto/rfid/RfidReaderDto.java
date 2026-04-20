package com.smart.rh.dto.rfid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for RFID reader device configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfidReaderDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("location")
    private String location;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("mqttUsername")
    private String mqttUsername;

    @JsonProperty("mqttPassword")
    private String mqttPassword;
}
