package com.smart.rh.service;

import com.smart.rh.dto.employe.EmployeDto;
import com.smart.rh.dto.recrutement.HireRequest;
import com.smart.rh.entity.*;
import com.smart.rh.exception.BadRequestException;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.CandidateMapper;
import com.smart.rh.mapper.EmployeMapper;
import com.smart.rh.mapper.RecrutementMapper;
import com.smart.rh.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RecrutementService} — focuses on the hire() workflow.
 * No Spring context — all collaborators are Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class RecrutementServiceTest {

    @Mock private RecrutementRepository   recrutementRepository;
    @Mock private CandidateRepository     candidateRepository;
    @Mock private EmployeRepository       employeRepository;
    @Mock private DossierRHRepository     dossierRHRepository;
    @Mock private PosteRepository         posteRepository;
    @Mock private ResponsableRHRepository responsableRHRepository;
    @Mock private AuditService            auditService;
    @Mock private RecrutementMapper       recrutementMapper;
    @Mock private CandidateMapper         candidateMapper;
    @Mock private EmployeMapper           employeMapper;

    @InjectMocks private RecrutementService service;

    // ── hire — success path ──────────────────────────────────────────────────

    @Test
    void hire_successPath_createsEmployeeAndDossierAndUpdatesRecruitmentStatus() {
        // Given
        Recrutement rec = makeRecrutement(1L, "Développeur Java", "OUVERT");
        Candidate   cand = makeCandidate(10L, "Martin", "Jean", "jean.martin@test.com", rec);
        HireRequest req  = new HireRequest(10L, null, null); // use candidate's email

        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(candidateRepository.findById(10L)).thenReturn(Optional.of(cand));
        when(employeRepository.existsByEmail("jean.martin@test.com")).thenReturn(false);

        Employe savedEmploye = new Employe();
        savedEmploye.setId(100L);
        savedEmploye.setNom("Martin");
        savedEmploye.setPrenom("Jean");
        savedEmploye.setEmail("jean.martin@test.com");
        when(employeRepository.save(any(Employe.class))).thenReturn(savedEmploye);
        when(dossierRHRepository.save(any(DossierRH.class))).thenReturn(new DossierRH());
        when(recrutementRepository.save(any(Recrutement.class))).thenReturn(rec);

        EmployeDto expectedDto = makeEmployeDto(100L, "Martin", "Jean", "jean.martin@test.com");
        when(employeMapper.toDto(savedEmploye)).thenReturn(expectedDto);

        // When
        EmployeDto result = service.hire(1L, req);

        // Then — returned DTO
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.nom()).isEqualTo("Martin");
        assertThat(result.prenom()).isEqualTo("Jean");
        assertThat(result.email()).isEqualTo("jean.martin@test.com");

        // Then — Employee entity fields
        ArgumentCaptor<Employe> employCap = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository).save(employCap.capture());
        Employe capturedEmploye = employCap.getValue();
        assertThat(capturedEmploye.getNom()).isEqualTo("Martin");
        assertThat(capturedEmploye.getPrenom()).isEqualTo("Jean");
        assertThat(capturedEmploye.getEmail()).isEqualTo("jean.martin@test.com");
        assertThat(capturedEmploye.getPosteLibelle()).isEqualTo("Développeur Java"); // from recruitment

        // Then — DossierRH created and linked
        ArgumentCaptor<DossierRH> dossierCap = ArgumentCaptor.forClass(DossierRH.class);
        verify(dossierRHRepository).save(dossierCap.capture());
        assertThat(dossierCap.getValue().getEmploye()).isSameAs(savedEmploye);

        // Then — recruitment status updated to EMBAUCHE
        ArgumentCaptor<Recrutement> recCap = ArgumentCaptor.forClass(Recrutement.class);
        verify(recrutementRepository).save(recCap.capture());
        assertThat(recCap.getValue().getStatut()).isEqualTo("EMBAUCHE");

        // Then — audit logged with action HIRE
        verify(auditService).log(eq("HIRE"), eq("Employe"), eq(100L), anyString());
    }

    @Test
    void hire_withEmailOverride_usesOverrideEmailInsteadOfCandidateEmail() {
        Recrutement rec = makeRecrutement(1L, "DevOps Engineer", "OUVERT");
        Candidate cand = makeCandidate(10L, "Dupont", "Marie", "marie.original@test.com", rec);
        HireRequest req = new HireRequest(10L, null, "marie.override@company.com");

        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(candidateRepository.findById(10L)).thenReturn(Optional.of(cand));
        when(employeRepository.existsByEmail("marie.override@company.com")).thenReturn(false);

        Employe saved = new Employe();
        saved.setId(200L);
        saved.setEmail("marie.override@company.com");
        when(employeRepository.save(any(Employe.class))).thenReturn(saved);
        when(dossierRHRepository.save(any())).thenReturn(new DossierRH());
        when(recrutementRepository.save(any())).thenReturn(rec);
        when(employeMapper.toDto(saved)).thenReturn(
                makeEmployeDto(200L, "Dupont", "Marie", "marie.override@company.com"));

        service.hire(1L, req);

        ArgumentCaptor<Employe> cap = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository).save(cap.capture());
        assertThat(cap.getValue().getEmail()).isEqualTo("marie.override@company.com");
    }

    @Test
    void hire_withPosteId_linksPosteToEmployee() {
        Recrutement rec  = makeRecrutement(1L, "Chef de Projet", "OUVERT");
        Candidate   cand = makeCandidate(10L, "Bernard", "Paul", "paul.b@test.com", rec);
        HireRequest req  = new HireRequest(10L, 55L, null);

        Poste poste = new Poste();
        poste.setId(55L);
        poste.setTitre("Chef de Projet IT");

        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(candidateRepository.findById(10L)).thenReturn(Optional.of(cand));
        when(employeRepository.existsByEmail("paul.b@test.com")).thenReturn(false);
        when(posteRepository.findById(55L)).thenReturn(Optional.of(poste));

        Employe saved = new Employe();
        saved.setId(300L);
        when(employeRepository.save(any(Employe.class))).thenReturn(saved);
        when(dossierRHRepository.save(any())).thenReturn(new DossierRH());
        when(recrutementRepository.save(any())).thenReturn(rec);
        when(employeMapper.toDto(saved)).thenReturn(
                makeEmployeDto(300L, "Bernard", "Paul", "paul.b@test.com"));

        service.hire(1L, req);

        ArgumentCaptor<Employe> cap = ArgumentCaptor.forClass(Employe.class);
        verify(employeRepository).save(cap.capture());
        assertThat(cap.getValue().getPoste()).isSameAs(poste);
    }

    // ── hire — error paths ────────────────────────────────────────────────────

    @Test
    void hire_recruitmentNotFound_throwsResourceNotFoundException() {
        when(recrutementRepository.findById(99L)).thenReturn(Optional.empty());
        var req = new HireRequest(10L, null, null);

        assertThatThrownBy(() -> service.hire(99L, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Recrutement");

        verify(candidateRepository, never()).findById(any());
        verify(employeRepository, never()).save(any());
    }

    @Test
    void hire_candidateNotFound_throwsResourceNotFoundException() {
        Recrutement rec = makeRecrutement(1L, "Dev", "OUVERT");
        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(candidateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hire(1L, new HireRequest(99L, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Candidate");

        verify(employeRepository, never()).save(any());
    }

    @Test
    void hire_candidateBelongsToOtherRecruitment_throwsBadRequest() {
        Recrutement rec1 = makeRecrutement(1L, "Dev", "OUVERT");
        Recrutement rec2 = makeRecrutement(2L, "QA",  "OUVERT");  // different recruitment
        Candidate cand = makeCandidate(10L, "Martin", "Jean", "jean@test.com", rec2);

        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec1));
        when(candidateRepository.findById(10L)).thenReturn(Optional.of(cand));

        assertThatThrownBy(() -> service.hire(1L, new HireRequest(10L, null, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Candidate does not belong to this recruitment");

        verify(employeRepository, never()).save(any());
    }

    @Test
    void hire_recruitmentAlreadyHired_throwsBadRequest() {
        Recrutement rec = makeRecrutement(1L, "Dev", "EMBAUCHE");   // already closed
        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec));

        assertThatThrownBy(() -> service.hire(1L, new HireRequest(10L, null, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already closed");

        verify(candidateRepository, never()).findById(any());
        verify(employeRepository, never()).save(any());
    }

    @Test
    void hire_duplicateEmail_throwsBadRequest() {
        Recrutement rec  = makeRecrutement(1L, "Dev", "OUVERT");
        Candidate   cand = makeCandidate(10L, "Martin", "Jean", "duplicate@test.com", rec);

        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(candidateRepository.findById(10L)).thenReturn(Optional.of(cand));
        when(employeRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.hire(1L, new HireRequest(10L, null, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("duplicate@test.com");

        verify(employeRepository, never()).save(any());
    }

    @Test
    void hire_posteNotFound_throwsResourceNotFoundException() {
        Recrutement rec  = makeRecrutement(1L, "Dev", "OUVERT");
        Candidate   cand = makeCandidate(10L, "Bob", "Test", "bob@test.com", rec);

        when(recrutementRepository.findById(1L)).thenReturn(Optional.of(rec));
        when(candidateRepository.findById(10L)).thenReturn(Optional.of(cand));
        when(employeRepository.existsByEmail("bob@test.com")).thenReturn(false);
        when(posteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.hire(1L, new HireRequest(10L, 999L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Poste");

        verify(employeRepository, never()).save(any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Recrutement makeRecrutement(Long id, String posteCible, String statut) {
        Recrutement r = new Recrutement();
        r.setId(id);
        r.setPosteCible(posteCible);
        r.setStatut(statut);
        return r;
    }

    private Candidate makeCandidate(Long id, String nom, String prenom,
                                    String email, Recrutement recrutement) {
        Candidate c = new Candidate();
        c.setId(id);
        c.setNom(nom);
        c.setPrenom(prenom);
        c.setEmail(email);
        c.setRecrutement(recrutement);
        return c;
    }

    private EmployeDto makeEmployeDto(Long id, String nom, String prenom, String email) {
        return new EmployeDto(id, nom, prenom, email, null, null, null, null, null,
                Instant.parse("2024-01-01T00:00:00Z"), null);
    }
}
