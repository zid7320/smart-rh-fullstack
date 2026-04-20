package com.smart.rh.repository;

import com.smart.rh.entity.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /**
     * Find all readings for a device with pagination
     */
    Page<SensorReading> findByDeviceIdOrderByTimestampDesc(Long deviceId, Pageable pageable);

    /**
     * Find readings for a device by reading type
     */
    List<SensorReading> findByDeviceIdAndReadingTypeOrderByTimestampDesc(Long deviceId, String readingType);

    /**
     * Find readings for a device within a time range
     */
    @Query("SELECT sr FROM SensorReading sr WHERE sr.device.id = ?1 AND sr.timestamp BETWEEN ?2 AND ?3 ORDER BY sr.timestamp DESC")
    List<SensorReading> findByDeviceAndTimeRange(Long deviceId, Instant startTime, Instant endTime);

    /**
     * Find latest reading for each device
     */
    @Query(value = "SELECT * FROM sensor_reading sr1 WHERE sr1.timestamp = (SELECT MAX(sr2.timestamp) FROM sensor_reading sr2 WHERE sr2.device_id = sr1.device_id) ORDER BY sr1.timestamp DESC", nativeQuery = true)
    List<SensorReading> findLatestReadingPerDevice();

    /**
     * Find readings with processing status
     */
    List<SensorReading> findByProcessingStatus(SensorReading.ProcessingStatus status);

    /**
     * Count readings by device
     */
    long countByDeviceId(Long deviceId);

    /**
     * Delete old readings (for data retention policy)
     */
    @Modifying
    @Query("DELETE FROM SensorReading sr WHERE sr.timestamp < ?1")
    void deleteOlderThan(Instant timestamp);
}
