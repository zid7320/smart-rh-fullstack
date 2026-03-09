package com.smart.rh.service;

import com.smart.rh.dto.poste.PosteDto;
import com.smart.rh.dto.poste.PosteRequest;
import com.smart.rh.entity.Competence;
import com.smart.rh.entity.Poste;
import com.smart.rh.exception.BadRequestException;
import com.smart.rh.exception.ResourceNotFoundException;
import com.smart.rh.mapper.PosteMapper;
import com.smart.rh.repository.CompetenceRepository;
import com.smart.rh.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PosteService {

    private final PosteRepository       repository;
    private final CompetenceRepository  competenceRepository;
    private final PosteMapper           mapper;
    private final AuditService          auditService;

    @Transactional(readOnly = true)
    public Page<PosteDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public PosteDto findById(Long id) {
        return mapper.toDto(getOrThrow(id));
    }

    @Transactional
    public PosteDto create(PosteRequest request) {
        Poste poste = mapper.toEntity(request);
        resolveCompetences(poste, request.competenceIds());
        Poste saved = repository.save(poste);
        auditService.log("CREATE", "Poste", saved.getId(),
                "Poste created: " + saved.getTitre());
        return mapper.toDto(saved);
    }

    @Transactional
    public PosteDto update(Long id, PosteRequest request) {
        Poste poste = getOrThrow(id);
        mapper.partialUpdate(request, poste);
        resolveCompetences(poste, request.competenceIds());
        Poste saved = repository.save(poste);
        auditService.log("UPDATE", "Poste", saved.getId(),
                "Poste #" + saved.getId() + " updated: " + saved.getTitre());
        return mapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Poste poste = getOrThrow(id);
        if (!poste.getEmployes().isEmpty()) {
            throw new BadRequestException("Cannot delete Poste with assigned employees");
        }
        repository.deleteById(id);
        auditService.log("DELETE", "Poste", id, "Deleted poste #" + id);
    }

    /** Attach a set of competences to a poste (replaces existing). */
    @Transactional
    public PosteDto attachCompetences(Long id, List<Long> competenceIds) {
        Poste poste = getOrThrow(id);
        resolveCompetences(poste, competenceIds);
        Poste saved = repository.save(poste);
        auditService.log("ATTACH_COMPETENCES", "Poste", saved.getId(),
                "Attached " + competenceIds.size() + " competence(s) to poste #" + saved.getId());
        return mapper.toDto(saved);
    }

    /** Add a single competence to a poste (path-param style, idempotent). */
    @Transactional
    public PosteDto addCompetence(Long id, Long competenceId) {
        Poste poste = getOrThrow(id);
        Competence competence = competenceRepository.findById(competenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Competence", "id", competenceId));
        poste.getCompetences().add(competence);
        Poste saved = repository.save(poste);
        auditService.log("ADD_COMPETENCE", "Poste", saved.getId(),
                "Added competence #" + competenceId + " to poste #" + saved.getId());
        return mapper.toDto(saved);
    }

    /** Detach a single competence from a poste. */
    @Transactional
    public PosteDto detachCompetence(Long id, Long competenceId) {
        Poste poste = getOrThrow(id);
        poste.getCompetences().removeIf(c -> c.getId().equals(competenceId));
        Poste saved = repository.save(poste);
        auditService.log("DETACH_COMPETENCE", "Poste", saved.getId(),
                "Detached competence #" + competenceId + " from poste #" + saved.getId());
        return mapper.toDto(saved);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    public Poste getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Poste", "id", id));
    }

    private void resolveCompetences(Poste poste, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        Set<Competence> competences = new HashSet<>(competenceRepository.findAllById(ids));
        poste.setCompetences(competences);
    }
}
