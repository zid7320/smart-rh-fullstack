package com.smart.rh.service;

import com.smart.rh.dto.dossier.DossierRHDto;
import com.smart.rh.dto.dossier.DossierRHRequest;
import com.smart.rh.entity.DossierRH;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.DossierRHMapper;
import com.smart.rh.repository.DossierRHRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DossierRHService {

    private final DossierRHRepository repository;
    private final DossierRHMapper     mapper;
    private final AuditService        auditService;

    @Transactional(readOnly = true)
    public Page<DossierRHDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public DossierRHDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public DossierRHDto findByEmployeId(Long employeId) {
        DossierRH dossier = repository.findByEmployeId(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("DossierRH", "employeId", employeId));
        return mapper.toDto(dossier);
    }

    @Transactional
    public DossierRHDto update(Long id, DossierRHRequest request) {
        DossierRH dossier = getOrThrow(id);
        mapper.partialUpdate(request, dossier);
        DossierRH saved = repository.save(dossier);
        auditService.log("UPDATE", "DossierRH", saved.getId(),
                "DossierRH #" + saved.getId() + " updated");
        return mapper.toDto(saved);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private DossierRH getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DossierRH", "id", id));
    }
}
