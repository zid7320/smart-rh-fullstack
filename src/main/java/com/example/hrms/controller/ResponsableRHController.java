package com.example.hrms.controller;

import com.example.hrms.dto.ResponsableRHDTO;
import com.example.hrms.mapper.ResponsableRHMapper;
import com.example.hrms.service.ResponsableRHService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/responsables")
public class ResponsableRHController {
    private final ResponsableRHService service;
    private final ResponsableRHMapper mapper;

    public ResponsableRHController(ResponsableRHService service, ResponsableRHMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ResponsableRHDTO>> list() {
        List<ResponsableRHDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<ResponsableRHDTO> create(@Valid @RequestBody ResponsableRHDTO responsableRHDTO) {
        var entity = mapper.toEntity(responsableRHDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsableRHDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponsableRHDTO> update(@PathVariable Integer id, @Valid @RequestBody ResponsableRHDTO responsableRHDTO) {
        return service.get(id).map(existing -> {
            responsableRHDTO.setIdResponsable(id);
            var entity = mapper.toEntity(responsableRHDTO);
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
