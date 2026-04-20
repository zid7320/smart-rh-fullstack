package com.smart.rh.repository;

import com.smart.rh.entity.SensorAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorAlertRepository extends JpaRepository<SensorAlert, Long> {
    List<SensorAlert> findByIsActiveTrueOrderByTriggeredAtDesc();

    List<SensorAlert> findBySensorIdAndIsActiveTrueOrderByTriggeredAtDesc(Long sensorId);

    List<SensorAlert> findByAlertTypeAndIsActiveTrue(String alertType);
}
