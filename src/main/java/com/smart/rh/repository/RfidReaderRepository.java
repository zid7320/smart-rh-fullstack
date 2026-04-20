package com.smart.rh.repository;

import com.smart.rh.entity.RfidReader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfidReaderRepository extends JpaRepository<RfidReader, Long> {
    Optional<RfidReader> findByDeviceId(String deviceId);

    List<RfidReader> findByIsActiveTrue();

    boolean existsByDeviceId(String deviceId);
}
