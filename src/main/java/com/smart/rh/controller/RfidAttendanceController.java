package com.smart.rh.controller;

import com.smart.rh.dto.rfid.AttendanceRecordDto;
import com.smart.rh.dto.rfid.AttendanceVerificationDto;
import com.smart.rh.dto.rfid.RfidCardDto;
import com.smart.rh.dto.rfid.RfidReaderDto;
import com.smart.rh.entity.AttendanceRecord;
import com.smart.rh.entity.AttendanceVerification;
import com.smart.rh.entity.RfidCard;
import com.smart.rh.entity.RfidReader;
import com.smart.rh.repository.AttendanceRecordRepository;
import com.smart.rh.repository.AttendanceVerificationRepository;
import com.smart.rh.repository.RfidCardRepository;
import com.smart.rh.repository.RfidReaderRepository;
import com.smart.rh.service.RfidAttendanceService;
import com.smart.rh.service.RfidReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for RFID attendance system
 * Endpoints for reader/card management and attendance record retrieval
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class RfidAttendanceController {

    private final RfidReaderService rfidReaderService;
    private final RfidAttendanceService rfidAttendanceService;
    private final RfidReaderRepository rfidReaderRepository;
    private final RfidCardRepository rfidCardRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceVerificationRepository attendanceVerificationRepository;

    // ── RFID Reader Endpoints ────────────────────────────────────────────────

    @PostMapping("/readers")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<RfidReaderDto> registerReader(@RequestBody RfidReaderDto dto) {
        log.info("Registering RFID reader: {}", dto.getDeviceId());
        RfidReaderDto readerDto = rfidReaderService.registerReader(dto);
        return ResponseEntity.ok(readerDto);
    }

    @GetMapping("/readers")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<List<RfidReaderDto>> getAllReaders() {
        List<RfidReader> readers = rfidReaderRepository.findAll();
        return ResponseEntity.ok(readers.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/readers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<RfidReaderDto> getReaderById(@PathVariable Long id) {
        return rfidReaderRepository.findById(id)
                .map(reader -> ResponseEntity.ok(mapToDto(reader)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/readers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<RfidReaderDto> updateReader(@PathVariable Long id, @RequestBody RfidReaderDto dto) {
        return rfidReaderRepository.findById(id)
                .map(reader -> {
                    reader.setName(dto.getName());
                    reader.setLocation(dto.getLocation());
                    reader.setIsActive(dto.getIsActive());
                    RfidReader saved = rfidReaderRepository.save(reader);
                    log.info("Updated RFID reader: {}", id);
                    return ResponseEntity.ok(mapToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── RFID Card Endpoints ──────────────────────────────────────────────────

    @PostMapping("/cards")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<RfidCardDto> registerCard(@RequestBody RfidCardDto dto) {
        log.info("Registering RFID card for employee: {}", dto.getEmployeeId());
        RfidCard card = new RfidCard();
        card.setCardId(dto.getCardId());
        card.setIsActive(true);
        RfidCard saved = rfidCardRepository.save(card);
        return ResponseEntity.ok(mapToDto(saved));
    }

    @GetMapping("/cards/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<List<RfidCardDto>> getEmployeeCards(@PathVariable Long employeeId) {
        List<RfidCard> cards = rfidCardRepository.findByEmployeeId(employeeId);
        return ResponseEntity.ok(cards.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<Void> deactivateCard(@PathVariable Long id) {
        return rfidCardRepository.findById(id)
                .map(card -> {
                    card.setIsActive(false);
                    rfidCardRepository.save(card);
                    log.info("Deactivated RFID card: {}", id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Attendance Record Endpoints ──────────────────────────────────────────

    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<AttendanceRecordDto>> getAttendanceHistory(
            @RequestParam Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<AttendanceRecord> records;
        if (date != null) {
            Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfDay = date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            records = attendanceRecordRepository.findByEmployeeAndDateRange(employeeId, startOfDay, endOfDay);
        } else {
            records = attendanceRecordRepository.findByEmployeeIdOrderByEventTimestampDesc(employeeId);
        }

        return ResponseEntity.ok(records.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/records/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<AttendanceRecordDto> getRecord(@PathVariable Long id) {
        return attendanceRecordRepository.findById(id)
                .map(record -> ResponseEntity.ok(mapToDto(record)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/records/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<AttendanceVerificationDto> verifyRecord(
            @PathVariable Long id,
            @RequestBody AttendanceVerificationDto dto) {

        return attendanceRecordRepository.findById(id)
                .map(record -> {
                    AttendanceVerification verification = new AttendanceVerification();
                    verification.setAttendanceRecord(record);
                    verification.setVerifiedBy(dto.getVerifiedBy());
                    verification.setVerificationStatus(AttendanceVerification.VerificationStatus.valueOf(dto.getVerificationStatus()));
                    verification.setRejectionReason(dto.getRejectionReason());
                    AttendanceVerification saved = attendanceVerificationRepository.save(verification);
                    record.setIsVerified(true);
                    attendanceRecordRepository.save(record);
                    log.info("Verified attendance record: {}", id);
                    return ResponseEntity.ok(mapToDto(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH', 'EMPLOYEE')")
    public ResponseEntity<List<AttendanceRecordDto>> getTodayAttendance(
            @RequestParam Long employeeId) {

        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        List<AttendanceRecord> records = attendanceRecordRepository.findByEmployeeAndDateRange(
                employeeId, startOfDay, endOfDay);
        return ResponseEntity.ok(records.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    @GetMapping("/report/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RH')")
    public ResponseEntity<List<AttendanceRecordDto>> getMonthlyReport(@PathVariable Long employeeId) {
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdOrderByEventTimestampDesc(employeeId);
        return ResponseEntity.ok(records.stream().map(this::mapToDto).collect(Collectors.toList()));
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private RfidReaderDto mapToDto(RfidReader reader) {
        return RfidReaderDto.builder()
                .id(reader.getId())
                .deviceId(reader.getDeviceId())
                .name(reader.getName())
                .location(reader.getLocation())
                .isActive(reader.getIsActive())
                .build();
    }

    private RfidCardDto mapToDto(RfidCard card) {
        return RfidCardDto.builder()
                .id(card.getId())
                .cardId(card.getCardId())
                .isActive(card.getIsActive())
                .build();
    }

    private AttendanceRecordDto mapToDto(AttendanceRecord record) {
        return AttendanceRecordDto.builder()
                .id(record.getId())
                .eventType(record.getEventType().toString())
                .eventTimestamp(record.getEventTimestamp().toString())
                .location(record.getLocation())
                .isVerified(record.getIsVerified())
                .build();
    }

    private AttendanceVerificationDto mapToDto(AttendanceVerification verification) {
        return AttendanceVerificationDto.builder()
                .id(verification.getId())
                .attendanceRecordId(verification.getAttendanceRecord().getId())
                .verifiedBy(verification.getVerifiedBy())
                .verificationStatus(verification.getVerificationStatus().toString())
                .rejectionReason(verification.getRejectionReason())
                .verifiedAt(verification.getVerifiedAt() != null ? verification.getVerifiedAt().toString() : null)
                .build();
    }
}
