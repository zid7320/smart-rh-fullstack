package com.smart.rh.controller;

import com.smart.rh.dto.iot.AlertDto;
import com.smart.rh.entity.Alert;
import com.smart.rh.mapper.IotMapper;
import com.smart.rh.service.AlertingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for device alerts and notifications.
 */
@Tag(name = "Alerts", description = "Device alerts and notifications")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RH')")
public class AlertController {

    private final AlertingService alertingService;
    private final IotMapper iotMapper;

    @Operation(summary = "Get all recent alerts")
    @GetMapping
    public Page<AlertDto> getRecentAlerts(
            @PageableDefault(size = 20) Pageable pageable) {
        return alertingService.getRecentAlerts(pageable)
                .map(iotMapper::toAlertDto);
    }

    @Operation(summary = "Get alerts for a specific device")
    @GetMapping("/device/{deviceId}")
    public Page<AlertDto> getAlertsByDevice(
            @PathVariable Long deviceId,
            @PageableDefault(size = 20) Pageable pageable) {
        return alertingService.getAlertsByDevice(deviceId, pageable)
                .map(iotMapper::toAlertDto);
    }

    @Operation(summary = "Get unacknowledged alerts")
    @GetMapping("/unacknowledged")
    public List<AlertDto> getUnacknowledgedAlerts() {
        return alertingService.getUnacknowledgedAlerts()
                .stream().map(iotMapper::toAlertDto).toList();
    }

    @Operation(summary = "Get unacknowledged alerts for a device")
    @GetMapping("/device/{deviceId}/unacknowledged")
    public List<AlertDto> getUnacknowledgedAlertsByDevice(
            @PathVariable Long deviceId) {
        return alertingService.getUnacknowledgedAlertsByDevice(deviceId)
                .stream().map(iotMapper::toAlertDto).toList();
    }

    @Operation(summary = "Get alerts by severity")
    @GetMapping("/severity/{severity}")
    public List<AlertDto> getAlertsBySeverity(
            @PathVariable Alert.AlertSeverity severity) {
        return alertingService.getAlertsBySeverity(severity)
                .stream().map(iotMapper::toAlertDto).toList();
    }

    @Operation(summary = "Acknowledge an alert")
    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<AlertDto> acknowledgeAlert(
            @PathVariable Long alertId,
            @RequestBody AcknowledgeAlertRequest request) {
        Alert alert = alertingService.acknowledgeAlert(alertId, request.acknowledgedBy());
        return ResponseEntity.ok(iotMapper.toAlertDto(alert));
    }

    @Operation(summary = "Get unacknowledged alert count")
    @GetMapping("/count/unacknowledged")
    public ResponseEntity<Long> getUnacknowledgedCount() {
        return ResponseEntity.ok(alertingService.getUnacknowledgedAlertCount());
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    public record AcknowledgeAlertRequest(
            String acknowledgedBy
    ) {}
}
