package com.smart.rh.dto.rfid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for RFID swipe event from ESP32 device
 * 
 * Supports field name aliases for flexibility:
 * - cardId (or cardUid)
 * - deviceId (or sensorId)
 * - rfidReaderId (optional, numeric reader ID from database)
 * - location (optional)
 * - timestamp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RfidSwipeEventDto {

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("cardId")
    @JsonAlias("cardUid")
    private String cardId;

    @JsonProperty("rfidReaderId")
    private Long rfidReaderId;

    @JsonProperty("deviceId")
    @JsonAlias({"readerId", "sensorId"})
    private String deviceId;

    @JsonProperty("location")
    private String location;
}
