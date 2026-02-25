package com.example.hrms.controller;

import com.example.hrms.dto.DossierRHDTO;
import com.example.hrms.mapper.DossierRHMapper;
import com.example.hrms.service.DossierRHService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dossiers")
public class DossierRHController {
    private final DossierRHService service;
    private final DossierRHMapper mapper;

    public DossierRHController(DossierRHService service, DossierRHMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<DossierRHDTO>> list() {
        List<DossierRHDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<DossierRHDTO> create(@Valid @RequestBody DossierRHDTO dossierRHDTO) {
        var entity = mapper.toEntity(dossierRHDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DossierRHDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DossierRHDTO> update(@PathVariable Integer id, @Valid @RequestBody DossierRHDTO dossierRHDTO) {
        return service.get(id).map(existing -> {
            dossierRHDTO.setIdDossier(id);
            var entity = mapper.toEntity(dossierRHDTO);
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
