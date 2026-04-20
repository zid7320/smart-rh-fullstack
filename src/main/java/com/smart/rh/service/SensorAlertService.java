package com.smart.rh.service;

import com.smart.rh.entity.SensorAlert;
import com.smart.rh.repository.SensorAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Service for managing sensor alerts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SensorAlertService {

    private final SensorAlertRepository sensorAlertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public SensorAlert createAlert(Long sensorId, SensorAlert.AlertType alertType, BigDecimal threshold,
            BigDecimal actualValue) {
        SensorAlert alert = new SensorAlert();
        alert.setSensor(null); // Set via repository query
        alert.setAlertType(alertType);
        alert.setThresholdValue(threshold);
        alert.setActualValue(actualValue);
        alert.setIsActive(true);
        alert.setTriggeredAt(Instant.now());

        SensorAlert saved = sensorAlertRepository.save(alert);
        log.info("Alert created: type={}, sensor={}", alertType, sensorId);

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/bureau/alerts", saved);

        return saved;
    }

    @Transactional
    public void acknowledgeAlert(Long alertId, String acknowledgedBy) {
        sensorAlertRepository.findById(alertId).ifPresent(alert -> {
            alert.setIsActive(false);
            alert.setAcknowledgedAt(Instant.now());
            alert.setAcknowledgedBy(acknowledgedBy);
            sensorAlertRepository.save(alert);
            log.info("Alert acknowledged: id={}, by={}", alertId, acknowledgedBy);
        });
    }

    @Transactional(readOnly = true)
    public List<SensorAlert> getActiveAlerts() {
        return sensorAlertRepository.findByIsActiveTrueOrderByTriggeredAtDesc();
    }
}
