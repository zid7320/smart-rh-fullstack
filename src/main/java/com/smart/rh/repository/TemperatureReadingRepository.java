package com.smart.rh.repository;

import com.smart.rh.entity.TemperatureReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TemperatureReadingRepository extends JpaRepository<TemperatureReading, Long> {
    List<TemperatureReading> findBySensorIdOrderByReadingTimestampDesc(Long sensorId);

    Page<TemperatureReading> findBySensorIdOrderByReadingTimestampDesc(Long sensorId, Pageable pageable);

    @Query("SELECT t FROM TemperatureReading t WHERE t.sensor.id = :sensorId AND t.readingTimestamp > :since ORDER BY t.readingTimestamp DESC")
    List<TemperatureReading> findByTimeRange(@Param("sensorId") Long sensorId, @Param("since") Instant since);

    @Query("SELECT t FROM TemperatureReading t WHERE t.sensor.id = :sensorId ORDER BY t.readingTimestamp DESC LIMIT 1")
    Optional<TemperatureReading> findLatestBySensor(@Param("sensorId") Long sensorId);
}
