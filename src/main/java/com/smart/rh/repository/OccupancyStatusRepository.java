package com.smart.rh.repository;

import com.smart.rh.entity.OccupancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OccupancyStatusRepository extends JpaRepository<OccupancyStatus, Long> {
    List<OccupancyStatus> findBySensorIdOrderByStatusTimestampDesc(Long sensorId);

    @Query("SELECT o FROM OccupancyStatus o WHERE o.sensor.id = :sensorId AND o.statusTimestamp > :since ORDER BY o.statusTimestamp DESC")
    List<OccupancyStatus> findByTimeRange(@Param("sensorId") Long sensorId, @Param("since") Instant since);

    @Query("SELECT o FROM OccupancyStatus o WHERE o.sensor.id = :sensorId ORDER BY o.statusTimestamp DESC LIMIT 1")
    Optional<OccupancyStatus> findLatestBySensor(@Param("sensorId") Long sensorId);
}
