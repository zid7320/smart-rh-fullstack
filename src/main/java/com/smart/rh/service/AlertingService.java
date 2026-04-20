package com.smart.rh.service;

import com.smart.rh.entity.Alert;
import com.smart.rh.entity.Device;
import com.smart.rh.entity.DeviceHealth;
import com.smart.rh.repository.AlertRepository;
import com.smart.rh.repository.DeviceHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service for managing alerts and alert rules.
 * Evaluates conditions and generates alerts based on device health and readings.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertingService {

    private static final String ALERT_TOPIC = "/topic/alerts";
    private static final BigDecimal ERROR_RATE_THRESHOLD = BigDecimal.valueOf(5); // 5%

    private final AlertRepository alertRepository;
    private final DeviceHealthRepository deviceHealthRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditService auditService;

    /**
     * Create a new alert
     */
    @Transactional
    public Alert createAlert(Device device, String alertType, String message, Alert.AlertSeverity severity) {
        Alert alert = new Alert();
        alert.setDevice(device);
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setAcknowledged(false);

        Alert saved = alertRepository.save(alert);

        // Broadcast to WebSocket subscribers
        broadcastAlert(saved);

        // Audit logging
        auditService.log("ALERT_CREATED", "Alert", saved.getId(),
                String.format("type=%s severity=%s device=%s", alertType, severity, device.getId()));

        log.warn("Alert created: id={} type={} severity={} device={}",
                saved.getId(), alertType, severity, device.getId());

        return saved;
    }

    /**
     * Acknowledge an alert (mark as read)
     */
    @Transactional
    public Alert acknowledgeAlert(Long alertId, String acknowledgedBy) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(acknowledgedBy);

        Alert updated = alertRepository.save(alert);

        auditService.log("ALERT_ACKNOWLEDGED", "Alert", alertId,
                String.format("by=%s", acknowledgedBy));

        return updated;
    }

    /**
     * Get all alerts for a device
     */
    @Transactional(readOnly = true)
    public Page<Alert> getAlertsByDevice(Long deviceId, Pageable pageable) {
        return alertRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId, pageable);
    }

    /**
     * Get unacknowledged alerts
     */
    @Transactional(readOnly = true)
    public List<Alert> getUnacknowledgedAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc();
    }

    /**
     * Get unacknowledged alerts for a device
     */
    @Transactional(readOnly = true)
    public List<Alert> getUnacknowledgedAlertsByDevice(Long deviceId) {
        return alertRepository.findByDeviceIdAndAcknowledgedFalse(deviceId);
    }

    /**
     * Get alerts by severity
     */
    @Transactional(readOnly = true)
    public List<Alert> getAlertsBySeverity(Alert.AlertSeverity severity) {
        return alertRepository.findBySeverityOrderByCreatedAtDesc(severity);
    }

    /**
     * Count unacknowledged alerts
     */
    @Transactional(readOnly = true)
    public long getUnacknowledgedAlertCount() {
        return alertRepository.countByAcknowledgedFalse();
    }

    /**
     * Get recent alerts (last 24 hours) for dashboard
     */
    @Transactional(readOnly = true)
    public Page<Alert> getRecentAlerts(Pageable pageable) {
        return alertRepository.findRecentAlerts(java.time.Instant.now().minusSeconds(86400), pageable);
    }

    /**
     * Evaluate alert rules based on sensor readings
     * Called after each sensor reading is ingested
     */
    @Transactional
    public void evaluateAlerts(Device device, String readingType, String value) {
        try {
            // Evaluate based on reading type
            switch (readingType.toLowerCase()) {
                case "motion":
                    evaluateMotionAlert(device, value);
                    break;
                case "swipe":
                case "access":
                    evaluateAccessAlert(device, value);
                    break;
                case "lock_status":
                    evaluateLockAlert(device, value);
                    break;
                case "error":
                    evaluateErrorAlert(device, value);
                    break;
                default:
                    // Unknown reading type - no specific alert logic
                    break;
            }

            // Check health-based alerts
            evaluateHealthAlerts(device);

        } catch (Exception e) {
            log.error("Error evaluating alerts for device: {}", device.getId(), e);
        }
    }

    /**
     * Evaluate motion sensor alerts (e.g., motion after hours)
     */
    private void evaluateMotionAlert(Device device, String value) {
        if ("detected".equalsIgnoreCase(value)) {
            // Could check time of day here for after-hours alerts
            // For now, just log
            log.debug("Motion detected on device: {}", device.getId());
        }
    }

    /**
     * Evaluate access control alerts (e.g., unauthorized swipe)
     */
    private void evaluateAccessAlert(Device device, String value) {
        if ("unauthorized".equalsIgnoreCase(value)) {
            createAlert(device,
                    "UNAUTHORIZED_ACCESS",
                    "Unauthorized access attempt detected",
                    Alert.AlertSeverity.WARNING);
        }
    }

    /**
     * Evaluate door lock alerts (e.g., failed unlock)
     */
    private void evaluateLockAlert(Device device, String value) {
        if ("failed".equalsIgnoreCase(value)) {
            createAlert(device,
                    "LOCK_FAILURE",
                    "Door lock operation failed",
                    Alert.AlertSeverity.WARNING);
        }
    }

    /**
     * Evaluate error alerts from device
     */
    private void evaluateErrorAlert(Device device, String value) {
        createAlert(device,
                "DEVICE_ERROR",
                "Device reported error: " + value,
                Alert.AlertSeverity.WARNING);
    }

    /**
     * Evaluate health-based alerts (offline, high error rate)
     */
    @Transactional
    public void evaluateHealthAlerts(Device device) {
        DeviceHealth health = deviceHealthRepository.findByDeviceId(device.getId())
                .orElse(null);

        if (health == null) {
            return;
        }

        // Check for high error rate
        if (health.getErrorRate().compareTo(ERROR_RATE_THRESHOLD) > 0) {
            // Check if alert already exists
            List<Alert> existingAlerts = alertRepository.findByDeviceIdAndAcknowledgedFalse(device.getId());
            boolean hasHighErrorAlert = existingAlerts.stream()
                    .anyMatch(a -> "HIGH_ERROR_RATE".equals(a.getAlertType()));

            if (!hasHighErrorAlert) {
                createAlert(device,
                        "HIGH_ERROR_RATE",
                        String.format("Device error rate is %.2f%%", health.getErrorRate()),
                        Alert.AlertSeverity.WARNING);
            }
        }

        // Check for consecutive failures
        if (health.getConsecutiveFailures() >= 3) {
            List<Alert> existingAlerts = alertRepository.findByDeviceIdAndAcknowledgedFalse(device.getId());
            boolean hasConsecutiveFailuresAlert = existingAlerts.stream()
                    .anyMatch(a -> "CONSECUTIVE_FAILURES".equals(a.getAlertType()));

            if (!hasConsecutiveFailuresAlert) {
                createAlert(device,
                        "CONSECUTIVE_FAILURES",
                        String.format("Device has %d consecutive failures", health.getConsecutiveFailures()),
                        Alert.AlertSeverity.CRITICAL);
            }
        }
    }

    /**
     * Alert for offline devices
     * Called by scheduled task that checks device heartbeats
     */
    @Transactional
    public void createOfflineAlert(Device device) {
        // Check if alert already exists
        List<Alert> existingAlerts = alertRepository.findByDeviceIdAndAcknowledgedFalse(device.getId());
        boolean hasOfflineAlert = existingAlerts.stream()
                .anyMatch(a -> "DEVICE_OFFLINE".equals(a.getAlertType()));

        if (!hasOfflineAlert) {
            createAlert(device,
                    "DEVICE_OFFLINE",
                    "Device has not reported in for more than 5 minutes",
                    Alert.AlertSeverity.CRITICAL);
        }
    }

    /**
     * Resolve alerts when device comes back online
     */
    @Transactional
    public void resolveOfflineAlerts(Device device) {
        List<Alert> offlineAlerts = alertRepository.findByDeviceIdAndAcknowledgedFalse(device.getId()).stream()
                .filter(a -> "DEVICE_OFFLINE".equals(a.getAlertType()))
                .toList();

        for (Alert alert : offlineAlerts) {
            acknowledgeAlert(alert.getId(), "SYSTEM_AUTO_RESOLVED");
        }
    }

    /**
     * Broadcast alert to WebSocket subscribers
     */
    private void broadcastAlert(Alert alert) {
        try {
            messagingTemplate.convertAndSend(ALERT_TOPIC, Map.of(
                    "id", alert.getId(),
                    "deviceId", alert.getDevice().getId(),
                    "alertType", alert.getAlertType(),
                    "severity", alert.getSeverity(),
                    "message", alert.getMessage(),
                    "acknowledged", alert.getAcknowledged(),
                    "timestamp", alert.getCreatedAt().toString()
            ));
        } catch (Exception e) {
            log.warn("Failed to broadcast alert via WebSocket", e);
        }
    }
}
