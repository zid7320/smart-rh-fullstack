package com.example.hrms.controller;

import com.example.hrms.dto.CandidateDTO;
import com.example.hrms.mapper.CandidateMapper;
import com.example.hrms.service.CandidateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {
    private final CandidateService service;
    private final CandidateMapper mapper;

    public CandidateController(CandidateService service, CandidateMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CandidateDTO>> list() {
        List<CandidateDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CandidateDTO> create(@Valid @RequestBody CandidateDTO candidateDTO) {
        var entity = mapper.toEntity(candidateDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateDTO> update(@PathVariable Integer id, @Valid @RequestBody CandidateDTO candidateDTO) {
        return service.get(id).map(existing -> {
            candidateDTO.setIdCandidate(id);
            var entity = mapper.toEntity(candidateDTO);
            var updated = service.update(entity);
            return ResponseEntity.ok(mapper.toDTO(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (service.get(id).isPresent()) {
            service.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
