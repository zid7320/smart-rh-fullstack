package com.smart.rh.service;

import com.smart.rh.dto.formation.FormationDto;
import com.smart.rh.dto.formation.FormationRequest;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Formation;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.FormationMapper;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.FormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository repository;
    private final EmployeRepository   employeRepository;
    private final FormationMapper     mapper;
    private final AuditService        auditService;

    @Transactional(readOnly = true)
    public Page<FormationDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public FormationDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<FormationDto> findByEmploye(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(mapper::toDto);
    }

    @Transactional
    public FormationDto create(FormationRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
        Formation formation = mapper.toEntity(request);
        formation.setEmploye(employe);
        Formation saved = repository.save(formation);
        auditService.log("CREATE", "Formation", saved.getId(),
                "Training \"" + saved.getTitre() + "\" created for employe #" + employe.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public FormationDto update(Long id, FormationRequest request) {
        Formation formation = getOrThrow(id);
        mapper.partialUpdate(request, formation);
        if (request.employeId() != null && !request.employeId().equals(formation.getEmploye().getId())) {
            Employe employe = employeRepository.findById(request.employeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
            formation.setEmploye(employe);
        }
        Formation saved = repository.save(formation);
        auditService.log("UPDATE", "Formation", saved.getId(),
                "Training #" + saved.getId() + " updated");
        return mapper.toDto(saved);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Formation getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation", "id", id));
    }
}
