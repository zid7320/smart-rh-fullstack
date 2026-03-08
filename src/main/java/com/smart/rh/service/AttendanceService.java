package com.smart.rh.service;

import com.smart.rh.dto.attendance.AttendanceDto;
import com.smart.rh.dto.attendance.AttendanceRequest;
import com.smart.rh.entity.Attendance;
import com.smart.rh.entity.Employe;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.AttendanceMapper;
import com.smart.rh.repository.AttendanceRepository;
import com.smart.rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    /** WebSocket topic subscribed to by Angular for real-time updates. */
    private static final String ATTENDANCE_TOPIC = "/topic/attendance";

    private final AttendanceRepository  repository;
    private final EmployeRepository     employeRepository;
    private final AttendanceMapper      mapper;
    private final AuditService          auditService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Recognition ──────────────────────────────────────────────────────────

    /**
     * Persist a recognition event, broadcast it to WebSocket subscribers,
     * and write an audit entry.
     *
     * <p>{@code rawImagePath} is never included in the DTO broadcast — it is an
     * internal server-side storage path and must not be sent to Angular clients.</p>
     */
    @Transactional
    public AttendanceDto recognize(AttendanceRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employe", "id", request.employeId()));

        Attendance att = mapper.toEntity(request);
        att.setEmploye(employe);

        // Default clockedAt to now when the IoT device did not provide one
        if (att.getClockedAt() == null) {
            att.setClockedAt(Instant.now());
        }

        Attendance saved = repository.save(att);
        AttendanceDto dto = mapper.toDto(saved);

        // Real-time broadcast  — Angular receives this payload on /topic/attendance
        messagingTemplate.convertAndSend(ATTENDANCE_TOPIC, dto);

        auditService.log("ATTENDANCE_RECOGNITION", "Attendance", saved.getId(),
                "Type=" + saved.getType()
                        + " employe=#" + employe.getId()
                        + (saved.getConfidence() != null
                                ? " confidence=" + saved.getConfidence() : ""));

        return dto;
    }

    // ── History ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AttendanceDto> findAll(Pageable pageable) {
        return repository.findAllByOrderByClockedAtDesc(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto> findByEmploye(Long employeId, Pageable pageable) {
        if (!employeRepository.existsById(employeId)) {
            throw new ResourceNotFoundException("Employe", "id", employeId);
        }
        return repository.findByEmployeIdOrderByClockedAtDesc(employeId, pageable)
                .map(mapper::toDto);
    }
}
