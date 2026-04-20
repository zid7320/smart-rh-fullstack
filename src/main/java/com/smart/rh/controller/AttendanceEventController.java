package com.smart.rh.controller;

import com.smart.rh.dto.attendance.AttendanceEventDto;
import com.smart.rh.entity.AttendanceEvent;
import com.smart.rh.mapper.AttendanceMapper;
import com.smart.rh.service.AttendanceEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for facial recognition attendance events.
 * Provides real-time access to attendance data for dashboard and reporting.
 */
@Tag(name = "Attendance", description = "Facial recognition attendance events and dashboard")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RH','MANAGER')")
public class AttendanceEventController {

        private final AttendanceEventService attendanceEventService;
        private final AttendanceMapper attendanceMapper;

        /**
         * Get recent attendance events for dashboard (real-time feed)
         */
        @Operation(summary = "Get recent attendance events", description = "Latest facial recognition events for dashboard")
        @GetMapping("/recent")
        public Page<AttendanceEventDto> getRecentEvents(
                        @PageableDefault(size = 50) Pageable pageable) {
                return attendanceEventService.getRecentEvents(pageable)
                                .map(attendanceMapper::toAttendanceEventDto);
        }

        /**
         * Get attendance events for a specific employee
         */
        @Operation(summary = "Get attendance history for employee")
        @GetMapping("/employee/{employeeId}")
        public Page<AttendanceEventDto> getEmployeeAttendance(
                        @PathVariable Long employeeId,
                        @PageableDefault(size = 30) Pageable pageable) {
                return attendanceEventService.getEmployeeAttendanceHistory(employeeId, pageable)
                                .map(attendanceMapper::toAttendanceEventDto);
        }

        /**
         * Get today's attendance for an employee
         */
        @Operation(summary = "Get today's attendance for employee")
        @GetMapping("/employee/{employeeId}/today")
        public ResponseEntity<List<AttendanceEventDto>> getTodayAttendance(
                        @PathVariable Long employeeId) {
                List<AttendanceEvent> events = attendanceEventService.getTodayAttendance(employeeId);
                return ResponseEntity.ok(
                                events.stream()
                                                .map(attendanceMapper::toAttendanceEventDto)
                                                .toList());
        }

        /**
         * Get current status for an employee (clocked in/out)
         */
        @Operation(summary = "Get current check-in status for employee")
        @GetMapping("/employee/{employeeId}/current-status")
        public ResponseEntity<Map<String, String>> getCurrentStatus(
                        @PathVariable Long employeeId) {
                String status = attendanceEventService.getCurrentStatus(employeeId);
                return ResponseEntity.ok(Map.of("status", status));
        }

        /**
         * Get unverified fraud alerts (for HR to investigate)
         */
        @Operation(summary = "Get unverified fraud alerts", description = "Suspicious attendance events pending manual verification")
        @GetMapping("/fraud/unverified")
        @PreAuthorize("hasAnyRole('ADMIN','RH')")
        public ResponseEntity<List<AttendanceEventDto>> getUnverifiedFraudAlerts() {
                List<AttendanceEvent> alerts = attendanceEventService.getUnverifiedFraudAlerts();
                return ResponseEntity.ok(
                                alerts.stream()
                                                .map(attendanceMapper::toAttendanceEventDto)
                                                .toList());
        }

        /**
         * Get suspicious attendance events
         */
        @Operation(summary = "Get suspicious attendance events")
        @GetMapping("/suspicious")
        @PreAuthorize("hasAnyRole('ADMIN','RH')")
        public Page<AttendanceEventDto> getSuspiciousEvents(
                        @PageableDefault(size = 30) Pageable pageable) {
                return attendanceEventService.getSuspiciousEvents(pageable)
                                .map(attendanceMapper::toAttendanceEventDto);
        }

        /**
         * Verify an attendance event (HR confirmation of fraud/valid)
         */
        @Operation(summary = "Verify suspicious attendance event", description = "HR confirms if event is valid or fraud")
        @PostMapping("/{eventId}/verify")
        @PreAuthorize("hasAnyRole('ADMIN','RH')")
        public ResponseEntity<Void> verifyAttendanceEvent(
                        @PathVariable Long eventId,
                        @RequestBody VerifyAttendanceRequest request) {
                attendanceEventService.verifyAttendanceEvent(
                                eventId,
                                request.valid(),
                                request.notes(),
                                null // TODO: Get from JWT token
                );
                return ResponseEntity.noContent().build();
        }

        /**
         * Get today's attendance summary (dashboard widget)
         */
        @Operation(summary = "Get today's attendance summary", description = "Quick stats for dashboard: check-ins, check-outs, suspicious events")
        @GetMapping("/summary/today")
        public ResponseEntity<Map<String, Long>> getTodayAttendanceSummary() {
                Map<String, Long> summary = attendanceEventService.getTodayAttendanceSummary();
                return ResponseEntity.ok(summary);
        }

        /**
         * Get attendance events from a specific camera/device
         */
        @Operation(summary = "Get attendance events from a camera")
        @GetMapping("/device/{deviceId}")
        public Page<AttendanceEventDto> getDeviceAttendance(
                        @PathVariable Long deviceId,
                        @PageableDefault(size = 30) Pageable pageable) {
                return attendanceEventService.getDeviceAttendanceHistory(deviceId, pageable)
                                .map(attendanceMapper::toAttendanceEventDto);
        }

        /**
         * Check if an employee is currently on-site (check-in status)
         */
        @Operation(summary = "Check if employee is currently on-site")
        @GetMapping("/employee/{employeeId}/is-present")
        public ResponseEntity<Map<String, Boolean>> isEmployeePresent(
                        @PathVariable Long employeeId) {
                String status = attendanceEventService.getCurrentStatus(employeeId);
                boolean isPresent = "CHECKED_IN".equals(status);
                return ResponseEntity.ok(Map.of("isPresent", isPresent));
        }

        // ── DTOs ──────────────────────────────────────────────────────────────────

        public record VerifyAttendanceRequest(
                        boolean valid,
                        String notes) {
        }
}
