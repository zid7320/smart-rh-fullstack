package com.smart.rh.repository;

import com.smart.rh.entity.SensorHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorHealthRepository extends JpaRepository<SensorHealth, Long> {
    Optional<SensorHealth> findBySensorId(Long sensorId);

    @Query("SELECT s FROM SensorHealth s WHERE s.healthTimestamp < :threshold")
    List<SensorHealth> findStaleHealthRecords(@Param("threshold") Instant threshold);
}
