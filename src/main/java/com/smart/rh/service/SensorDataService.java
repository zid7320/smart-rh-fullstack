package com.smart.rh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.rh.entity.Device;
import com.smart.rh.entity.SensorReading;
import com.smart.rh.repository.DeviceRepository;
import com.smart.rh.repository.SensorReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for processing and storing sensor readings from IoT devices.
 * Handles validation, persistence, and real-time WebSocket broadcasting.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SensorDataService {

    private static final String SENSOR_TOPIC = "/topic/sensor-data";
    private static final int MAX_READING_SIZE = 1000;

    private final SensorReadingRepository sensorReadingRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final AlertingService alertingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Ingest a sensor reading from an MQTT message.
     * Validates payload, persists to database, broadcasts via WebSocket.
     */
    @Transactional
    public SensorReading ingestReading(String deviceId, String readingType, String value, Map<String, Object> metadata) {
        try {
            // Find device by MQTT username or ID
            Optional<Device> deviceOpt = Optional.empty();
            if (deviceId != null && !deviceId.isEmpty()) {
                try {
                    Long id = Long.parseLong(deviceId);
                    deviceOpt = deviceRepository.findById(id);
                } catch (NumberFormatException e) {
                    // Try to find by mqtt_username if ID parse fails
                    deviceOpt = deviceRepository.findByMqttUsername(deviceId);
                }
            }

            if (deviceOpt.isEmpty()) {
                log.warn("Received reading for unknown device: {}", deviceId);
                return null;
            }

            Device device = deviceOpt.get();

            // Validate reading type
            if (readingType == null || readingType.isEmpty()) {
                log.warn("Received reading with empty type from device: {}", deviceId);
                return null;
            }

            // Validate value size
            if (value != null && value.length() > MAX_READING_SIZE) {
                log.warn("Reading value exceeds max size for device: {}", deviceId);
                value = value.substring(0, MAX_READING_SIZE);
            }

            // Create and persist reading
            SensorReading reading = new SensorReading();
            reading.setDevice(device);
            reading.setReadingType(readingType);
            reading.setValue(value);
            reading.setTimestamp(Instant.now());
            reading.setMetadata(serializeMetadata(metadata));
            reading.setProcessingStatus(SensorReading.ProcessingStatus.PROCESSED);

            SensorReading saved = sensorReadingRepository.save(reading);

            // Record device heartbeat
            deviceService.recordHeartbeat(device.getId());

            // Broadcast to WebSocket subscribers
            broadcastReading(saved);

            // Check for alerts based on reading content
            alertingService.evaluateAlerts(device, readingType, value);

            log.debug("Sensor reading ingested: device={} type={} value={}",
                    device.getId(), readingType, value);

            return saved;

        } catch (Exception e) {
            log.error("Error ingesting sensor reading from device: {}", deviceId, e);
            // Try to record error for device health tracking
            try {
                Long deviceIdLong = Long.parseLong(deviceId);
                deviceService.recordDeviceError(deviceIdLong, e.getMessage());
            } catch (Exception ex) {
                // Fail silently
            }
            return null;
        }
    }

    /**
     * Get sensor readings for a device with pagination
     */
    @Transactional(readOnly = true)
    public Page<SensorReading> getReadingsByDevice(Long deviceId, Pageable pageable) {
        return sensorReadingRepository.findByDeviceIdOrderByTimestampDesc(deviceId, pageable);
    }

    /**
     * Get latest readings for all devices (for dashboard)
     */
    @Transactional(readOnly = true)
    public List<SensorReading> getLatestReadings() {
        return sensorReadingRepository.findLatestReadingPerDevice();
    }

    /**
     * Get readings by type for a device
     */
    @Transactional(readOnly = true)
    public List<SensorReading> getReadingsByType(Long deviceId, String readingType) {
        return sensorReadingRepository.findByDeviceIdAndReadingTypeOrderByTimestampDesc(deviceId, readingType);
    }

    /**
     * Get readings within a time range for a device
     */
    @Transactional(readOnly = true)
    public List<SensorReading> getReadingsByTimeRange(Long deviceId, Instant startTime, Instant endTime) {
        return sensorReadingRepository.findByDeviceAndTimeRange(deviceId, startTime, endTime);
    }

    /**
     * Get a specific reading by ID
     */
    @Transactional(readOnly = true)
    public SensorReading getReadingById(Long readingId) {
        return sensorReadingRepository.findById(readingId).orElse(null);
    }

    /**
     * Count readings for a device
     */
    @Transactional(readOnly = true)
    public long getReadingCountForDevice(Long deviceId) {
        return sensorReadingRepository.countByDeviceId(deviceId);
    }

    /**
     * Cleanup old readings (data retention policy)
     * Called by a scheduled task
     */
    @Transactional
    public void cleanupOldReadings(Instant beforeTimestamp) {
        sensorReadingRepository.deleteOlderThan(beforeTimestamp);
        log.info("Cleaned up sensor readings older than: {}", beforeTimestamp);
    }

    /**
     * Broadcast sensor reading to WebSocket subscribers
     */
    private void broadcastReading(SensorReading reading) {
        try {
            messagingTemplate.convertAndSend(SENSOR_TOPIC, Map.of(
                    "deviceId", reading.getDevice().getId(),
                    "readingType", reading.getReadingType(),
                    "value", reading.getValue(),
                    "timestamp", reading.getTimestamp().toString(),
                    "metadata", reading.getMetadata()
            ));
        } catch (Exception e) {
            log.warn("Failed to broadcast sensor reading via WebSocket", e);
        }
    }

    /**
     * Serialize metadata map to JSON string
     */
    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to serialize metadata", e);
            return null;
        }
    }

    /**
     * Deserialize metadata JSON string to map
     */
    public Map<String, Object> deserializeMetadata(String metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadata, Map.class);
        } catch (Exception e) {
            log.warn("Failed to deserialize metadata", e);
            return Map.of();
        }
    }
}
