package com.example.hrms.controller;

import com.example.hrms.dto.ContratDTO;
import com.example.hrms.mapper.ContratMapper;
import com.example.hrms.service.ContratService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contrats")
public class ContratController {
    private final ContratService service;
    private final ContratMapper mapper;

    public ContratController(ContratService service, ContratMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ContratDTO>> list() {
        List<ContratDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<ContratDTO> create(@Valid @RequestBody ContratDTO contratDTO) {
        var entity = mapper.toEntity(contratDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContratDTO> update(@PathVariable Integer id, @Valid @RequestBody ContratDTO contratDTO) {
        return service.get(id).map(existing -> {
            contratDTO.setIdContrat(id);
            var entity = mapper.toEntity(contratDTO);
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
