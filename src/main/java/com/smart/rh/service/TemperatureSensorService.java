package com.smart.rh.service;

import com.smart.rh.dto.sensors.SensorUpdateDto;
import com.smart.rh.dto.sensors.TemperatureReadingDto;
import com.smart.rh.entity.TemperatureReading;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.TemperatureReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Service for processing temperature readings
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TemperatureSensorService {

    private static final BigDecimal TEMP_MIN = BigDecimal.valueOf(-40);
    private static final BigDecimal TEMP_MAX = BigDecimal.valueOf(80);

    private final TemperatureReadingRepository temperatureReadingRepository;
    private final BureauSensorRepository bureauSensorRepository;
    private final SensorAlertService alertService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public TemperatureReading processReading(String sensorIdOrDeviceId, BigDecimal temperature, BigDecimal humidity,
            Instant timestamp) {
        // Validate range
        if (temperature.compareTo(TEMP_MIN) < 0 || temperature.compareTo(TEMP_MAX) > 0) {
            log.warn("Temperature out of range: {} (sensor: {})", temperature, sensorIdOrDeviceId);
            return null;
        }

        // Try to parse as numeric ID first, then look up by device ID
        var sensor = lookupSensor(sensorIdOrDeviceId);
        if (sensor == null) {
            log.warn("Sensor not found: {}", sensorIdOrDeviceId);
            return null;
        }

        TemperatureReading reading = new TemperatureReading();
        reading.setSensor(sensor);
        reading.setTemperature(temperature);
        reading.setHumidity(humidity);
        reading.setReadingTimestamp(timestamp);
        reading.setIsValid(true);

        TemperatureReading saved = temperatureReadingRepository.save(reading);
        log.debug("Temperature reading saved: sensor={}, temp={}", sensorIdOrDeviceId, temperature);
        
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
                .latestTemperature(TemperatureReadingDto.builder()
                        .id(saved.getId())
                        .sensorId(String.valueOf(saved.getSensor().getId()))
                        .temperature(saved.getTemperature())
                        .humidity(saved.getHumidity())
                        .timestamp(saved.getReadingTimestamp().toString())
                        .isValid(saved.getIsValid())
                        .build())
                .build();
        
        messagingTemplate.convertAndSend("/topic/bureau/sensors", update);

        return saved;
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
