package com.smart.rh.controller;

import com.smart.rh.dto.attendance.AttendanceDto;
import com.smart.rh.dto.attendance.AttendanceRequest;
import com.smart.rh.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Attendance recognition and history endpoints.
 *
 * <h3>Access control</h3>
 * <ul>
 *   <li>POST /recognition — ADMIN or RH only (triggered by the AI/IoT pipeline)</li>
 *   <li>GET  /history     — ADMIN or RH only (full dataset)</li>
 *   <li>GET  /byEmployee/{id} — ADMIN or RH see any employee;
 *       an EMPLOYEE can only see their own records
 *       (enforced via {@code @attendanceSecurity.isSelf})</li>
 * </ul>
 */
@Tag(name = "Attendance", description = "Attendance recognition events (AI/IoT) and history")
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService service;

    // ── Recognition ──────────────────────────────────────────────────────────

    @Operation(summary = "Submit a face-recognition attendance event",
               description = "Saves the record and broadcasts it to WebSocket topic /topic/attendance")
    @PostMapping("/recognition")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<AttendanceDto> recognize(
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.recognize(request));
    }

    // ── History ───────────────────────────────────────────────────────────────

    @Operation(summary = "Global attendance history (ADMIN / RH only), newest first")
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<AttendanceDto> getHistory(
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Attendance by employee — self-service access for EMPLOYEE role")
    @GetMapping("/byEmployee/{employeId}")
    @PreAuthorize("hasAnyRole('ADMIN','RH') or @attendanceSecurity.isSelf(authentication, #employeId)")
    public Page<AttendanceDto> getByEmployee(
            @PathVariable Long employeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByEmploye(employeId, pageable);
    }
}
