package com.smart.rh.dto.rfid;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for attendance verification response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceVerificationDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("attendanceRecordId")
    private Long attendanceRecordId;

    @JsonProperty("verifiedBy")
    private String verifiedBy;

    @JsonProperty("verificationStatus")
    private String verificationStatus;

    @JsonProperty("rejectionReason")
    private String rejectionReason;

    @JsonProperty("verifiedAt")
    private String verifiedAt;

    @JsonProperty("createdAt")
    private String createdAt;
}
