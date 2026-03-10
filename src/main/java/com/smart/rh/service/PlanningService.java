package com.smart.rh.service;

import com.smart.rh.dto.planning.PlanningDto;
import com.smart.rh.dto.planning.PlanningRequest;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Planning;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.PlanningMapper;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.PlanningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final PlanningRepository repository;
    private final EmployeRepository  employeRepository;
    private final PlanningMapper     mapper;
    private final AuditService       auditService;

    @Transactional(readOnly = true)
    public Page<PlanningDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PlanningDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<PlanningDto> findByEmploye(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(mapper::toDto);
    }

    @Transactional
    public PlanningDto create(PlanningRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
        Planning planning = mapper.toEntity(request);
        planning.setEmploye(employe);
        Planning saved = repository.save(planning);
        auditService.log("CREATE", "Planning", saved.getId(),
                "Planning created for employe #" + employe.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public PlanningDto update(Long id, PlanningRequest request) {
        Planning planning = getOrThrow(id);
        if (!planning.getEmploye().getId().equals(request.employeId())) {
            Employe employe = employeRepository.findById(request.employeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
            planning.setEmploye(employe);
        }
        mapper.partialUpdate(request, planning);
        Planning saved = repository.save(planning);
        auditService.log("UPDATE", "Planning", saved.getId(), "Planning updated #" + saved.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        repository.deleteById(id);
        auditService.log("DELETE", "Planning", id, "Deleted planning #" + id);
    }

    private Planning getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Planning", "id", id));
    }
}
