package com.smart.rh.service;

import com.smart.rh.dto.rfid.RfidReaderDto;
import com.smart.rh.entity.RfidReader;
import com.smart.rh.repository.RfidReaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing RFID reader devices
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RfidReaderService {

    private final RfidReaderRepository rfidReaderRepository;

    /**
     * Register a new RFID reader
     */
    @Transactional
    public RfidReaderDto registerReader(RfidReaderDto dto) {
        log.info("Registering RFID reader: deviceId={}, name={}", dto.getDeviceId(), dto.getName());

        // Check if device already exists
        if (rfidReaderRepository.existsByDeviceId(dto.getDeviceId())) {
            throw new RuntimeException("Device already registered: " + dto.getDeviceId());
        }

        RfidReader reader = new RfidReader();
        reader.setDeviceId(dto.getDeviceId());
        reader.setName(dto.getName());
        reader.setLocation(dto.getLocation());
        reader.setIsActive(true);

        // Generate MQTT credentials
        reader.setMqttUsername("rfid_" + reader.getDeviceId());
        reader.setMqttPassword(UUID.randomUUID().toString());

        RfidReader saved = rfidReaderRepository.save(reader);
        log.info("RFID reader registered: id={}, deviceId={}", saved.getId(), saved.getDeviceId());

        return mapToDto(saved);
    }

    /**
     * Get all active readers
     */
    @Transactional(readOnly = true)
    public List<RfidReaderDto> getAllActiveReaders() {
        return rfidReaderRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get reader by device ID
     */
    @Transactional(readOnly = true)
    public Optional<RfidReaderDto> getReaderByDeviceId(String deviceId) {
        return rfidReaderRepository.findByDeviceId(deviceId).map(this::mapToDto);
    }

    /**
     * Update reader status
     */
    @Transactional
    public RfidReaderDto updateReaderStatus(Long readerId, Boolean isActive) {
        Optional<RfidReader> readerOpt = rfidReaderRepository.findById(readerId);
        if (readerOpt.isEmpty()) {
            throw new RuntimeException("Reader not found: " + readerId);
        }

        RfidReader reader = readerOpt.get();
        reader.setIsActive(isActive);
        RfidReader updated = rfidReaderRepository.save(reader);
        log.info("Reader status updated: id={}, isActive={}", readerId, isActive);

        return mapToDto(updated);
    }

    /**
     * Map entity to DTO
     */
    private RfidReaderDto mapToDto(RfidReader reader) {
        RfidReaderDto dto = new RfidReaderDto();
        dto.setId(reader.getId());
        dto.setDeviceId(reader.getDeviceId());
        dto.setName(reader.getName());
        dto.setLocation(reader.getLocation());
        dto.setIsActive(reader.getIsActive());
        return dto;
    }
}
