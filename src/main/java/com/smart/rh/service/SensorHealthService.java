package com.smart.rh.service;

import com.smart.rh.entity.SensorHealth;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.SensorHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service for monitoring sensor health and heartbeat
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SensorHealthService {

    private final SensorHealthRepository sensorHealthRepository;
    private final BureauSensorRepository bureauSensorRepository;
    private final SensorAlertService alertService;

    @Transactional
    public SensorHealth recordHeartbeat(String sensorIdOrDeviceId, Integer battery, Integer signal) {
        // Try to parse as numeric ID first, then look up by device ID
        var sensor = lookupSensor(sensorIdOrDeviceId);
        if (sensor == null) {
            log.warn("Sensor not found for heartbeat: {}", sensorIdOrDeviceId);
            return null;
        }

        var existingOpt = sensorHealthRepository.findBySensorId(sensor.getId());
        SensorHealth health = existingOpt.orElseGet(SensorHealth::new);

        health.setSensor(sensor);
        health.setBatteryLevel(battery);
        health.setSignalStrength(signal);
        health.setHealthTimestamp(Instant.now());
        health.setErrorCount(0);

        SensorHealth saved = sensorHealthRepository.save(health);
        log.debug("Sensor health updated: sensor={}, battery={}, signal={}", sensor.getId(), battery, signal);

        return saved;
    }

    @Transactional
    public void detectOfflineSensors() {
        Instant threshold = Instant.now().minus(5, ChronoUnit.MINUTES);
        var staleRecords = sensorHealthRepository.findStaleHealthRecords(threshold);

        for (SensorHealth health : staleRecords) {
            if (health.getSensor().getIsActive()) {
                log.warn("Sensor offline detected: {}", health.getSensor().getId());
                alertService.createAlert(
                        health.getSensor().getId(),
                        com.smart.rh.entity.SensorAlert.AlertType.OFFLINE,
                        null,
                        null);
            }
        }
    }

    /**
     * Look up sensor by numeric ID or device ID string
     * Handles both "1" (numeric) and "sensor-temp-01" (device ID)
     */
    private com.smart.rh.entity.BureauSensor lookupSensor(String sensorIdOrDeviceId) {
        // Try parsing as numeric ID first
        try {
            Long numericId = Long.parseLong(sensorIdOrDeviceId);
            return bureauSensorRepository.findById(numericId).orElse(null);
        } catch (NumberFormatException e) {
            // Not a number, treat as device ID
            log.debug("Looking up sensor by device ID: {}", sensorIdOrDeviceId);
        }
        
        // Look up by device ID
        return bureauSensorRepository.findByDeviceId(sensorIdOrDeviceId).orElse(null);
    }
}
