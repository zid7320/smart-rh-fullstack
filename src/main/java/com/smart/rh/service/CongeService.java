package com.smart.rh.service;

import com.smart.rh.dto.conge.CongeDto;
import com.smart.rh.dto.conge.CongeRequest;
import com.smart.rh.entity.Conge;
import com.smart.rh.entity.CongeStatut;
import com.smart.rh.entity.Employe;
import com.smart.rh.exception.BadRequestException;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.CongeMapper;
import com.smart.rh.repository.CongeRepository;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CongeService {

    private final CongeRepository   repository;
    private final EmployeRepository employeRepository;
    private final UserRepository    userRepository;
    private final AuditService      auditService;
    private final CongeMapper       mapper;

    @Transactional(readOnly = true)
    public Page<CongeDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public CongeDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CongeDto> findByEmploye(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(mapper::toDto);
    }

    @Transactional
    public CongeDto create(CongeRequest request) {
        if (request.dateFin().isBefore(request.dateDebut())) {
            throw new BadRequestException("dateFin must be after or equal to dateDebut");
        }
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
        Conge conge = mapper.toEntity(request);
        conge.setEmploye(employe);
        Conge saved = repository.save(conge);
        auditService.log("CREATE", "Conge", saved.getId(),
                "Leave request submitted for employe #" + employe.getId()
                        + " (" + saved.getType() + ")");
        return mapper.toDto(saved);
    }

    @Transactional
    public CongeDto approve(Long id, Long actorUserId) {
        Conge conge = getOrThrow(id);
        assertPending(conge);
        conge.setStatut(CongeStatut.APPROVED);
        conge.setDecidedAt(Instant.now());
        setDecidedBy(conge, actorUserId);
        Conge saved = repository.save(conge);
        auditService.log("APPROVE_LEAVE", "Conge", saved.getId(), "Leave approved");
        return mapper.toDto(saved);
    }

    @Transactional
    public CongeDto reject(Long id, Long actorUserId) {
        Conge conge = getOrThrow(id);
        assertPending(conge);
        conge.setStatut(CongeStatut.REJECTED);
        conge.setDecidedAt(Instant.now());
        setDecidedBy(conge, actorUserId);
        Conge saved = repository.save(conge);
        auditService.log("REJECT_LEAVE", "Conge", saved.getId(), "Leave rejected");
        return mapper.toDto(saved);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Conge getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conge", "id", id));
    }

    private void assertPending(Conge conge) {
        if (conge.getStatut() != CongeStatut.PENDING) {
            throw new BadRequestException("Leave request has already been decided (status: " + conge.getStatut() + ")");
        }
    }

    private void setDecidedBy(Conge conge, Long userId) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(conge::setDecidedBy);
        }
    }
}
