package com.smart.rh.service;

import com.smart.rh.dto.employe.EmployeDto;
import com.smart.rh.dto.recrutement.CandidateDto;
import com.smart.rh.dto.recrutement.CandidateRequest;
import com.smart.rh.dto.recrutement.HireRequest;
import com.smart.rh.dto.recrutement.RecrutementDto;
import com.smart.rh.dto.recrutement.RecrutementRequest;
import com.smart.rh.entity.*;
import com.smart.rh.exception.BadRequestException;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.CandidateMapper;
import com.smart.rh.mapper.EmployeMapper;
import com.smart.rh.mapper.RecrutementMapper;
import com.smart.rh.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecrutementService {

    private final RecrutementRepository   recrutementRepository;
    private final CandidateRepository     candidateRepository;
    private final EmployeRepository       employeRepository;
    private final DossierRHRepository     dossierRHRepository;
    private final PosteRepository         posteRepository;
    private final ResponsableRHRepository responsableRHRepository;
    private final AuditService            auditService;
    private final RecrutementMapper       recrutementMapper;
    private final CandidateMapper         candidateMapper;
    private final EmployeMapper           employeMapper;

    // ── Recrutement CRUD ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<RecrutementDto> findAll(Pageable pageable) {
        return recrutementRepository.findAll(pageable).map(recrutementMapper::toDto);
    }

    @Transactional(readOnly = true)
    public RecrutementDto findById(Long id) {
        return recrutementMapper.toDto(getOrThrow(id));
    }

    @Transactional
    public RecrutementDto create(RecrutementRequest request) {
        Recrutement rec = recrutementMapper.toEntity(request);
        if (request.responsableId() != null) {
            ResponsableRH resp = responsableRHRepository.findById(request.responsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("ResponsableRH", "id", request.responsableId()));
            rec.setResponsable(resp);
        }
        Recrutement saved = recrutementRepository.save(rec);
        auditService.log("CREATE", "Recrutement", saved.getId(),
                "Recruitment created for poste: " + saved.getPosteCible());
        return recrutementMapper.toDto(saved);
    }

    @Transactional
    public RecrutementDto update(Long id, RecrutementRequest request) {
        Recrutement rec = getOrThrow(id);
        recrutementMapper.partialUpdate(request, rec);
        if (request.responsableId() != null) {
            ResponsableRH resp = responsableRHRepository.findById(request.responsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("ResponsableRH", "id", request.responsableId()));
            rec.setResponsable(resp);
        }
        Recrutement saved = recrutementRepository.save(rec);
        auditService.log("UPDATE", "Recrutement", saved.getId(),
                "Recruitment #" + saved.getId() + " updated");
        return recrutementMapper.toDto(saved);
    }

    // ── Candidate sub-resource ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CandidateDto> findCandidates(Long recrutementId, Pageable pageable) {
        getOrThrow(recrutementId); // existence check
        return candidateRepository.findByRecrutementId(recrutementId, pageable)
                .map(candidateMapper::toDto);
    }

    @Transactional
    public CandidateDto addCandidate(Long recrutementId, CandidateRequest request) {
        Recrutement rec = getOrThrow(recrutementId);
        Candidate candidate = candidateMapper.toEntity(request);
        candidate.setRecrutement(rec);
        Candidate saved = candidateRepository.save(candidate);
        auditService.log("ADD_CANDIDATE", "Candidate", saved.getId(),
                "Candidate " + saved.getNom() + " " + saved.getPrenom()
                        + " added to recruitment #" + recrutementId);
        return candidateMapper.toDto(saved);
    }

    // ── Hire ─────────────────────────────────────────────────────────────────

    @Transactional
    public EmployeDto hire(Long recrutementId, HireRequest request) {
        Recrutement rec = getOrThrow(recrutementId);
        if ("EMBAUCHE".equals(rec.getStatut())) {
            throw new BadRequestException("Recruitment is already closed with a hire");
        }

        Candidate candidate = candidateRepository.findById(request.candidateId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate", "id", request.candidateId()));

        if (!candidate.getRecrutement().getId().equals(recrutementId)) {
            throw new BadRequestException("Candidate does not belong to this recruitment");
        }

        String email = (request.email() != null && !request.email().isBlank())
                ? request.email() : candidate.getEmail();

        if (employeRepository.existsByEmail(email)) {
            throw new BadRequestException("An employee with this email already exists: " + email);
        }

        // Build the new employee
        Employe emp = new Employe();
        emp.setNom(candidate.getNom());
        emp.setPrenom(candidate.getPrenom());
        emp.setEmail(email);
        emp.setPosteLibelle(rec.getPosteCible());
        emp.setRecrutement(rec);

        if (request.posteId() != null) {
            Poste poste = posteRepository.findById(request.posteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Poste", "id", request.posteId()));
            emp.setPoste(poste);
        }

        Employe saved = employeRepository.save(emp);

        // Auto-create an empty dossier for the new employee
        DossierRH dossier = new DossierRH();
        dossier.setEmploye(saved);
        dossierRHRepository.save(dossier);

        // Update recruitment status
        rec.setStatut("EMBAUCHE");
        recrutementRepository.save(rec);

        auditService.log("HIRE", "Employe", saved.getId(),
                "Hired candidate " + candidate.getNom() + " " + candidate.getPrenom()
                        + " for recruitment #" + recrutementId);

        return employeMapper.toDto(saved);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Recrutement getOrThrow(Long id) {
        return recrutementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recrutement", "id", id));
    }
}
