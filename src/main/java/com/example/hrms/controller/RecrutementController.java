package com.example.hrms.controller;

import com.example.hrms.dto.RecrutementDTO;
import com.example.hrms.mapper.RecrutementMapper;
import com.example.hrms.service.RecruitmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recrutements")
public class RecrutementController {
    private final RecruitmentService service;
    private final RecrutementMapper mapper;

    public RecrutementController(RecruitmentService service, RecrutementMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<RecrutementDTO>> list() {
        List<RecrutementDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<RecrutementDTO> create(@Valid @RequestBody RecrutementDTO recrutementDTO) {
        var entity = mapper.toEntity(recrutementDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecrutementDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecrutementDTO> update(@PathVariable Integer id, @Valid @RequestBody RecrutementDTO recrutementDTO) {
        return service.get(id).map(existing -> {
            recrutementDTO.setIdRecrutement(id);
            var entity = mapper.toEntity(recrutementDTO);
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
