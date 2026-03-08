package com.smart.rh.dto.attendance;

import java.time.Instant;

/**
 * Response DTO for attendance events.
 * Also used as the WebSocket broadcast payload on /topic/attendance.
 */
public record AttendanceDto(
        Long    id,
        Long    employeId,
        String  employeNomComplet,
        String  type,
        Instant clockedAt,
        Double  confidence,
        String  cameraId,
        String  siteId,
        Instant createdAt
) {}
