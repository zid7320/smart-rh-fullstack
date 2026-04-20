package com.smart.rh.dto.iot;

import com.smart.rh.entity.DeviceType;
import java.time.Instant;

public record DeviceDto(
        Long id,
        String name,
        DeviceType deviceType,
        String status,
        String location,
        String description,
        String mqttUsername,
        Instant lastHeartbeat,
        Instant createdAt,
        Instant updatedAt
) {}
