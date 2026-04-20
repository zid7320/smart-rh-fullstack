package com.smart.rh.dto.iot;

import java.math.BigDecimal;
import java.time.Instant;

public record DeviceHealthDto(
        Long id,
        Long deviceId,
        Long messagesReceived,
        Long messagesFailed,
        Long messagesProcessed,
        BigDecimal uptimePercent,
        BigDecimal errorRate,
        Instant lastChecked,
        String lastError,
        Integer consecutiveFailures
) {}
