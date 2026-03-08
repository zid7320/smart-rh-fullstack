package com.smart.rh.service;

import com.smart.rh.dto.evaluation.EvaluationDto;
import com.smart.rh.dto.evaluation.EvaluationRequest;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Evaluation;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.EvaluationMapper;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.EvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository repository;
    private final EmployeRepository    employeRepository;
    private final EvaluationMapper     mapper;
    private final AuditService         auditService;

    @Transactional(readOnly = true)
    public Page<EvaluationDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public EvaluationDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<EvaluationDto> findByEmploye(Long employeId, Pageable pageable) {
        return repository.findByEmployeId(employeId, pageable).map(mapper::toDto);
    }

    @Transactional
    public EvaluationDto create(EvaluationRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
        Evaluation eval = mapper.toEntity(request);
        eval.setEmploye(employe);
        Evaluation saved = repository.save(eval);
        auditService.log("CREATE", "Evaluation", saved.getId(),
                "Evaluation created for employe #" + employe.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public EvaluationDto update(Long id, EvaluationRequest request) {
        Evaluation eval = getOrThrow(id);
        mapper.partialUpdate(request, eval);
        if (request.employeId() != null && !request.employeId().equals(eval.getEmploye().getId())) {
            Employe employe = employeRepository.findById(request.employeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", request.employeId()));
            eval.setEmploye(employe);
        }
        Evaluation saved = repository.save(eval);
        auditService.log("UPDATE", "Evaluation", saved.getId(),
                "Evaluation #" + saved.getId() + " updated");
        return mapper.toDto(saved);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Evaluation getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation", "id", id));
    }
}
