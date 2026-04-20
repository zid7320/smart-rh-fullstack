package com.smart.rh.repository;

import com.smart.rh.entity.BureauSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BureauSensorRepository extends JpaRepository<BureauSensor, Long> {
    Optional<BureauSensor> findByDeviceId(String deviceId);

    List<BureauSensor> findByIsActiveTrue();

    boolean existsByDeviceId(String deviceId);
}
