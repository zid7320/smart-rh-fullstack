package com.smart.rh.service;

import com.smart.rh.entity.Device;
import com.smart.rh.entity.DeviceHealth;
import com.smart.rh.repository.DeviceHealthRepository;
import com.smart.rh.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Service for monitoring and analyzing device health metrics.
 * Tracks uptime, error rates, and device performance.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceHealthService {

    private final DeviceHealthRepository deviceHealthRepository;
    private final DeviceRepository deviceRepository;
    private final AlertingService alertingService;

    /**
     * Get health metrics for a device
     */
    @Transactional(readOnly = true)
    public DeviceHealth getDeviceHealth(Long deviceId) {
        return deviceHealthRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device health not found: " + deviceId));
    }

    /**
     * Record a successful message from a device
     */
    @Transactional
    public void recordMessageSuccess(Long deviceId) {
        DeviceHealth health = deviceHealthRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (health != null) {
            health.recordSuccess();
            health.setLastChecked(Instant.now());
            deviceHealthRepository.save(health);
        }
    }

    /**
     * Record a failed message from a device
     */
    @Transactional
    public void recordMessageFailure(Long deviceId, String errorMessage) {
        DeviceHealth health = deviceHealthRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (health != null) {
            health.recordFailure(errorMessage);
            health.setLastChecked(Instant.now());
            deviceHealthRepository.save(health);
        }
    }

    /**
     * Get devices with high error rates (above threshold)
     */
    @Transactional(readOnly = true)
    public List<DeviceHealth> getDevicesWithHighErrorRate(BigDecimal threshold) {
        return deviceHealthRepository.findByErrorRateGreaterThanEqual(threshold);
    }

    /**
     * Get devices with consecutive failures (potential offline)
     */
    @Transactional(readOnly = true)
    public List<DeviceHealth> getDevicesWithConsecutiveFailures(Integer failureThreshold) {
        return deviceHealthRepository.findDevicesWithConsecutiveFailures(failureThreshold);
    }

    /**
     * Get devices with low uptime
     */
    @Transactional(readOnly = true)
    public List<DeviceHealth> getDevicesWithLowUptime(BigDecimal uptimeThreshold) {
        return deviceHealthRepository.findDevicesWithLowUptime(uptimeThreshold);
    }

    /**
     * Calculate average error rate across all devices
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageErrorRate() {
        BigDecimal avg = deviceHealthRepository.getAverageErrorRate();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    /**
     * Calculate average uptime across all devices
     */
    @Transactional(readOnly = true)
    public BigDecimal getAverageUptime() {
        BigDecimal avg = deviceHealthRepository.getAverageUptime();
        return avg != null ? avg : BigDecimal.valueOf(100);
    }

    /**
     * Update device uptime percentage based on heartbeat history
     * Called by a scheduled task
     */
    @Transactional
    public void calculateDeviceUptime(Long deviceId, Instant startTime, Instant endTime) {
        DeviceHealth health = deviceHealthRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (health == null) {
            return;
        }

        // Simple calculation: uptime = (total_seconds - offline_seconds) / total_seconds * 100
        // In a production system, you'd track offline periods more precisely
        long totalSeconds = endTime.getEpochSecond() - startTime.getEpochSecond();
        long offlineSeconds = 0; // Would be calculated from offline periods

        if (totalSeconds > 0) {
            BigDecimal uptime = BigDecimal.valueOf((totalSeconds - offlineSeconds) * 100.0)
                    .divide(BigDecimal.valueOf(totalSeconds), 2, java.math.RoundingMode.HALF_UP);
            health.setUptimePercent(uptime);
            deviceHealthRepository.save(health);

            log.debug("Updated uptime for device {}: {}%", deviceId, uptime);
        }
    }

    /**
     * Reset health metrics for a device (useful after maintenance)
     */
    @Transactional
    public void resetHealthMetrics(Long deviceId) {
        DeviceHealth health = deviceHealthRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (health != null) {
            health.setMessagesReceived(0L);
            health.setMessagesFailed(0L);
            health.setMessagesProcessed(0L);
            health.setErrorRate(BigDecimal.ZERO);
            health.setConsecutiveFailures(0);
            health.setLastError(null);
            health.setLastChecked(Instant.now());
            deviceHealthRepository.save(health);

            log.info("Reset health metrics for device: {}", deviceId);
        }
    }

    /**
     * Check all devices and detect offline ones
     * Called by a scheduled task every 5 minutes
     */
    @Transactional
    public void checkAllDeviceHealth() {
        List<Device> devices = deviceRepository.findAll();

        for (Device device : devices) {
            if (device.getLastHeartbeat() == null) {
                continue;
            }

            Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
            if (device.getLastHeartbeat().isBefore(fiveMinutesAgo) &&
                    device.getStatus() != Device.DeviceStatus.OFFLINE) {
                // Device is offline
                device.setStatus(Device.DeviceStatus.OFFLINE);
                deviceRepository.save(device);

                // Create offline alert
                alertingService.createOfflineAlert(device);

                log.warn("Device detected as offline: {} ({})", device.getId(), device.getName());
            } else if (device.getLastHeartbeat().isAfter(fiveMinutesAgo) &&
                    device.getStatus() == Device.DeviceStatus.OFFLINE) {
                // Device came back online
                device.setStatus(Device.DeviceStatus.ACTIVE);
                deviceRepository.save(device);

                // Resolve offline alerts
                alertingService.resolveOfflineAlerts(device);

                log.info("Device came back online: {} ({})", device.getId(), device.getName());
            }
        }
    }

    /**
     * Get health summary for dashboard
     */
    @Transactional(readOnly = true)
    public HealthSummary getHealthSummary() {
        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countByStatus(Device.DeviceStatus.ACTIVE);
        long offlineDevices = deviceRepository.countByStatus(Device.DeviceStatus.OFFLINE);
        long errorDevices = deviceRepository.countByStatus(Device.DeviceStatus.ERROR);

        BigDecimal avgErrorRate = getAverageErrorRate();
        BigDecimal avgUptime = getAverageUptime();

        return new HealthSummary(
                totalDevices,
                activeDevices,
                offlineDevices,
                errorDevices,
                avgErrorRate,
                avgUptime
        );
    }

    /**
     * DTO for health summary
     */
    public record HealthSummary(
            long totalDevices,
            long activeDevices,
            long offlineDevices,
            long errorDevices,
            BigDecimal averageErrorRate,
            BigDecimal averageUptime
    ) {
    }
}
