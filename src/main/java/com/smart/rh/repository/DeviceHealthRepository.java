package com.smart.rh.repository;

import com.smart.rh.entity.DeviceHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DeviceHealthRepository extends JpaRepository<DeviceHealth, Long> {

    /**
     * Find health metrics by device ID
     */
    Optional<DeviceHealth> findByDeviceId(Long deviceId);

    /**
     * Find all devices with high error rates
     */
    List<DeviceHealth> findByErrorRateGreaterThanEqual(BigDecimal errorRate);

    /**
     * Find devices with consecutive failures
     */
    @Query("SELECT dh FROM DeviceHealth dh WHERE dh.consecutiveFailures >= ?1")
    List<DeviceHealth> findDevicesWithConsecutiveFailures(Integer failureThreshold);

    /**
     * Find devices with low uptime
     */
    @Query("SELECT dh FROM DeviceHealth dh WHERE dh.uptimePercent <= ?1")
    List<DeviceHealth> findDevicesWithLowUptime(BigDecimal uptimeThreshold);

    /**
     * Calculate average error rate across all devices
     */
    @Query("SELECT AVG(dh.errorRate) FROM DeviceHealth dh")
    BigDecimal getAverageErrorRate();

    /**
     * Calculate average uptime across all devices
     */
    @Query("SELECT AVG(dh.uptimePercent) FROM DeviceHealth dh")
    BigDecimal getAverageUptime();
}
