package com.smart.rh.service;

import com.smart.rh.entity.AttendanceEvent;
import com.smart.rh.entity.Device;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Alert;
import com.smart.rh.repository.AttendanceEventRepository;
import com.smart.rh.repository.DeviceRepository;
import com.smart.rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing attendance events from facial recognition system.
 * Processes, validates, and broadcasts real-time attendance updates.
 * <p>
 * Integrated with:
 * - Facial recognition AI (receives events via MQTT/REST)
 * - WebSocket broadcast (real-time dashboard updates)
 * - Audit system (tracks manual verification)
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceEventService {

    private static final String ATTENDANCE_TOPIC = "/topic/attendance";
    private static final BigDecimal FRAUD_THRESHOLD = BigDecimal.valueOf(50); // <50% confidence = suspicious

    private final AttendanceEventRepository attendanceEventRepository;
    private final EmployeRepository employeRepository;
    private final DeviceRepository deviceRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AlertingService alertingService;

    /**
     * Record a new attendance event from facial recognition system.
     * Called by the AI pipeline when a face is recognized.
     */
    @Transactional
    public AttendanceEvent recordAttendanceEvent(
            Long employeeId,
            Long deviceId,
            BigDecimal confidenceScore,
            String photoData,
            String faceEmbedding,
            String fraudReasonIfAny) {

        try {
            // Find employee and device
            Optional<Employe> employeeOpt = employeRepository.findById(employeeId);
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);

            if (employeeOpt.isEmpty()) {
                log.warn("Attendance event: employee not found (id={})", employeeId);
                return null;
            }

            if (deviceOpt.isEmpty()) {
                log.warn("Attendance event: device not found (id={})", deviceId);
                return null;
            }

            Employe employee = employeeOpt.get();
            Device device = deviceOpt.get();

            // Determine event type (IN or OUT) based on last event today
            AttendanceEvent lastEvent = attendanceEventRepository.findLastEventToday(employeeId);
            AttendanceEvent.EventType eventType = (lastEvent == null
                    || lastEvent.getEventType() == AttendanceEvent.EventType.OUT)
                            ? AttendanceEvent.EventType.IN
                            : AttendanceEvent.EventType.OUT;

            // Check if fraud suspected
            boolean isFraud = confidenceScore.compareTo(FRAUD_THRESHOLD) < 0 || fraudReasonIfAny != null;

            // Create and persist event
            AttendanceEvent event = AttendanceEvent.builder()
                    .employee(employee)
                    .device(device)
                    .eventType(isFraud ? AttendanceEvent.EventType.SUSPICIOUS : eventType)
                    .eventTimestamp(Instant.now())
                    .confidenceScore(confidenceScore)
                    .faceEmbedding(faceEmbedding)
                    .photoData(photoData)
                    .isFraudSuspected(isFraud)
                    .fraudReason(fraudReasonIfAny)
                    .processingStatus(AttendanceEvent.ProcessingStatus.PROCESSED)
                    .processingSystem("opencv-facial-recognition")
                    .processingTimeMs(System.currentTimeMillis())
                    .build();

            AttendanceEvent saved = attendanceEventRepository.save(event);

            // Broadcast to WebSocket
            broadcastAttendanceEvent(saved);

            // Create alert if fraud suspected
            if (isFraud) {
                alertingService.createAlert(
                        device,
                        "SUSPICIOUS_ATTENDANCE",
                        "Suspicious attendance event: "
                                + (fraudReasonIfAny != null ? fraudReasonIfAny : "low confidence"),
                        Alert.AlertSeverity.CRITICAL);
            }

            log.info("Attendance event recorded: employee={} device={} type={} confidence={} fraud={}",
                    employee.getId(), device.getId(), eventType, confidenceScore, isFraud);

            return saved;

        } catch (Exception e) {
            log.error("Error recording attendance event: employeeId={} deviceId={}", employeeId, deviceId, e);
            return null;
        }
    }

    /**
     * Get recent attendance events for dashboard
     */
    @Transactional(readOnly = true)
    public Page<AttendanceEvent> getRecentEvents(Pageable pageable) {
        return attendanceEventRepository.findRecentEvents(pageable);
    }

    /**
     * Get attendance history for an employee
     */
    @Transactional(readOnly = true)
    public Page<AttendanceEvent> getEmployeeAttendanceHistory(Long employeeId, Pageable pageable) {
        return attendanceEventRepository.findByEmployeeId(employeeId, pageable);
    }

    /**
     * Get today's attendance for an employee
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getTodayAttendance(Long employeeId) {
        return attendanceEventRepository.findTodayByEmployeeId(employeeId);
    }

    /**
     * Get current status for an employee (clocked in or out)
     */
    @Transactional(readOnly = true)
    public String getCurrentStatus(Long employeeId) {
        AttendanceEvent lastEvent = attendanceEventRepository.findLastEventToday(employeeId);

        if (lastEvent == null) {
            return "NOT_CHECKED_IN";
        }

        return switch (lastEvent.getEventType()) {
            case IN -> "CHECKED_IN";
            case OUT -> "CHECKED_OUT";
            case SUSPICIOUS -> "SUSPICIOUS_EVENT";
        };
    }

    /**
     * Get unverified fraud alerts
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getUnverifiedFraudAlerts() {
        return attendanceEventRepository.findUnverifiedFraudAlerts();
    }

    /**
     * Get fraud/suspicious events for investigation
     */
    @Transactional(readOnly = true)
    public Page<AttendanceEvent> getSuspiciousEvents(Pageable pageable) {
        return attendanceEventRepository.findSuspiciousEvents(pageable);
    }

    /**
     * Manually verify an attendance event (HR/Security approval)
     */
    @Transactional
    public void verifyAttendanceEvent(Long eventId, Boolean isValid, String notes, Long verifiedByUserId) {
        Optional<AttendanceEvent> eventOpt = attendanceEventRepository.findById(eventId);

        if (eventOpt.isPresent()) {
            AttendanceEvent event = eventOpt.get();
            event.setManuallyVerified(isValid);
            event.setVerifiedAt(Instant.now());
            event.setVerificationNotes(notes);

            attendanceEventRepository.save(event);

            log.info("Attendance event verified: eventId={} isValid={} verifiedBy={}",
                    eventId, isValid, verifiedByUserId);
        }
    }

    /**
     * Get today's attendance summary for dashboard
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getTodayAttendanceSummary() {
        List<Object[]> results = attendanceEventRepository.countEventsByTypeToday();

        long checkIns = 0;
        long checkOuts = 0;
        long suspicious = 0;

        for (Object[] row : results) {
            AttendanceEvent.EventType type = (AttendanceEvent.EventType) row[0];
            Long count = ((Number) row[1]).longValue();

            switch (type) {
                case IN -> checkIns = count;
                case OUT -> checkOuts = count;
                case SUSPICIOUS -> suspicious = count;
            }
        }

        return Map.of(
                "checkIns", checkIns,
                "checkOuts", checkOuts,
                "suspicious", suspicious,
                "present", Math.min(checkIns, checkIns - checkOuts) // Currently on-site
        );
    }

    /**
     * Get attendance events for a specific device (camera)
     */
    @Transactional(readOnly = true)
    public Page<AttendanceEvent> getDeviceAttendanceHistory(Long deviceId, Pageable pageable) {
        return attendanceEventRepository.findByDeviceId(deviceId, pageable);
    }

    /**
     * Get attendance events within a date range
     */
    @Transactional(readOnly = true)
    public List<AttendanceEvent> getAttendanceByDateRange(Long employeeId, Instant startDate, Instant endDate) {
        return attendanceEventRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate);
    }

    /**
     * Clean up old attendance records (data retention policy)
     */
    @Transactional
    public int cleanupOldAttendanceEvents(Instant beforeDate) {
        List<AttendanceEvent> oldEvents = attendanceEventRepository.findAll()
                .stream()
                .filter(e -> e.getEventTimestamp().isBefore(beforeDate))
                .toList();

        int count = oldEvents.size();
        attendanceEventRepository.deleteAll(oldEvents);

        log.info("Cleaned up {} old attendance events before {}", count, beforeDate);
        return count;
    }

    /**
     * Broadcast attendance event to WebSocket subscribers
     */
    private void broadcastAttendanceEvent(AttendanceEvent event) {
        try {
            messagingTemplate.convertAndSend(ATTENDANCE_TOPIC, Map.of(
                    "eventId", event.getId(),
                    "employeeId", event.getEmployee().getId(),
                    "employeeName", event.getEmployee().getPrenom() + " " + event.getEmployee().getNom(),
                    "deviceId", event.getDevice().getId(),
                    "deviceName", event.getDevice().getName(),
                    "eventType", event.getEventType().toString(),
                    "timestamp", event.getEventTimestamp().toString(),
                    "confidence", event.getConfidenceScore().toString(),
                    "isFraud", event.getIsFraudSuspected(),
                    "fraudReason", event.getFraudReason()));
        } catch (Exception e) {
            log.warn("Failed to broadcast attendance event via WebSocket", e);
        }
    }
}
