package com.smart.rh.mapper;

import com.smart.rh.dto.iot.AlertDto;
import com.smart.rh.dto.iot.DeviceDto;
import com.smart.rh.dto.iot.DeviceHealthDto;
import com.smart.rh.dto.iot.SensorReadingDto;
import com.smart.rh.entity.Alert;
import com.smart.rh.entity.Device;
import com.smart.rh.entity.DeviceHealth;
import com.smart.rh.entity.SensorReading;
import org.springframework.stereotype.Component;

/**
 * Mappers for IoT DTOs
 */
@Component
public class IotMapper {

    public DeviceDto toDeviceDto(Device device) {
        if (device == null) return null;

        return new DeviceDto(
                device.getId(),
                device.getName(),
                device.getDeviceType(),
                device.getStatus().toString(),
                device.getLocation(),
                device.getDescription(),
                device.getMqttUsername(),
                device.getLastHeartbeat(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    public DeviceHealthDto toDeviceHealthDto(DeviceHealth health) {
        if (health == null) return null;

        return new DeviceHealthDto(
                health.getId(),
                health.getDevice().getId(),
                health.getMessagesReceived(),
                health.getMessagesFailed(),
                health.getMessagesProcessed(),
                health.getUptimePercent(),
                health.getErrorRate(),
                health.getLastChecked(),
                health.getLastError(),
                health.getConsecutiveFailures()
        );
    }

    public AlertDto toAlertDto(Alert alert) {
        if (alert == null) return null;

        return new AlertDto(
                alert.getId(),
                alert.getDevice().getId(),
                alert.getDevice().getName(),
                alert.getAlertType(),
                alert.getSeverity().toString(),
                alert.getMessage(),
                alert.getAcknowledged(),
                alert.getAcknowledgedBy(),
                alert.getCreatedAt()
        );
    }

    public SensorReadingDto toSensorReadingDto(SensorReading reading) {
        if (reading == null) return null;

        return new SensorReadingDto(
                reading.getId(),
                reading.getDevice().getId(),
                reading.getDevice().getName(),
                reading.getReadingType(),
                reading.getValue(),
                reading.getTimestamp(),
                reading.getMetadata()
        );
    }
}
