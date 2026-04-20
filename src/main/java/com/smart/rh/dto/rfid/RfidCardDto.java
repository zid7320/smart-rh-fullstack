package com.smart.rh.dto.rfid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for RFID card mapping
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfidCardDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("cardId")
    private String cardId;

    @JsonProperty("employeeId")
    private Long employeeId;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("registeredAt")
    private String registeredAt;
}
