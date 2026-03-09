package com.smart.rh.service;

import com.smart.rh.dto.attendance.AttendanceDto;
import com.smart.rh.dto.attendance.AttendanceRequest;
import com.smart.rh.entity.Attendance;
import com.smart.rh.entity.AttendanceType;
import com.smart.rh.entity.Employe;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.AttendanceMapper;
import com.smart.rh.repository.AttendanceRepository;
import com.smart.rh.repository.EmployeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AttendanceService} — recognition event workflow.
 * No Spring context — SimpMessagingTemplate is mocked so no WebSocket broker needed.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    private static final String ATTENDANCE_TOPIC = "/topic/attendance";

    @Mock private AttendanceRepository  repository;
    @Mock private EmployeRepository     employeRepository;
    @Mock private AttendanceMapper      mapper;
    @Mock private AuditService          auditService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private AttendanceService service;

    // ── recognize — success paths ─────────────────────────────────────────────

    @Test
    void recognize_validRequest_savesEventBroadcastsAndAudits() {
        Employe emp = makeEmploye(1L, "Martin", "Jean");
        Instant clockedAt = Instant.parse("2024-03-01T08:00:00Z");

        var request = new AttendanceRequest(1L, AttendanceType.IN, clockedAt, 0.95, "CAM_01", "SITE_HQ");

        when(employeRepository.findById(1L)).thenReturn(Optional.of(emp));

        Attendance attEntity = new Attendance();
        attEntity.setType(AttendanceType.IN);
        attEntity.setClockedAt(clockedAt);   // non-null → service preserves it
        attEntity.setConfidence(0.95);
        attEntity.setCameraId("CAM_01");
        attEntity.setSiteId("SITE_HQ");
        when(mapper.toEntity(request)).thenReturn(attEntity);

        Attendance saved = new Attendance();
        saved.setId(50L);
        saved.setEmploye(emp);
        saved.setType(AttendanceType.IN);
        saved.setClockedAt(clockedAt);
        saved.setConfidence(0.95);
        when(repository.save(attEntity)).thenReturn(saved);

        AttendanceDto dto = new AttendanceDto(50L, 1L, "Jean Martin", "IN",
                clockedAt, 0.95, "CAM_01", "SITE_HQ", Instant.now());
        when(mapper.toDto(saved)).thenReturn(dto);

        AttendanceDto result = service.recognize(request);

        // Correct DTO returned
        assertThat(result.id()).isEqualTo(50L);
        assertThat(result.type()).isEqualTo("IN");
        assertThat(result.employeId()).isEqualTo(1L);
        assertThat(result.clockedAt()).isEqualTo(clockedAt);
        assertThat(result.confidence()).isEqualTo(0.95);

        // Employee linked before save
        assertThat(attEntity.getEmploye()).isSameAs(emp);
        // clockedAt preserved (was non-null)
        assertThat(attEntity.getClockedAt()).isEqualTo(clockedAt);

        // WebSocket broadcast sent
        verify(messagingTemplate).convertAndSend(ATTENDANCE_TOPIC, dto);

        // Audit written with correct fields
        verify(auditService).log(eq("ATTENDANCE_RECOGNITION"), eq("Attendance"),
                eq(50L), anyString());
    }

    @Test
    void recognize_nullClockedAt_serviceDefaultsToCurrentTime() {
        Employe emp = makeEmploye(2L, "Dubois", "Claire");
        var request = new AttendanceRequest(2L, AttendanceType.OUT, null, 0.88, null, null);

        when(employeRepository.findById(2L)).thenReturn(Optional.of(emp));

        Attendance attEntity = new Attendance();
        attEntity.setType(AttendanceType.OUT);
        attEntity.setConfidence(0.88);
        // clockedAt intentionally null → service must set it
        when(mapper.toEntity(request)).thenReturn(attEntity);

        Attendance saved = new Attendance();
        saved.setId(51L);
        saved.setEmploye(emp);
        saved.setType(AttendanceType.OUT);
        when(repository.save(attEntity)).thenReturn(saved);

        AttendanceDto dto = new AttendanceDto(51L, 2L, "Claire Dubois", "OUT",
                Instant.now(), 0.88, null, null, Instant.now());
        when(mapper.toDto(saved)).thenReturn(dto);

        service.recognize(request);

        // Service must default clockedAt to Instant.now() when null
        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getClockedAt()).isNotNull();
    }

    @Test
    void recognize_auditSummaryIncludesTypeAndEmployeeId() {
        Employe emp = makeEmploye(3L, "Bernard", "Marc");
        var request = new AttendanceRequest(3L, AttendanceType.IN,
                Instant.now(), 0.75, "CAM_02", "SITE_B");

        when(employeRepository.findById(3L)).thenReturn(Optional.of(emp));
        Attendance att = new Attendance();
        att.setType(AttendanceType.IN);
        att.setConfidence(0.75);
        att.setClockedAt(Instant.now());
        when(mapper.toEntity(request)).thenReturn(att);

        Attendance saved = new Attendance();
        saved.setId(52L);
        saved.setEmploye(emp);
        saved.setType(AttendanceType.IN);
        saved.setConfidence(0.75);
        when(repository.save(att)).thenReturn(saved);

        AttendanceDto dto = new AttendanceDto(52L, 3L, "Marc Bernard", "IN",
                Instant.now(), 0.75, "CAM_02", "SITE_B", Instant.now());
        when(mapper.toDto(saved)).thenReturn(dto);

        service.recognize(request);

        ArgumentCaptor<String> summaryCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditService).log(eq("ATTENDANCE_RECOGNITION"), eq("Attendance"),
                eq(52L), summaryCaptor.capture());

        String summary = summaryCaptor.getValue();
        assertThat(summary).contains("IN");          // type present
        assertThat(summary).contains("3");           // employee ID present
        assertThat(summary).contains("0.75");        // confidence present
    }

    @Test
    void recognize_outEvent_isHandledIdentically() {
        Employe emp = makeEmploye(4L, "Petit", "Lucie");
        var request = new AttendanceRequest(4L, AttendanceType.OUT,
                Instant.parse("2024-03-01T17:30:00Z"), 0.99, "CAM_03", "SITE_A");

        when(employeRepository.findById(4L)).thenReturn(Optional.of(emp));
        Attendance att = new Attendance();
        att.setType(AttendanceType.OUT);
        att.setClockedAt(Instant.parse("2024-03-01T17:30:00Z"));
        when(mapper.toEntity(request)).thenReturn(att);

        Attendance saved = new Attendance();
        saved.setId(53L);
        saved.setType(AttendanceType.OUT);
        saved.setEmploye(emp);
        when(repository.save(att)).thenReturn(saved);

        AttendanceDto dto = new AttendanceDto(53L, 4L, "Lucie Petit", "OUT",
                Instant.parse("2024-03-01T17:30:00Z"), 0.99, "CAM_03", "SITE_A", Instant.now());
        when(mapper.toDto(saved)).thenReturn(dto);

        AttendanceDto result = service.recognize(request);

        assertThat(result.type()).isEqualTo("OUT");
        verify(messagingTemplate).convertAndSend(eq(ATTENDANCE_TOPIC), eq(dto));
    }

    // ── recognize — error paths ───────────────────────────────────────────────

    @Test
    void recognize_unknownEmployee_throwsResourceNotFoundWithoutSaving() {
        when(employeRepository.findById(999L)).thenReturn(Optional.empty());
        var request = new AttendanceRequest(999L, AttendanceType.IN, Instant.now(), null, null, null);

        assertThatThrownBy(() -> service.recognize(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employe");

        verify(repository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        verify(auditService, never()).log(anyString(), anyString(), any(), anyString());
    }

    // ── findByEmploye — error path ────────────────────────────────────────────

    @Test
    void findByEmploye_unknownEmployee_throwsResourceNotFound() {
        when(employeRepository.existsById(88L)).thenReturn(false);

        assertThatThrownBy(() ->
                service.findByEmploye(88L, org.springframework.data.domain.Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employe");
    }

    @Test
    void findByEmploye_knownEmployee_delegatesToRepository() {
        when(employeRepository.existsById(1L)).thenReturn(true);
        when(repository.findByEmployeIdOrderByClockedAtDesc(eq(1L), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        assertThatNoException().isThrownBy(() ->
                service.findByEmploye(1L, org.springframework.data.domain.Pageable.unpaged()));

        verify(repository).findByEmployeIdOrderByClockedAtDesc(eq(1L), any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Employe makeEmploye(Long id, String nom, String prenom) {
        Employe e = new Employe();
        e.setId(id);
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setEmail(prenom.toLowerCase() + "." + nom.toLowerCase() + "@test.com");
        return e;
    }
}
