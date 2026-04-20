package com.smart.rh.repository;

import com.smart.rh.entity.Co2Reading;
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
public interface Co2ReadingRepository extends JpaRepository<Co2Reading, Long> {
    List<Co2Reading> findBySensorIdOrderByReadingTimestampDesc(Long sensorId);

    Page<Co2Reading> findBySensorIdOrderByReadingTimestampDesc(Long sensorId, Pageable pageable);

    @Query("SELECT c FROM Co2Reading c WHERE c.sensor.id = :sensorId AND c.readingTimestamp > :since ORDER BY c.readingTimestamp DESC")
    List<Co2Reading> findByTimeRange(@Param("sensorId") Long sensorId, @Param("since") Instant since);

    @Query("SELECT c FROM Co2Reading c WHERE c.sensor.id = :sensorId ORDER BY c.readingTimestamp DESC LIMIT 1")
    Optional<Co2Reading> findLatestBySensor(@Param("sensorId") Long sensorId);

    List<Co2Reading> findByAlarmTriggeredTrue();
}
