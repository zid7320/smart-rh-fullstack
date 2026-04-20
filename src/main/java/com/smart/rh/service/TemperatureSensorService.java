package com.smart.rh.service;

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
    public TemperatureReading processReading(Long sensorId, BigDecimal temperature, BigDecimal humidity,
            Instant timestamp) {
        // Validate range
        if (temperature.compareTo(TEMP_MIN) < 0 || temperature.compareTo(TEMP_MAX) > 0) {
            log.warn("Temperature out of range: {} (sensor: {})", temperature, sensorId);
            return null;
        }

        var sensor = bureauSensorRepository.findById(sensorId).orElse(null);
        if (sensor == null)
            return null;

        TemperatureReading reading = new TemperatureReading();
        reading.setSensor(sensor);
        reading.setTemperature(temperature);
        reading.setHumidity(humidity);
        reading.setReadingTimestamp(timestamp);
        reading.setIsValid(true);

        TemperatureReading saved = temperatureReadingRepository.save(reading);
        log.debug("Temperature reading saved: sensor={}, temp={}", sensorId, temperature);

        return saved;
    }
}
