package com.smart.rh.repository;

import com.smart.rh.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Find all alerts for a device with pagination
     */
    Page<Alert> findByDeviceIdOrderByCreatedAtDesc(Long deviceId, Pageable pageable);

    /**
     * Find unacknowledged alerts
     */
    List<Alert> findByAcknowledgedFalseOrderByCreatedAtDesc();

    /**
     * Find unacknowledged alerts for a specific device
     */
    List<Alert> findByDeviceIdAndAcknowledgedFalse(Long deviceId);

    /**
     * Find alerts by severity
     */
    List<Alert> findByDeviceIdAndSeverityOrderByCreatedAtDesc(Long deviceId, Alert.AlertSeverity severity);

    /**
     * Find critical alerts
     */
    List<Alert> findBySeverityOrderByCreatedAtDesc(Alert.AlertSeverity severity);

    /**
     * Find alerts within a time range
     */
    @Query("SELECT a FROM Alert a WHERE a.device.id = ?1 AND a.createdAt BETWEEN ?2 AND ?3 ORDER BY a.createdAt DESC")
    List<Alert> findByDeviceAndTimeRange(Long deviceId, Instant startTime, Instant endTime);

    /**
     * Count unacknowledged alerts
     */
    long countByAcknowledgedFalse();

    /**
     * Count unacknowledged alerts for a device
     */
    long countByDeviceIdAndAcknowledgedFalse(Long deviceId);

    /**
     * Find recent alerts for dashboard (last 24h)
     */
    @Query("SELECT a FROM Alert a WHERE a.createdAt >= ?1 ORDER BY a.createdAt DESC")
    Page<Alert> findRecentAlerts(Instant since, Pageable pageable);
}
