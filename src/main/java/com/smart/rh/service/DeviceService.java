package com.smart.rh.service;

import com.smart.rh.entity.Device;
import com.smart.rh.entity.DeviceHealth;
import com.smart.rh.entity.DeviceType;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.repository.DeviceRepository;
import com.smart.rh.repository.DeviceHealthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing IoT devices: registration, provisioning, status tracking.
 * Handles credential generation and device lifecycle.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceHealthRepository deviceHealthRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Register a new IoT device.
     * Auto-generates MQTT credentials and creates a DeviceHealth record.
     */
    @Transactional
    public Device registerDevice(String name, DeviceType deviceType, String location, String description) {
        // Validate device name uniqueness
        if (deviceRepository.existsByName(name)) {
            throw new IllegalArgumentException("Device with name '" + name + "' already exists");
        }

        // Generate unique MQTT credentials
        String mqttUsername = generateMqttUsername(deviceType);
        String plainPassword = generateMqttPassword();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        Device device = new Device();
        device.setName(name);
        device.setDeviceType(deviceType);
        device.setLocation(location);
        device.setDescription(description);
        device.setMqttUsername(mqttUsername);
        device.setMqttPasswordHash(hashedPassword);
        device.setStatus(Device.DeviceStatus.INACTIVE);

        Device savedDevice = deviceRepository.save(device);

        // DeviceHealth is auto-created by trigger, but ensure it exists
        if (!deviceHealthRepository.findByDeviceId(savedDevice.getId()).isPresent()) {
            DeviceHealth health = new DeviceHealth();
            health.setDevice(savedDevice);
            deviceHealthRepository.save(health);
        }

        auditService.log("DEVICE_REGISTERED", "Device", savedDevice.getId(),
                String.format("name=%s type=%s mqtt_user=%s", name, deviceType, mqttUsername));

        log.info("Device registered: id={} name={} type={} mqttUsername={}",
                savedDevice.getId(), name, deviceType, mqttUsername);

        return savedDevice;
    }

    /**
     * Get device by ID with complete details
     */
    @Transactional(readOnly = true)
    public Device getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
    }

    /**
     * Get device by MQTT username (used for credential validation)
     */
    @Transactional(readOnly = true)
    public Device getDeviceByMqttUsername(String mqttUsername) {
        return deviceRepository.findByMqttUsername(mqttUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "mqttUsername", mqttUsername));
    }

    /**
     * Verify MQTT credentials (used during MQTT connection)
     */
    @Transactional(readOnly = true)
    public boolean verifyMqttCredentials(String mqttUsername, String plainPassword) {
        return deviceRepository.findByMqttUsername(mqttUsername)
                .map(device -> {
                    boolean valid = passwordEncoder.matches(plainPassword, device.getMqttPasswordHash());
                    if (!valid) {
                        log.warn("Invalid MQTT credentials for device: {}", mqttUsername);
                    }
                    return valid;
                })
                .orElse(false);
    }

    /**
     * List all devices with pagination
     */
    @Transactional(readOnly = true)
    public Page<Device> getAllDevices(Pageable pageable) {
        return deviceRepository.findAllByOrderByUpdatedAtDesc(pageable);
    }

    /**
     * List devices by type
     */
    @Transactional(readOnly = true)
    public Page<Device> getDevicesByType(DeviceType deviceType, Pageable pageable) {
        return deviceRepository.findByDeviceTypeOrderByUpdatedAtDesc(deviceType, pageable);
    }

    /**
     * Update device metadata
     */
    @Transactional
    public Device updateDevice(Long deviceId, String name, String location, String description) {
        Device device = getDeviceById(deviceId);

        if (name != null && !name.equals(device.getName())) {
            if (deviceRepository.existsByName(name)) {
                throw new IllegalArgumentException("Device with name '" + name + "' already exists");
            }
            device.setName(name);
        }

        if (location != null) {
            device.setLocation(location);
        }

        if (description != null) {
            device.setDescription(description);
        }

        Device updated = deviceRepository.save(device);
        auditService.log("DEVICE_UPDATED", "Device", deviceId,
                String.format("name=%s location=%s", name, location));

        return updated;
    }

    /**
     * Update device status
     */
    @Transactional
    public Device updateDeviceStatus(Long deviceId, Device.DeviceStatus status) {
        Device device = getDeviceById(deviceId);
        device.setStatus(status);
        Device updated = deviceRepository.save(device);

        auditService.log("DEVICE_STATUS_CHANGED", "Device", deviceId,
                String.format("status=%s", status));

        return updated;
    }

    /**
     * Record a device heartbeat (called when device publishes to heartbeat topic)
     */
    @Transactional
    public void recordHeartbeat(Long deviceId) {
        Device device = getDeviceById(deviceId);

        // Update last heartbeat timestamp
        device.setLastHeartbeat(Instant.now());
        device.setStatus(Device.DeviceStatus.ACTIVE);
        deviceRepository.save(device);

        // Update device health
        DeviceHealth health = deviceHealthRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (health != null) {
            health.recordSuccess();
            health.setLastChecked(Instant.now());
            deviceHealthRepository.save(health);
        }
    }

    /**
     * Record a device error
     */
    @Transactional
    public void recordDeviceError(Long deviceId, String errorMessage) {
        Device device = getDeviceById(deviceId);

        // Update device health
        DeviceHealth health = deviceHealthRepository.findByDeviceId(deviceId)
                .orElse(null);

        if (health != null) {
            health.recordFailure(errorMessage);
            health.setLastChecked(Instant.now());
            deviceHealthRepository.save(health);

            // If too many consecutive failures, mark device as error
            if (health.getConsecutiveFailures() >= 5) {
                device.setStatus(Device.DeviceStatus.ERROR);
                deviceRepository.save(device);
                log.warn("Device marked as ERROR: id={} errors={}", deviceId, health.getConsecutiveFailures());
            }
        }
    }

    /**
     * Reset device credentials (generates new MQTT username and password)
     */
    @Transactional
    public Device resetDeviceCredentials(Long deviceId) {
        Device device = getDeviceById(deviceId);

        String newMqttUsername = generateMqttUsername(device.getDeviceType());
        String newPlainPassword = generateMqttPassword();
        String newHashedPassword = passwordEncoder.encode(newPlainPassword);

        device.setMqttUsername(newMqttUsername);
        device.setMqttPasswordHash(newHashedPassword);

        Device updated = deviceRepository.save(device);

        auditService.log("DEVICE_CREDENTIALS_RESET", "Device", deviceId,
                String.format("new_mqtt_user=%s", newMqttUsername));

        log.info("Device credentials reset: id={} new_mqttUsername={}", deviceId, newMqttUsername);

        return updated;
    }

    /**
     * Delete a device (soft delete via repository method or cascade)
     */
    @Transactional
    public void deleteDevice(Long deviceId) {
        Device device = getDeviceById(deviceId);

        auditService.log("DEVICE_DELETED", "Device", deviceId,
                String.format("name=%s type=%s", device.getName(), device.getDeviceType()));

        deviceRepository.delete(device);
        log.info("Device deleted: id={} name={}", deviceId, device.getName());
    }

    /**
     * Detect offline devices (no heartbeat in last 5 minutes)
     */
    @Transactional
    public void detectOfflineDevices() {
        Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
        List<Device> offlineDevices = deviceRepository.findOfflineDevices(fiveMinutesAgo);

        for (Device device : offlineDevices) {
            if (device.getStatus() != Device.DeviceStatus.OFFLINE) {
                device.setStatus(Device.DeviceStatus.OFFLINE);
                deviceRepository.save(device);

                auditService.log("DEVICE_OFFLINE_DETECTED", "Device", device.getId(),
                        String.format("last_heartbeat=%s", device.getLastHeartbeat()));

                log.warn("Device detected as offline: id={} name={}", device.getId(), device.getName());
            }
        }
    }

    /**
     * Get count of devices by status (for dashboard)
     */
    @Transactional(readOnly = true)
    public long getDeviceCountByStatus(Device.DeviceStatus status) {
        return deviceRepository.countByStatus(status);
    }

    /**
     * Generate unique MQTT username: device_type_uuid
     */
    private String generateMqttUsername(DeviceType deviceType) {
        return String.format("%s_%s",
                deviceType.toString().toLowerCase(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Generate strong MQTT password: 32 character random string
     */
    private String generateMqttPassword() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
