package com.example.hrms.controller;

import com.example.hrms.dto.EvaluationDTO;
import com.example.hrms.mapper.EvaluationMapper;
import com.example.hrms.service.EvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {
    private final EvaluationService service;
    private final EvaluationMapper mapper;

    public EvaluationController(EvaluationService service, EvaluationMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<EvaluationDTO>> list() {
        List<EvaluationDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<EvaluationDTO> create(@Valid @RequestBody EvaluationDTO evaluationDTO) {
        var entity = mapper.toEntity(evaluationDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EvaluationDTO> update(@PathVariable Integer id, @Valid @RequestBody EvaluationDTO evaluationDTO) {
        return service.get(id).map(existing -> {
            evaluationDTO.setIdEval(id);
            var entity = mapper.toEntity(evaluationDTO);
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
