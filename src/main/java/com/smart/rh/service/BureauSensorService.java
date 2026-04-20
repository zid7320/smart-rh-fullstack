package com.smart.rh.service;

import com.smart.rh.dto.sensors.*;
import com.smart.rh.entity.*;
import com.smart.rh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for bureau sensor management and dashboard data
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BureauSensorService {

    private final BureauSensorRepository bureauSensorRepository;
    private final TemperatureReadingRepository temperatureReadingRepository;
    private final Co2ReadingRepository co2ReadingRepository;
    private final OccupancyStatusRepository occupancyStatusRepository;
    private final SensorHealthRepository sensorHealthRepository;
    private final SensorAlertRepository sensorAlertRepository;

    /**
     * Register new sensor
     */
    @Transactional
    public BureauSensorDto registerSensor(BureauSensorDto dto) {
        log.info("Registering bureau sensor: deviceId={}, name={}", dto.getDeviceId(), dto.getName());

        if (bureauSensorRepository.existsByDeviceId(dto.getDeviceId())) {
            throw new RuntimeException("Sensor already registered: " + dto.getDeviceId());
        }

        BureauSensor sensor = new BureauSensor();
        sensor.setDeviceId(dto.getDeviceId());
        sensor.setName(dto.getName());
        sensor.setLocation(dto.getLocation());
        sensor.setSensorTypes(dto.getSensorTypes());
        sensor.setIsActive(true);

        BureauSensor saved = bureauSensorRepository.save(sensor);
        return mapToDto(saved);
    }

    /**
     * Get dashboard data for all sensors
     */
    @Transactional(readOnly = true)
    public List<BureauDashboardDto> getAllSensorsDashboard() {
        return bureauSensorRepository.findByIsActiveTrue().stream()
                .map(this::buildDashboard)
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard for specific sensor
     */
    @Transactional(readOnly = true)
    public BureauDashboardDto getSensorDashboard(Long sensorId) {
        Optional<BureauSensor> sensorOpt = bureauSensorRepository.findById(sensorId);
        return sensorOpt.map(this::buildDashboard).orElse(null);
    }

    /**
     * Build complete dashboard DTO for sensor
     */
    private BureauDashboardDto buildDashboard(BureauSensor sensor) {
        BureauDashboardDto dashboard = new BureauDashboardDto();
        dashboard.setSensor(mapToDto(sensor));

        // Latest readings
        temperatureReadingRepository.findLatestBySensor(sensor.getId())
                .ifPresent(t -> dashboard.setLatestTemperature(mapToDto(t)));

        co2ReadingRepository.findLatestBySensor(sensor.getId())
                .ifPresent(c -> dashboard.setLatestCo2(mapToDto(c)));

        occupancyStatusRepository.findLatestBySensor(sensor.getId())
                .ifPresent(o -> dashboard.setLatestOccupancy(mapToDto(o)));

        // Health
        sensorHealthRepository.findBySensorId(sensor.getId())
                .ifPresent(h -> dashboard.setHealth(mapToDto(h)));

        // Active alerts
        List<SensorAlert> activeAlerts = sensorAlertRepository
                .findBySensorIdAndIsActiveTrueOrderByTriggeredAtDesc(sensor.getId());
        dashboard.setActiveAlerts(activeAlerts.stream().map(this::mapToDto).collect(Collectors.toList()));

        // 24h history
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        dashboard.setTemperatureHistory(
                temperatureReadingRepository.findByTimeRange(sensor.getId(), last24h).stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList()));
        dashboard.setCo2History(
                co2ReadingRepository.findByTimeRange(sensor.getId(), last24h).stream()
                        .map(this::mapToDto)
                        .collect(Collectors.toList()));

        return dashboard;
    }

    // DTO mapping methods
    private BureauSensorDto mapToDto(BureauSensor sensor) {
        BureauSensorDto dto = new BureauSensorDto();
        dto.setId(sensor.getId());
        dto.setDeviceId(sensor.getDeviceId());
        dto.setName(sensor.getName());
        dto.setLocation(sensor.getLocation());
        dto.setSensorTypes(sensor.getSensorTypes());
        dto.setIsActive(sensor.getIsActive());
        return dto;
    }

    private TemperatureReadingDto mapToDto(TemperatureReading t) {
        TemperatureReadingDto dto = new TemperatureReadingDto();
        dto.setId(t.getId());
        dto.setSensorId(t.getSensor().getId());
        dto.setTemperature(t.getTemperature());
        dto.setHumidity(t.getHumidity());
        dto.setTimestamp(t.getReadingTimestamp().toString());
        dto.setIsValid(t.getIsValid());
        return dto;
    }

    private Co2ReadingDto mapToDto(Co2Reading c) {
        Co2ReadingDto dto = new Co2ReadingDto();
        dto.setId(c.getId());
        dto.setSensorId(c.getSensor().getId());
        dto.setCo2Level(c.getCo2Level());
        dto.setGasConcentration(c.getGasConcentration());
        dto.setTimestamp(c.getReadingTimestamp().toString());
        dto.setIsValid(c.getIsValid());
        dto.setAlarmTriggered(c.getAlarmTriggered());
        dto.setStatus(c.getCo2Level() < 1000 ? "Good" : c.getCo2Level() < 1500 ? "Warning" : "Alert");
        return dto;
    }

    private OccupancyStatusDto mapToDto(OccupancyStatus o) {
        OccupancyStatusDto dto = new OccupancyStatusDto();
        dto.setId(o.getId());
        dto.setSensorId(o.getSensor().getId());
        dto.setIsOccupied(o.getIsOccupied());
        dto.setMotionDuration(o.getMotionDuration());
        dto.setConfidenceLevel(o.getConfidenceLevel());
        dto.setTimestamp(o.getStatusTimestamp().toString());
        return dto;
    }

    private SensorHealthDto mapToDto(SensorHealth h) {
        SensorHealthDto dto = new SensorHealthDto();
        dto.setId(h.getId());
        dto.setSensorId(h.getSensor().getId());
        dto.setUptimeSeconds(h.getUptimeSeconds());
        dto.setBatteryLevel(h.getBatteryLevel());
        dto.setSignalStrength(h.getSignalStrength());
        dto.setErrorCount(h.getErrorCount());
        dto.setLastErrorMessage(h.getLastErrorMessage());
        dto.setHealthTimestamp(h.getHealthTimestamp().toString());
        // Online if heartbeat within last 5 minutes
        dto.setIsOnline(h.getHealthTimestamp().isAfter(Instant.now().minus(5, ChronoUnit.MINUTES)));
        return dto;
    }

    private SensorAlertDto mapToDto(SensorAlert a) {
        SensorAlertDto dto = new SensorAlertDto();
        dto.setId(a.getId());
        dto.setSensorId(a.getSensor().getId());
        dto.setAlertType(a.getAlertType().toString());
        dto.setThresholdValue(a.getThresholdValue());
        dto.setActualValue(a.getActualValue());
        dto.setIsActive(a.getIsActive());
        dto.setTriggeredAt(a.getTriggeredAt() != null ? a.getTriggeredAt().toString() : null);
        dto.setAcknowledgedAt(a.getAcknowledgedAt() != null ? a.getAcknowledgedAt().toString() : null);
        dto.setAcknowledgedBy(a.getAcknowledgedBy());
        dto.setCreatedAt(a.getCreatedAt().toString());
        return dto;
    }
}
