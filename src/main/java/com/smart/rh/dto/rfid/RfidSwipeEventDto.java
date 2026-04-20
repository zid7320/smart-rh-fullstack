package com.smart.rh.dto.rfid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for RFID swipe event from ESP32 device
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RfidSwipeEventDto {

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("cardId")
    private String cardId;

    @JsonProperty("rfidReaderId")
    private Long rfidReaderId;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("location")
    private String location;
}
