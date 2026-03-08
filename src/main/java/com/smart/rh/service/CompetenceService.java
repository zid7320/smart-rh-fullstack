package com.smart.rh.service;

import com.smart.rh.dto.competence.CompetenceDto;
import com.smart.rh.dto.competence.CompetenceRequest;
import com.smart.rh.entity.Competence;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.CompetenceMapper;
import com.smart.rh.repository.CompetenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompetenceService {

    private final CompetenceRepository repository;
    private final CompetenceMapper     mapper;
    private final AuditService         auditService;

    @Transactional(readOnly = true)
    public Page<CompetenceDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public CompetenceDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional
    public CompetenceDto create(CompetenceRequest request) {
        Competence competence = mapper.toEntity(request);
        Competence saved = repository.save(competence);
        auditService.log("CREATE", "Competence", saved.getId(),
                "Competence created: " + saved.getNom());
        return mapper.toDto(saved);
    }

    @Transactional
    public CompetenceDto update(Long id, CompetenceRequest request) {
        Competence competence = getOrThrow(id);
        mapper.partialUpdate(request, competence);
        Competence saved = repository.save(competence);
        auditService.log("UPDATE", "Competence", saved.getId(),
                "Competence #" + saved.getId() + " updated: " + saved.getNom());
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        repository.deleteById(id);
        auditService.log("DELETE", "Competence", id, "Deleted competence #" + id);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    public Competence getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Competence", "id", id));
    }
}
