package com.smart.rh.dto.rfid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for attendance record response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("employeeId")
    private Long employeeId;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("eventTimestamp")
    private String eventTimestamp;

    @JsonProperty("location")
    private String location;

    @JsonProperty("isVerified")
    private Boolean isVerified;

    @JsonProperty("createdAt")
    private String createdAt;
}
