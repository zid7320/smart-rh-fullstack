package com.smart.rh.service;

import com.smart.rh.entity.OccupancyStatus;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.OccupancyStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Service for processing occupancy sensor readings
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OccupancySensorService {

    private final OccupancyStatusRepository occupancyStatusRepository;
    private final BureauSensorRepository bureauSensorRepository;

    @Transactional
    public OccupancyStatus processReading(Long sensorId, Boolean isOccupied, Integer motionDuration,
            BigDecimal confidenceLevel, Instant timestamp) {
        var sensor = bureauSensorRepository.findById(sensorId).orElse(null);
        if (sensor == null)
            return null;

        OccupancyStatus status = new OccupancyStatus();
        status.setSensor(sensor);
        status.setIsOccupied(isOccupied);
        status.setMotionDuration(motionDuration);
        status.setConfidenceLevel(confidenceLevel);
        status.setStatusTimestamp(timestamp);

        OccupancyStatus saved = occupancyStatusRepository.save(status);
        log.debug("Occupancy status saved: sensor={}, occupied={}", sensorId, isOccupied);

        return saved;
    }
}
