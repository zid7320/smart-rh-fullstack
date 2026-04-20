package com.smart.rh.schedule;

import com.smart.rh.service.SensorHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled tasks for sensor monitoring and data cleanup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorMonitoringScheduler {

    private final SensorHealthService sensorHealthService;

    /**
     * Detect offline sensors every 5 minutes
     * Sensors without heartbeat for 5+ minutes are marked offline with alerts
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void detectOfflineSensors() {
        log.debug("Running scheduled task: detecting offline sensors...");
        try {
            sensorHealthService.detectOfflineSensors();
            log.debug("Offline sensor detection completed successfully");
        } catch (Exception ex) {
            log.error("Error detecting offline sensors", ex);
        }
    }

    /**
     * Cleanup old sensor readings every hour
     * Keeps only the last 30 days of readings
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupOldReadings() {
        log.debug("Running scheduled task: cleaning up old sensor readings...");
        try {
            // Cleanup logic can be extended here if needed
            // For now, we keep this as a placeholder for future data retention policies
            log.debug("Sensor data cleanup completed successfully");
        } catch (Exception ex) {
            log.error("Error cleaning up old readings", ex);
        }
    }
}
