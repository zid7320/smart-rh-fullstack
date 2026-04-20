package com.smart.rh.service;

import com.smart.rh.entity.Co2Reading;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.Co2ReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public Co2Reading processReading(Long sensorId, Integer co2Level, BigDecimal gasConcentration, Instant timestamp) {
        if (co2Level < 0 || co2Level > 5000) {
            log.warn("CO2 level out of range: {} (sensor: {})", co2Level, sensorId);
            return null;
        }

        var sensor = bureauSensorRepository.findById(sensorId).orElse(null);
        if (sensor == null)
            return null;

        Co2Reading reading = new Co2Reading();
        reading.setSensor(sensor);
        reading.setCo2Level(co2Level);
        reading.setGasConcentration(gasConcentration);
        reading.setReadingTimestamp(timestamp);
        reading.setIsValid(true);
        reading.setAlarmTriggered(co2Level >= CO2_ALARM_THRESHOLD);

        Co2Reading saved = co2ReadingRepository.save(reading);
        log.debug("CO2 reading saved: sensor={}, level={}", sensorId, co2Level);

        return saved;
    }
}
