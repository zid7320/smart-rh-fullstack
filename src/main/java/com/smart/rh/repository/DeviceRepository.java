package com.smart.rh.repository;

import com.smart.rh.entity.Device;
import com.smart.rh.entity.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * Find device by MQTT username
     */
    Optional<Device> findByMqttUsername(String mqttUsername);

    /**
     * Find all devices by type
     */
    List<Device> findByDeviceType(DeviceType deviceType);

    /**
     * Find all devices with pagination
     */
    Page<Device> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    /**
     * Find all devices of a specific type with pagination
     */
    Page<Device> findByDeviceTypeOrderByUpdatedAtDesc(DeviceType deviceType, Pageable pageable);

    /**
     * Find offline devices (no heartbeat in the last N minutes)
     */
    @Query("SELECT d FROM Device d WHERE d.lastHeartbeat < ?1 OR d.lastHeartbeat IS NULL")
    List<Device> findOfflineDevices(Instant beforeTimestamp);

    /**
     * Count devices by status
     */
    long countByStatus(Device.DeviceStatus status);

    /**
     * Check if device exists by name
     */
    boolean existsByName(String name);
}
