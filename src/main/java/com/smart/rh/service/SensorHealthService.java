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
    public SensorHealth recordHeartbeat(Long sensorId, Integer battery, Integer signal) {
        var sensor = bureauSensorRepository.findById(sensorId).orElse(null);
        if (sensor == null)
            return null;

        var existingOpt = sensorHealthRepository.findBySensorId(sensorId);
        SensorHealth health = existingOpt.orElseGet(SensorHealth::new);

        health.setSensor(sensor);
        health.setBatteryLevel(battery);
        health.setSignalStrength(signal);
        health.setHealthTimestamp(Instant.now());
        health.setErrorCount(0);

        SensorHealth saved = sensorHealthRepository.save(health);
        log.debug("Sensor health updated: sensor={}, battery={}, signal={}", sensorId, battery, signal);

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
}
