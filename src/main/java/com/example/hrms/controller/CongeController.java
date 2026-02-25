package com.example.hrms.controller;

import com.example.hrms.dto.CongeDTO;
import com.example.hrms.mapper.CongeMapper;
import com.example.hrms.service.CongeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/conges")
public class CongeController {
    private final CongeService service;
    private final CongeMapper mapper;

    public CongeController(CongeService service, CongeMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CongeDTO>> list() {
        List<CongeDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<CongeDTO> create(@Valid @RequestBody CongeDTO congeDTO) {
        var entity = mapper.toEntity(congeDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CongeDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CongeDTO> update(@PathVariable Integer id, @Valid @RequestBody CongeDTO congeDTO) {
        return service.get(id).map(existing -> {
            congeDTO.setIdConge(id);
            var entity = mapper.toEntity(congeDTO);
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
