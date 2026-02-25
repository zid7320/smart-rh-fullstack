package com.example.hrms.controller;

import com.example.hrms.dto.FormationDTO;
import com.example.hrms.mapper.FormationMapper;
import com.example.hrms.service.FormationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/formations")
public class FormationController {
    private final FormationService service;
    private final FormationMapper mapper;

    public FormationController(FormationService service, FormationMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<FormationDTO>> list() {
        List<FormationDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<FormationDTO> create(@Valid @RequestBody FormationDTO formationDTO) {
        var entity = mapper.toEntity(formationDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormationDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormationDTO> update(@PathVariable Integer id, @Valid @RequestBody FormationDTO formationDTO) {
        return service.get(id).map(existing -> {
            formationDTO.setIdFormation(id);
            var entity = mapper.toEntity(formationDTO);
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
