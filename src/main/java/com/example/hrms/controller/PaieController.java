package com.example.hrms.controller;

import com.example.hrms.dto.PaieDTO;
import com.example.hrms.mapper.PaieMapper;
import com.example.hrms.service.PaieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/paies")
public class PaieController {
    private final PaieService service;
    private final PaieMapper mapper;

    public PaieController(PaieService service, PaieMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<PaieDTO>> list() {
        List<PaieDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<PaieDTO> create(@Valid @RequestBody PaieDTO paieDTO) {
        var entity = mapper.toEntity(paieDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaieDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaieDTO> update(@PathVariable Integer id, @Valid @RequestBody PaieDTO paieDTO) {
        return service.get(id).map(existing -> {
            paieDTO.setIdPaie(id);
            var entity = mapper.toEntity(paieDTO);
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
