package com.smart.rh.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.rh.dto.rfid.AttendanceRecordDto;
import com.smart.rh.dto.rfid.RfidSwipeEventDto;
import com.smart.rh.entity.*;
import com.smart.rh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service for processing RFID attendance swipes.
 * Saves data to both AttendanceRecord (RFID system) and Attendance (unified frontend display).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RfidAttendanceService {

    private static final String ATTENDANCE_TOPIC = "/topic/attendance";
    private static final int DUPLICATE_WINDOW_SECONDS = 30;

    private final RfidCardRepository rfidCardRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceVerificationRepository attendanceVerificationRepository;
    private final RfidReaderRepository rfidReaderRepository;
    private final EmployeRepository employeRepository;
    private final AttendanceRepository attendanceRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Process an RFID card swipe event
     */
    @Transactional
    public AttendanceRecordDto processRfidSwipe(RfidSwipeEventDto event) {
        try {
            log.info("Processing RFID swipe: cardId={}, device={}", event.getCardId(), event.getDeviceId());

            // Find the RFID card
            Optional<RfidCard> cardOpt = rfidCardRepository.findByCardId(event.getCardId());
            if (cardOpt.isEmpty()) {
                log.warn("Unknown RFID card: {}", event.getCardId());
                throw new RuntimeException("Card not registered");
            }

            RfidCard card = cardOpt.get();
            if (!card.getIsActive()) {
                log.warn("RFID card is inactive: {}", event.getCardId());
                throw new RuntimeException("Card is inactive");
            }

            Employe employee = card.getEmployee();

            // Check for duplicate swipe (< 30 seconds)
            if (isDuplicateSwipe(employee.getId())) {
                log.info("Duplicate swipe detected for employee: {}", employee.getId());
                throw new RuntimeException("Duplicate swipe within 30 seconds");
            }

            // Find reader if specified
            RfidReader reader = null;
            if (event.getRfidReaderId() != null) {
                reader = rfidReaderRepository.findById(event.getRfidReaderId()).orElse(null);
            }

            // Determine event type (IN or OUT)
            AttendanceRecord.EventType eventType = determineEventType(employee.getId());

            // Create attendance record (RFID system)
            AttendanceRecord record = new AttendanceRecord();
            record.setEmployee(employee);
            record.setRfidReader(reader);
            record.setRfidCardId(event.getCardId());
            record.setEventType(eventType);
            record.setEventTimestamp(Instant.now());
            record.setLocation(event.getLocation());
            record.setIsVerified(false);

            AttendanceRecord saved = attendanceRecordRepository.save(record);
            log.info("Attendance record created: id={}, employee={}, type={}", saved.getId(), employee.getId(),
                    eventType);

            // Also save to unified Attendance table for frontend display
            Attendance att = new Attendance();
            att.setEmploye(employee);
            att.setType(AttendanceType.valueOf(eventType.toString()));
            att.setClockedAt(record.getEventTimestamp());
            att.setConfidence(99.0); // RFID-based attendance has high confidence
            att.setCameraId("RFID");
            att.setSiteId(record.getLocation());
            attendanceRepository.save(att);
            log.debug("Attendance record also saved to unified table for frontend");

            // Broadcast via WebSocket
            broadcastAttendance(saved, employee);

            // Convert to DTO
            return mapToDto(saved, employee);

        } catch (Exception e) {
            log.error("Error processing RFID swipe", e);
            throw new RuntimeException("Failed to process swipe: " + e.getMessage());
        }
    }

    /**
     * Check if this is a duplicate swipe (< 30 seconds)
     */
    private boolean isDuplicateSwipe(Long employeeId) {
        Instant recentTime = Instant.now().minus(DUPLICATE_WINDOW_SECONDS, ChronoUnit.SECONDS);
        List<AttendanceRecord> recentSwipes = attendanceRecordRepository.findRecentByEmployee(employeeId, recentTime);
        return !recentSwipes.isEmpty();
    }

    /**
     * Determine if this swipe is IN or OUT based on last event
     */
    private AttendanceRecord.EventType determineEventType(Long employeeId) {
        List<AttendanceRecord> recentRecords = attendanceRecordRepository
                .findByEmployeeIdOrderByEventTimestampDesc(employeeId);

        if (recentRecords.isEmpty()) {
            return AttendanceRecord.EventType.IN;
        }

        AttendanceRecord.EventType lastType = recentRecords.get(0).getEventType();
        return lastType == AttendanceRecord.EventType.IN ? AttendanceRecord.EventType.OUT
                : AttendanceRecord.EventType.IN;
    }

    /**
     * Broadcast attendance event via WebSocket
     */
    private void broadcastAttendance(AttendanceRecord record, Employe employee) {
        try {
            AttendanceRecordDto dto = mapToDto(record, employee);
            messagingTemplate.convertAndSend(ATTENDANCE_TOPIC, dto);
            log.debug("Attendance broadcast sent: {}", record.getId());
        } catch (Exception e) {
            log.warn("Failed to broadcast attendance via WebSocket", e);
        }
    }

    /**
     * Map AttendanceRecord to DTO
     */
    private AttendanceRecordDto mapToDto(AttendanceRecord record, Employe employee) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(record.getId());
        dto.setEmployeeId(employee.getId());
        dto.setEmployeeName(employee.getPrenom() + " " + employee.getNom());
        dto.setEventType(record.getEventType().toString());
        dto.setEventTimestamp(record.getEventTimestamp().toString());
        dto.setLocation(record.getLocation());
        dto.setIsVerified(record.getIsVerified());
        dto.setCreatedAt(record.getCreatedAt().toString());
        return dto;
    }

    /**
     * Get attendance history for employee
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getEmployeeAttendanceHistory(Long employeeId) {
        return attendanceRecordRepository.findByEmployeeIdOrderByEventTimestampDesc(employeeId);
    }

    /**
     * Get latest attendance status for employee
     */
    @Transactional(readOnly = true)
    public Optional<AttendanceRecord> getLatestAttendance(Long employeeId) {
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdOrderByEventTimestampDesc(employeeId);
        return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
    }
}
