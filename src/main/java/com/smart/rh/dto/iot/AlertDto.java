package com.smart.rh.dto.iot;

import java.time.Instant;

public record AlertDto(
        Long id,
        Long deviceId,
        String deviceName,
        String alertType,
        String severity,
        String message,
        Boolean acknowledged,
        String acknowledgedBy,
        Instant createdAt
) {}
