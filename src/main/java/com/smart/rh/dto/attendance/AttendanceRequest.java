package com.smart.rh.dto.attendance;

import com.smart.rh.entity.AttendanceType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Request body for POST /api/attendance/recognition.
 *
 * <p>{@code clockedAt} is optional; the service defaults it to {@code Instant.now()}
 * when absent, allowing IoT devices that do not carry an RTC to omit it.</p>
 *
 * <p>{@code rawImagePath} is intentionally absent — that is an internal server-side
 * path set by the Python AI module, never sent by the REST client.</p>
 */
public record AttendanceRequest(

        @NotNull(message = "employeId is required")
        Long employeId,

        @NotNull(message = "type is required (IN or OUT)")
        AttendanceType type,

        /** UTC timestamp of the clocking event. Defaults to server time if null. */
        Instant clockedAt,

        /** AI recognition confidence (0.0 – 1.0). Optional. */
        Double confidence,

        String cameraId,
        String siteId
) {}
