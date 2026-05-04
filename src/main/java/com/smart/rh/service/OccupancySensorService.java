package com.smart.rh.service;

import com.smart.rh.dto.sensors.OccupancyEventDto;
import com.smart.rh.dto.sensors.SensorUpdateDto;
import com.smart.rh.entity.OccupancyStatus;
import com.smart.rh.repository.BureauSensorRepository;
import com.smart.rh.repository.OccupancyStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public OccupancyStatus processReading(String sensorIdOrDeviceId, Boolean isOccupied, Integer motionDuration,
            BigDecimal confidenceLevel, Instant timestamp) {
        // Try to parse as numeric ID first, then look up by device ID
        var sensor = lookupSensor(sensorIdOrDeviceId);
        if (sensor == null) {
            log.warn("Sensor not found: {}", sensorIdOrDeviceId);
            return null;
        }

        OccupancyStatus status = new OccupancyStatus();
        status.setSensor(sensor);
        status.setIsOccupied(isOccupied);
        status.setMotionDuration(motionDuration);
        status.setConfidenceLevel(confidenceLevel);
        status.setStatusTimestamp(timestamp);

        OccupancyStatus saved = occupancyStatusRepository.save(status);
        log.debug("Occupancy status saved: sensor={}, occupied={}", sensor.getId(), isOccupied);
        
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
                .latestOccupancy(OccupancyEventDto.builder()
                        .sensorId(String.valueOf(saved.getSensor().getId()))
                        .isOccupied(saved.getIsOccupied())
                        .motionDuration(saved.getMotionDuration())
                        .confidenceLevel(saved.getConfidenceLevel())
                        .timestamp(saved.getStatusTimestamp())
                        .build())
                .build();
        
        messagingTemplate.convertAndSend("/topic/bureau/sensors", update);

        return saved;
    }

    /**
     * Look up sensor by numeric ID or device ID string
     * Handles both "1" (numeric) and "sensor-motion-01" (device ID)
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
