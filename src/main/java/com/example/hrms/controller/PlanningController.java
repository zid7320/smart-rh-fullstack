package com.example.hrms.controller;

import com.example.hrms.dto.PlanningDTO;
import com.example.hrms.mapper.PlanningMapper;
import com.example.hrms.service.PlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/plannings")
public class PlanningController {
    private final PlanningService service;
    private final PlanningMapper mapper;

    public PlanningController(PlanningService service, PlanningMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<PlanningDTO>> list() {
        List<PlanningDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<PlanningDTO> create(@Valid @RequestBody PlanningDTO planningDTO) {
        var entity = mapper.toEntity(planningDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanningDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanningDTO> update(@PathVariable Integer id, @Valid @RequestBody PlanningDTO planningDTO) {
        return service.get(id).map(existing -> {
            planningDTO.setIdPlanning(id);
            var entity = mapper.toEntity(planningDTO);
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
