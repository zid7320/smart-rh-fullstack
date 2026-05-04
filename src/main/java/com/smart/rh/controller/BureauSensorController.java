package com.smart.rh.controller;

import com.smart.rh.dto.sensors.*;
import com.smart.rh.entity.*;
import com.smart.rh.repository.*;
import com.smart.rh.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for Bureau Sensors Dashboard
 * Endpoints for sensor management and real-time metrics retrieval
 */
@Slf4j
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class BureauSensorController {

    private final BureauSensorService bureauSensorService;
    private final BureauSensorRepository bureauSensorRepository;
    private final TemperatureReadingRepository temperatureReadingRepository;
    private final Co2ReadingRepository co2ReadingRepository;
    private final OccupancyStatusRepository occupancyStatusRepository;
    private final SensorAlertRepository sensorAlertRepository;
    private final SensorHealthRepository sensorHealthRepository;

    // ── Sensor Management ────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<BureauSensorDto> registerSensor(@RequestBody BureauSensorDto dto) {
        log.info("Registering bureau sensor: {}", dto.getDeviceId());
        BureauSensor sensor = new BureauSensor();
        sensor.setDeviceId(dto.getDeviceId());
        sensor.setName(dto.getName());
        sensor.setLocation(dto.getLocation());
        sensor.setSensorTypes(dto.getSensorTypes());
        sensor.setIsActive(true);
        BureauSensor saved = bureauSensorRepository.save(sensor);
        return ResponseEntity.ok(mapToDto(saved));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<BureauSensorDto>> getAllSensors() {
        List<BureauSensor> sensors = bureauSensorRepository.findByIsActiveTrue();
        return ResponseEntity.ok(sensors.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<BureauSensorDto> getSensor(@PathVariable Long id) {
        return bureauSensorRepository.findById(id)
                .map(sensor -> ResponseEntity.ok(mapToDto(sensor)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<BureauSensorDto> updateSensor(@PathVariable Long id, @RequestBody BureauSensorDto dto) {
        return bureauSensorRepository.findById(id)
                .map(sensor -> {
                    sensor.setName(dto.getName());
                    sensor.setLocation(dto.getLocation());
                    sensor.setSensorTypes(dto.getSensorTypes());
                    sensor.setIsActive(dto.getIsActive());
                    BureauSensor saved = bureauSensorRepository.save(sensor);
                    log.info("Updated bureau sensor: {}", id);
                    return ResponseEntity.ok(mapToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Dashboard Endpoints ──────────────────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<BureauDashboardDto>> getAllSensorsDashboard() {
        log.info("🔵 API CALL: GET /api/sensors/dashboard - Loading all sensors dashboard");
        List<BureauDashboardDto> dashboards = bureauSensorService.getAllSensorsDashboard();
        log.info("🔵 API RESPONSE: Found {} dashboards", dashboards.size());
        dashboards.forEach(d -> log.info("   - Sensor: {}, temp: {}", 
            d.getSensor().getId(), 
            d.getLatestTemperature() != null ? d.getLatestTemperature().getTemperature() : "null"));
        return ResponseEntity.ok(dashboards);
    }

    @GetMapping("/{id}/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<BureauDashboardDto> getSensorDashboard(@PathVariable Long id) {
        log.debug("Loading dashboard for sensor: {}", id);
        BureauDashboardDto dashboard = bureauSensorService.getSensorDashboard(id);
        return ResponseEntity.ok(dashboard);
    }

    // ── Temperature Endpoints ────────────────────────────────────────────────

    @GetMapping("/{id}/temperature/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<TemperatureReadingDto> getLatestTemperature(@PathVariable Long id) {
        var reading = temperatureReadingRepository.findLatestBySensor(id);
        return reading.map(r -> ResponseEntity.ok(mapToDto(r)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/temperature/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<TemperatureReadingDto>> getTemperatureHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") Integer hours) {

        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<TemperatureReading> readings = temperatureReadingRepository.findByTimeRange(id, since);
        return ResponseEntity.ok(readings.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    // ── CO2 Endpoints ────────────────────────────────────────────────────────

    @GetMapping("/{id}/co2/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<Co2ReadingDto> getLatestCo2(@PathVariable Long id) {
        var reading = co2ReadingRepository.findLatestBySensor(id);
        return reading.map(r -> ResponseEntity.ok(mapToDto(r)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/co2/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<Co2ReadingDto>> getCo2History(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") Integer hours) {

        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<Co2Reading> readings = co2ReadingRepository.findByTimeRange(id, since);
        return ResponseEntity.ok(readings.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    // ── Occupancy Endpoints ──────────────────────────────────────────────────

    @GetMapping("/{id}/occupancy/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<OccupancyStatusDto> getLatestOccupancy(@PathVariable Long id) {
        var reading = occupancyStatusRepository.findLatestBySensor(id);
        return reading.map(r -> ResponseEntity.ok(mapToDto(r)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}/occupancy/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<OccupancyStatusDto>> getOccupancyHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") Integer hours) {

        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<OccupancyStatus> readings = occupancyStatusRepository.findByTimeRange(id, since);
        return ResponseEntity.ok(readings.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    // ── Alert Endpoints ─────────────────────────────────────────────────────

    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<SensorAlertDto>> getActiveAlerts() {
        List<SensorAlert> alerts = sensorAlertRepository.findByIsActiveTrueOrderByTriggeredAtDesc();
        return ResponseEntity.ok(alerts.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @PutMapping("/alerts/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<SensorAlertDto> acknowledgeAlert(
            @PathVariable Long id,
            @RequestParam String acknowledgedBy) {

        return sensorAlertRepository.findById(id)
                .map(alert -> {
                    alert.setIsActive(false);
                    alert.setAcknowledgedAt(Instant.now());
                    alert.setAcknowledgedBy(acknowledgedBy);
                    SensorAlert saved = sensorAlertRepository.save(alert);
                    log.info("Alert acknowledged: {} by {}", id, acknowledgedBy);
                    return ResponseEntity.ok(mapToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private BureauSensorDto mapToDto(BureauSensor sensor) {
        return BureauSensorDto.builder()
                .id(sensor.getId())
                .deviceId(sensor.getDeviceId())
                .name(sensor.getName())
                .location(sensor.getLocation())
                .sensorTypes(sensor.getSensorTypes())
                .isActive(sensor.getIsActive())
                .lastUpdate(sensor.getUpdatedAt() != null ? sensor.getUpdatedAt().toString() : null)
                .build();
    }

    private TemperatureReadingDto mapToDto(TemperatureReading reading) {
        return TemperatureReadingDto.builder()
                .id(reading.getId())
                .sensorId(String.valueOf(reading.getSensor().getId()))
                .temperature(reading.getTemperature())
                .humidity(reading.getHumidity())
                .timestamp(reading.getReadingTimestamp().toString())
                .isValid(reading.getIsValid())
                .build();
    }

    private Co2ReadingDto mapToDto(Co2Reading reading) {
        String status = "Good";
        if (reading.getCo2Level() >= 1500) {
            status = "Alert";
        } else if (reading.getCo2Level() >= 1000) {
            status = "Warning";
        }
        return Co2ReadingDto.builder()
                .id(reading.getId())
                .sensorId(String.valueOf(reading.getSensor().getId()))
                .co2Level(reading.getCo2Level())
                .gasConcentration(reading.getGasConcentration())
                .timestamp(reading.getReadingTimestamp().toString())
                .isValid(reading.getIsValid())
                .alarmTriggered(reading.getAlarmTriggered())
                .status(status)
                .build();
    }

    private OccupancyStatusDto mapToDto(OccupancyStatus status) {
        return OccupancyStatusDto.builder()
                .id(status.getId())
                .sensorId(String.valueOf(status.getSensor().getId()))
                .isOccupied(status.getIsOccupied())
                .motionDuration(status.getMotionDuration())
                .confidenceLevel(status.getConfidenceLevel())
                .timestamp(status.getStatusTimestamp().toString())
                .build();
    }

    private SensorAlertDto mapToDto(SensorAlert alert) {
        return SensorAlertDto.builder()
                .id(alert.getId())
                .sensorId(String.valueOf(alert.getSensor().getId()))
                .alertType(alert.getAlertType().toString())
                .thresholdValue(alert.getThresholdValue())
                .actualValue(alert.getActualValue())
                .isActive(alert.getIsActive())
                .triggeredAt(alert.getTriggeredAt() != null ? alert.getTriggeredAt().toString() : null)
                .acknowledgedAt(alert.getAcknowledgedAt() != null ? alert.getAcknowledgedAt().toString() : null)
                .acknowledgedBy(alert.getAcknowledgedBy())
                .createdAt(alert.getCreatedAt().toString())
                .build();
    }
}
