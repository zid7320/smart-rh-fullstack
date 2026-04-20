package com.smart.rh.dto.iot;

import java.time.Instant;

public record SensorReadingDto(
        Long id,
        Long deviceId,
        String deviceName,
        String readingType,
        String value,
        Instant timestamp,
        String metadata
) {}
