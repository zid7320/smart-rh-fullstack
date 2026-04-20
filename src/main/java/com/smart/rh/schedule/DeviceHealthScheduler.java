package com.smart.rh.schedule;

import com.smart.rh.service.DeviceHealthService;
import com.smart.rh.service.SensorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Autonomous health monitoring and maintenance tasks for IoT devices.
 * Runs periodically to detect offline devices, manage alerts, and clean up old
 * data.
 * <p>
 * Enabled only when {@code app.mqtt.enabled=true}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mqtt.enabled", havingValue = "true")
public class DeviceHealthScheduler {

    private final DeviceHealthService deviceHealthService;
    private final SensorDataService sensorDataService;

    /**
     * Check all device health statuses every 5 minutes.
     * Detects offline devices (no heartbeat >5 min) and triggers offline alerts.
     * Updates error rates and uptime percentages.
     * <p>
     * Runs: Every 300,000 ms (5 minutes)
     * </p>
     */
    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    public void checkDeviceHealth() {
        log.info("Starting device health check cycle");
        try {
            deviceHealthService.checkAllDeviceHealth();
            log.info("Device health check completed successfully");
        } catch (Exception e) {
            log.error("Error during device health check", e);
        }
    }

    /**
     * Clean up old sensor readings daily.
     * Removes sensor readings older than 1 year to manage database size.
     * Runs: Once daily at 2:00 AM UTC (configurable via cron expression).
     * <p>
     * Data retention policy: Keep 1 year of historical data (365 days).
     * This supports trend analysis and compliance requirements while
     * managing storage costs for high-volume IoT devices.
     * </p>
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void cleanupOldReadings() {
        log.info("Starting daily sensor reading cleanup");
        try {
            Instant cutoffDate = Instant.now().minus(365, ChronoUnit.DAYS);
            sensorDataService.cleanupOldReadings(cutoffDate);
            log.info("Cleanup completed: removed readings older than {}", cutoffDate);
        } catch (Exception e) {
            log.error("Error during sensor reading cleanup", e);
        }
    }
}
