package com.smart.rh.service;

import com.smart.rh.dto.employe.EmployeDto;
import com.smart.rh.dto.employe.EmployeRequest;
import com.smart.rh.entity.Employe;
import com.smart.rh.entity.Poste;
import com.smart.rh.exception.BadRequestException;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.EmployeMapper;
import com.smart.rh.repository.EmployeRepository;
import com.smart.rh.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final PosteRepository   posteRepository;
    private final EmployeMapper     mapper;
    private final AuditService      auditService;

    // List
    @Transactional(readOnly = true)
    public Page<EmployeDto> findAll(String search, Pageable pageable) {
        Page<Employe> page = (search != null && !search.isBlank())
                ? employeRepository.search(search.trim(), pageable)
                : employeRepository.findAll(pageable);
        return page.map(mapper::toDto);
    }

    // Get by ID
    @Transactional(readOnly = true)
    public EmployeDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    // Get by linked user ID (for /me endpoint)
    @Transactional(readOnly = true)
    public Optional<EmployeDto> findByUserId(Long userId) {
        return employeRepository.findByUser_Id(userId).map(mapper::toDto);
    }

    // Create
    @Transactional
    public EmployeDto create(EmployeRequest request) {
        if (employeRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already used: " + request.email());
        }
        Employe emp = mapper.toEntity(request);
        resolvePoste(emp, request.posteId());
        Employe saved = employeRepository.save(emp);
        auditService.log("CREATE", "Employe", saved.getId(),
                "Created: " + saved.getPrenom() + " " + saved.getNom());
        return mapper.toDto(saved);
    }

    // Update
    @Transactional
    public EmployeDto update(Long id, EmployeRequest request) {
        Employe emp = getOrThrow(id);
        if (!emp.getEmail().equals(request.email())
                && employeRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already used: " + request.email());
        }
        mapper.partialUpdate(request, emp);
        resolvePoste(emp, request.posteId());
        Employe saved = employeRepository.save(emp);
        auditService.log("UPDATE", "Employe", saved.getId(),
                "Updated employe #" + saved.getId());
        return mapper.toDto(saved);
    }

    // Delete
    @Transactional
    public void delete(Long id) {
        getOrThrow(id);
        employeRepository.deleteById(id);
        auditService.log("DELETE", "Employe", id, "Deleted employe #" + id);
    }

    // Helpers
    public Employe getOrThrow(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employe", "id", id));
    }

    private void resolvePoste(Employe emp, Long posteId) {
        if (posteId != null) {
            Poste poste = posteRepository.findById(posteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Poste", "id", posteId));
            emp.setPoste(poste);
        }
    }
}
