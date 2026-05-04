package com.smart.rh.service;

import com.smart.rh.dto.sensors.Co2EventDto;
import com.smart.rh.dto.sensors.SensorUpdateDto;
import com.smart.rh.entity.Co2Reading;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.Co2ReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Service for processing CO2 readings
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class Co2SensorService {

    private static final int CO2_ALARM_THRESHOLD = 1500;

    private final Co2ReadingRepository co2ReadingRepository;
    private final BureauSensorRepository bureauSensorRepository;
    private final SensorAlertService alertService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Co2Reading processReading(String sensorIdOrDeviceId, Integer co2Level, BigDecimal gasConcentration, Instant timestamp) {
        if (co2Level < 0 || co2Level > 5000) {
            log.warn("CO2 level out of range: {} (sensor: {})", co2Level, sensorIdOrDeviceId);
            return null;
        }

        // Try to parse as numeric ID first, then look up by device ID
        var sensor = lookupSensor(sensorIdOrDeviceId);
        if (sensor == null) {
            log.warn("Sensor not found: {}", sensorIdOrDeviceId);
            return null;
        }

        Co2Reading reading = new Co2Reading();
        reading.setSensor(sensor);
        reading.setCo2Level(co2Level);
        reading.setGasConcentration(gasConcentration);
        reading.setReadingTimestamp(timestamp);
        reading.setIsValid(true);
        reading.setAlarmTriggered(co2Level >= CO2_ALARM_THRESHOLD);

        Co2Reading saved = co2ReadingRepository.save(reading);
        log.debug("CO2 reading saved: sensor={}, level={}", sensorIdOrDeviceId, co2Level);
        
        // Broadcast to WebSocket subscribers with wrapped DTO
        SensorUpdateDto update = SensorUpdateDto.builder()
                .sensor(SensorUpdateDto.SensorInfoDto.builder()
                        .id(sensor.getId())
                        .deviceId(sensor.getDeviceId())
                        .name(sensor.getName())
                        .location(sensor.getLocation())
                        .sensorTypes(sensor.getSensorTypes())
                        .isActive(sensor.getIsActive())
                        .build())
                .latestCo2(Co2EventDto.builder()
                        .sensorId(String.valueOf(saved.getSensor().getId()))
                        .co2Level(saved.getCo2Level())
                        .gasConcentration(saved.getGasConcentration())
                        .timestamp(saved.getReadingTimestamp())
                        .build())
                .build();
        
        messagingTemplate.convertAndSend("/topic/bureau/sensors", update);

        return saved;
    }

    /**
     * Look up sensor by numeric ID or device ID string
     * Handles both "1" (numeric) and "sensor-co2-01" (device ID)
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
