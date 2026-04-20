package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Tracks health metrics for each device (heartbeat, message counts, error rates).
 * One-to-one relationship with Device.
 */
@Entity
@Table(
    name = "device_health",
    indexes = {
        @Index(name = "idx_device_health", columnList = "device_id")
    }
)
@Getter
@Setter
public class DeviceHealth extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    @Column(name = "messages_received", nullable = false)
    private Long messagesReceived = 0L;

    @Column(name = "messages_failed", nullable = false)
    private Long messagesFailed = 0L;

    @Column(name = "messages_processed", nullable = false)
    private Long messagesProcessed = 0L;

    /**
     * Device uptime percentage (0-100)
     */
    @Column(name = "uptime_percent", precision = 5, scale = 2)
    private BigDecimal uptimePercent = BigDecimal.valueOf(100);

    /**
     * Percentage of failed messages (0-100)
     */
    @Column(name = "error_rate", precision = 5, scale = 2)
    private BigDecimal errorRate = BigDecimal.ZERO;

    /**
     * Last time health metrics were checked
     */
    @Column(name = "last_checked")
    private Instant lastChecked;

    /**
     * Last recorded error message
     */
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    /**
     * Consecutive heartbeat failures (reset to 0 on successful heartbeat)
     */
    @Column(name = "consecutive_failures", nullable = false)
    private Integer consecutiveFailures = 0;

    public void recordSuccess() {
        this.messagesReceived++;
        this.messagesProcessed++;
        this.consecutiveFailures = 0;
    }

    public void recordFailure(String errorMessage) {
        this.messagesReceived++;
        this.messagesFailed++;
        this.consecutiveFailures++;
        this.lastError = errorMessage;
        updateErrorRate();
    }

    public void updateErrorRate() {
        if (messagesReceived > 0) {
            BigDecimal failureRatio = BigDecimal.valueOf(messagesFailed)
                    .divide(BigDecimal.valueOf(messagesReceived), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            this.errorRate = failureRatio.setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}
