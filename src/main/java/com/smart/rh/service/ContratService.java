package com.smart.rh.service;

import com.smart.rh.dto.contrat.ContratDto;
import com.smart.rh.dto.contrat.ContratRequest;
import com.smart.rh.entity.Contrat;
import com.smart.rh.entity.Employe;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.ContratMapper;
import com.smart.rh.repository.ContratRepository;
import com.smart.rh.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository  repository;
    private final EmployeRepository  employeRepository;
    private final ContratMapper      mapper;
    private final AuditService       auditService;

    @Transactional(readOnly = true)
    public Page<ContratDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public ContratDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ContratDto> findByEmploye(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(mapper::toDto);
    }

    @Transactional
    public ContratDto create(ContratRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
        Contrat contrat = mapper.toEntity(request);
        contrat.setEmploye(employe);
        Contrat saved = repository.save(contrat);
        auditService.log("CREATE", "Contrat", saved.getId(),
                "Contract created for employe #" + employe.getId()
                        + " (" + saved.getType() + ")");
        return mapper.toDto(saved);
    }

    @Transactional
    public ContratDto update(Long id, ContratRequest request) {
        Contrat contrat = getOrThrow(id);
        mapper.partialUpdate(request, contrat);
        if (request.employeId() != null && !request.employeId().equals(contrat.getEmploye().getId())) {
            Employe employe = employeRepository.findById(request.employeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
            contrat.setEmploye(employe);
        }
        Contrat saved = repository.save(contrat);
        auditService.log("UPDATE", "Contrat", saved.getId(),
                "Contract #" + saved.getId() + " updated");
        return mapper.toDto(saved);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Contrat getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", id));
    }
}
