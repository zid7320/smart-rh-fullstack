package com.smart.rh.service;

import com.smart.rh.dto.conge.CongeDto;
import com.smart.rh.dto.conge.CongeRequest;
import com.smart.rh.entity.*;
import com.smart.rh.exception.BadRequestException;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.CongeMapper;
import com.smart.rh.repository.CongeRepository;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CongeService} — leave request workflow.
 * No Spring context — all collaborators are Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class CongeServiceTest {

    @Mock private CongeRepository    repository;
    @Mock private EmployeRepository  employeRepository;
    @Mock private UserRepository     userRepository;
    @Mock private AuditService       auditService;
    @Mock private CongeMapper        mapper;

    @InjectMocks private CongeService service;

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_validRequest_savesCongeWithPendingStatusAndAudits() {
        var request = new CongeRequest(5L, "ANNUAL",
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 10));

        Employe employe = makeEmploye(5L, "Martin", "Jean");
        when(employeRepository.findById(5L)).thenReturn(Optional.of(employe));

        Conge conge = new Conge();
        conge.setType("ANNUAL");
        conge.setDateDebut(LocalDate.of(2024, 3, 1));
        conge.setDateFin(LocalDate.of(2024, 3, 10));
        // CongeMapper sets statut = PENDING in its expression mapping
        conge.setStatut(CongeStatut.PENDING);
        when(mapper.toEntity(request)).thenReturn(conge);

        Conge saved = new Conge();
        saved.setId(1L);
        saved.setEmploye(employe);
        saved.setType("ANNUAL");
        saved.setStatut(CongeStatut.PENDING);
        when(repository.save(conge)).thenReturn(saved);

        CongeDto expectedDto = makeDto(1L, "PENDING");
        when(mapper.toDto(saved)).thenReturn(expectedDto);

        CongeDto result = service.create(request);

        // Verify the conge is linked to the correct employee before saving
        assertThat(conge.getEmploye()).isSameAs(employe);
        assertThat(result.statut()).isEqualTo("PENDING");

        verify(repository).save(conge);
        verify(auditService).log(eq("CREATE"), eq("Conge"), eq(1L), anyString());
    }

    @Test
    void create_dateFinBeforeDateDebut_throwsBadRequest() {
        var request = new CongeRequest(5L, "ANNUAL",
                LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 1)); // fin < debut

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("dateFin");

        verify(employeRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    void create_employeeNotFound_throwsResourceNotFound() {
        var request = new CongeRequest(99L, "SICK",
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 5));

        when(employeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employe");

        verify(repository, never()).save(any());
    }

    @Test
    void create_sameDateDebutAndDateFin_acceptedAsSingleDay() {
        LocalDate same = LocalDate.of(2024, 5, 15);
        var request = new CongeRequest(5L, "SICK", same, same);

        when(employeRepository.findById(5L)).thenReturn(Optional.of(makeEmploye(5L, "Doe", "Jane")));
        Conge conge = new Conge();
        conge.setStatut(CongeStatut.PENDING);
        when(mapper.toEntity(request)).thenReturn(conge);
        Conge saved = new Conge();
        saved.setId(2L);
        when(repository.save(conge)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(makeDto(2L, "PENDING"));

        assertThatNoException().isThrownBy(() -> service.create(request));
        verify(repository).save(any());
    }

    // ── approve ───────────────────────────────────────────────────────────────

    @Test
    void approve_fromPendingStatus_setsApprovedAndDecidedFields() {
        Conge conge = makePendingConge(10L);
        User approver = makeUser(7L);

        when(repository.findById(10L)).thenReturn(Optional.of(conge));
        when(userRepository.findById(7L)).thenReturn(Optional.of(approver));
        when(repository.save(conge)).thenReturn(conge);
        when(mapper.toDto(conge)).thenReturn(makeDto(10L, "APPROVED"));

        CongeDto result = service.approve(10L, 7L);

        assertThat(result.statut()).isEqualTo("APPROVED");

        ArgumentCaptor<Conge> captor = ArgumentCaptor.forClass(Conge.class);
        verify(repository).save(captor.capture());
        Conge captured = captor.getValue();
        assertThat(captured.getStatut()).isEqualTo(CongeStatut.APPROVED);
        assertThat(captured.getDecidedAt()).isNotNull();
        assertThat(captured.getDecidedBy()).isSameAs(approver);

        verify(auditService).log(eq("APPROVE_LEAVE"), eq("Conge"), eq(10L), anyString());
    }

    @Test
    void approve_fromPendingWithNullActor_stillApprovesWithoutDecidedBy() {
        Conge conge = makePendingConge(11L);

        when(repository.findById(11L)).thenReturn(Optional.of(conge));
        when(repository.save(conge)).thenReturn(conge);
        when(mapper.toDto(conge)).thenReturn(makeDto(11L, "APPROVED"));

        service.approve(11L, null);   // actorUserId = null

        ArgumentCaptor<Conge> captor = ArgumentCaptor.forClass(Conge.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatut()).isEqualTo(CongeStatut.APPROVED);
        assertThat(captor.getValue().getDecidedAt()).isNotNull();
        assertThat(captor.getValue().getDecidedBy()).isNull();

        verify(userRepository, never()).findById(any());
    }

    @Test
    void approve_whenAlreadyApproved_throwsBadRequest() {
        Conge conge = makeCongeWithStatut(20L, CongeStatut.APPROVED);
        when(repository.findById(20L)).thenReturn(Optional.of(conge));

        assertThatThrownBy(() -> service.approve(20L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been decided");

        verify(repository, never()).save(any());
    }

    @Test
    void approve_whenAlreadyRejected_throwsBadRequest() {
        Conge conge = makeCongeWithStatut(21L, CongeStatut.REJECTED);
        when(repository.findById(21L)).thenReturn(Optional.of(conge));

        assertThatThrownBy(() -> service.approve(21L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been decided");

        verify(repository, never()).save(any());
    }

    @Test
    void approve_leaveNotFound_throwsResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.approve(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conge");

        verify(repository, never()).save(any());
    }

    // ── reject ────────────────────────────────────────────────────────────────

    @Test
    void reject_fromPendingStatus_setsRejectedAndDecidedFields() {
        Conge conge = makePendingConge(30L);
        User rejector = makeUser(8L);

        when(repository.findById(30L)).thenReturn(Optional.of(conge));
        when(userRepository.findById(8L)).thenReturn(Optional.of(rejector));
        when(repository.save(conge)).thenReturn(conge);
        when(mapper.toDto(conge)).thenReturn(makeDto(30L, "REJECTED"));

        CongeDto result = service.reject(30L, 8L);

        assertThat(result.statut()).isEqualTo("REJECTED");

        ArgumentCaptor<Conge> captor = ArgumentCaptor.forClass(Conge.class);
        verify(repository).save(captor.capture());
        Conge captured = captor.getValue();
        assertThat(captured.getStatut()).isEqualTo(CongeStatut.REJECTED);
        assertThat(captured.getDecidedAt()).isNotNull();
        assertThat(captured.getDecidedBy()).isSameAs(rejector);

        verify(auditService).log(eq("REJECT_LEAVE"), eq("Conge"), eq(30L), anyString());
    }

    @Test
    void reject_whenNotPending_throwsBadRequest() {
        Conge conge = makeCongeWithStatut(31L, CongeStatut.APPROVED);
        when(repository.findById(31L)).thenReturn(Optional.of(conge));

        assertThatThrownBy(() -> service.reject(31L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been decided");

        verify(repository, never()).save(any());
    }

    @Test
    void reject_leaveNotFound_throwsResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reject(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conge");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_existingId_returnsDto() {
        Conge conge = makePendingConge(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(conge));
        when(mapper.toDto(conge)).thenReturn(makeDto(5L, "PENDING"));

        CongeDto dto = service.findById(5L);

        assertThat(dto.id()).isEqualTo(5L);
    }

    @Test
    void findById_missingId_throwsResourceNotFound() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Conge");
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

    private User makeUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.ROLE_RH);
        return u;
    }

    private Conge makePendingConge(Long id) {
        Conge c = new Conge();
        c.setId(id);
        c.setStatut(CongeStatut.PENDING);
        c.setType("ANNUAL");
        c.setDateDebut(LocalDate.of(2024, 6, 1));
        c.setDateFin(LocalDate.of(2024, 6, 10));
        return c;
    }

    private Conge makeCongeWithStatut(Long id, CongeStatut statut) {
        Conge c = makePendingConge(id);
        c.setStatut(statut);
        return c;
    }

    private CongeDto makeDto(Long id, String statut) {
        return new CongeDto(id, 5L, "Jean Martin", "ANNUAL",
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 10),
                statut, Instant.now(), null, null, Instant.now(), null);
    }
}
