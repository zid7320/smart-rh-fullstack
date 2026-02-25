package com.example.hrms.controller;

import com.example.hrms.dto.CompetenceDTO;
import com.example.hrms.mapper.CompetenceMapper;
import com.example.hrms.service.CompetenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/competences")
public class CompetenceController {
    private final CompetenceService service;
    private final CompetenceMapper mapper;

    public CompetenceController(CompetenceService service, CompetenceMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CompetenceDTO>> list() {
        List<CompetenceDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CompetenceDTO> create(@Valid @RequestBody CompetenceDTO competenceDTO) {
        var entity = mapper.toEntity(competenceDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetenceDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenceDTO> update(@PathVariable Integer id, @Valid @RequestBody CompetenceDTO competenceDTO) {
        return service.get(id).map(existing -> {
            competenceDTO.setIdCompetence(id);
            var entity = mapper.toEntity(competenceDTO);
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
