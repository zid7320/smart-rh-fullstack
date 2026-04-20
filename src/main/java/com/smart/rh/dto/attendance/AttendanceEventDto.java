package com.smart.rh.dto.attendance;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for attendance event (facial recognition event)
 * Returned by API endpoints for dashboard and reporting
 */
public record AttendanceEventDto(
                Long id,
                Long employeeId,
                String employeeName,
                Long deviceId,
                String deviceName,
                String eventType, // IN, OUT, SUSPICIOUS
                Instant eventTimestamp,
                BigDecimal confidenceScore, // 0-100%
                Boolean isFraudSuspected,
                String fraudReason,
                String photoData, // Base64 or path
                String processingStatus, // PENDING, PROCESSED, ERROR
                Long processingTimeMs,
                Boolean manuallyVerified,
                Instant verifiedAt,
                String verificationNotes,
                Instant createdAt) {
}
