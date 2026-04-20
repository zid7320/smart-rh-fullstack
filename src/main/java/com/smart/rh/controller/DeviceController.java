package com.smart.rh.controller;

import com.smart.rh.dto.iot.DeviceDto;
import com.smart.rh.dto.iot.DeviceHealthDto;
import com.smart.rh.entity.Device;
import com.smart.rh.entity.DeviceHealth;
import com.smart.rh.entity.DeviceType;
import com.smart.rh.mapper.IotMapper;
import com.smart.rh.service.DeviceHealthService;
import com.smart.rh.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for IoT device management.
 * Register, configure, and monitor connected devices.
 */
@Tag(name = "IoT Devices", description = "Device registration, management, and monitoring")
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RH')")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceHealthService deviceHealthService;
    private final IotMapper iotMapper;

    // ── Device Registration & Management ──────────────────────────────────────

    @Operation(summary = "Register a new IoT device",
               description = "Creates a new device and generates MQTT credentials")
    @PostMapping
    public ResponseEntity<DeviceDto> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request) {
        Device device = deviceService.registerDevice(
                request.name(),
                request.deviceType(),
                request.location(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iotMapper.toDeviceDto(device));
    }

    @Operation(summary = "Get all devices")
    @GetMapping
    public Page<DeviceDto> getAllDevices(
            @PageableDefault(size = 20) Pageable pageable) {
        return deviceService.getAllDevices(pageable)
                .map(iotMapper::toDeviceDto);
    }

    @Operation(summary = "Get devices by type")
    @GetMapping("/by-type/{type}")
    public Page<DeviceDto> getDevicesByType(
            @PathVariable DeviceType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return deviceService.getDevicesByType(type, pageable)
                .map(iotMapper::toDeviceDto);
    }

    @Operation(summary = "Get device details by ID")
    @GetMapping("/{deviceId}")
    public ResponseEntity<DeviceDto> getDevice(@PathVariable Long deviceId) {
        Device device = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(iotMapper.toDeviceDto(device));
    }

    @Operation(summary = "Update device information")
    @PutMapping("/{deviceId}")
    public ResponseEntity<DeviceDto> updateDevice(
            @PathVariable Long deviceId,
            @Valid @RequestBody UpdateDeviceRequest request) {
        Device device = deviceService.updateDevice(
                deviceId,
                request.name(),
                request.location(),
                request.description()
        );
        return ResponseEntity.ok(iotMapper.toDeviceDto(device));
    }

    @Operation(summary = "Update device status")
    @PatchMapping("/{deviceId}/status")
    public ResponseEntity<DeviceDto> updateDeviceStatus(
            @PathVariable Long deviceId,
            @RequestBody StatusUpdateRequest request) {
        Device device = deviceService.updateDeviceStatus(deviceId, request.status());
        return ResponseEntity.ok(iotMapper.toDeviceDto(device));
    }

    @Operation(summary = "Delete a device")
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

    // ── Device Credentials ────────────────────────────────────────────────────

    @Operation(summary = "Get MQTT credentials for a device")
    @GetMapping("/{deviceId}/credentials")
    public ResponseEntity<MqttCredentialsDto> getCredentials(@PathVariable Long deviceId) {
        Device device = deviceService.getDeviceById(deviceId);
        return ResponseEntity.ok(new MqttCredentialsDto(
                device.getMqttUsername(),
                "[REDACTED]" // Never send password in response
        ));
    }

    @Operation(summary = "Reset device credentials (generates new MQTT password)")
    @PostMapping("/{deviceId}/reset-credentials")
    public ResponseEntity<MqttCredentialsDto> resetCredentials(@PathVariable Long deviceId) {
        Device device = deviceService.resetDeviceCredentials(deviceId);
        // In a real app, you'd return the new credentials securely
        return ResponseEntity.ok(new MqttCredentialsDto(
                device.getMqttUsername(),
                "[NEW_PASSWORD_SENT_SECURELY]"
        ));
    }

    // ── Device Health ─────────────────────────────────────────────────────────

    @Operation(summary = "Get health metrics for a device")
    @GetMapping("/{deviceId}/health")
    public ResponseEntity<DeviceHealthDto> getDeviceHealth(@PathVariable Long deviceId) {
        DeviceHealth health = deviceHealthService.getDeviceHealth(deviceId);
        return ResponseEntity.ok(iotMapper.toDeviceHealthDto(health));
    }

    @Operation(summary = "Get system health summary")
    @GetMapping("/health/summary")
    public ResponseEntity<HealthSummaryDto> getHealthSummary() {
        DeviceHealthService.HealthSummary summary = deviceHealthService.getHealthSummary();
        return ResponseEntity.ok(new HealthSummaryDto(
                summary.totalDevices(),
                summary.activeDevices(),
                summary.offlineDevices(),
                summary.errorDevices(),
                summary.averageErrorRate().doubleValue(),
                summary.averageUptime().doubleValue()
        ));
    }

    @Operation(summary = "Reset health metrics for a device")
    @PostMapping("/{deviceId}/health/reset")
    public ResponseEntity<Void> resetHealthMetrics(@PathVariable Long deviceId) {
        deviceHealthService.resetHealthMetrics(deviceId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Check all device health status")
    @PostMapping("/health/check-all")
    public ResponseEntity<Void> checkAllDeviceHealth() {
        deviceHealthService.checkAllDeviceHealth();
        return ResponseEntity.noContent().build();
    }

    // ── DTOs ──────────────────────────────────────────────────────────────────

    public record RegisterDeviceRequest(
            String name,
            DeviceType deviceType,
            String location,
            String description
    ) {}

    public record UpdateDeviceRequest(
            String name,
            String location,
            String description
    ) {}

    public record StatusUpdateRequest(
            Device.DeviceStatus status
    ) {}

    public record MqttCredentialsDto(
            String username,
            String password
    ) {}

    public record HealthSummaryDto(
            long totalDevices,
            long activeDevices,
            long offlineDevices,
            long errorDevices,
            double averageErrorRate,
            double averageUptime
    ) {}
}
