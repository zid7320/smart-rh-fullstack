package com.example.hrms.controller;

import com.example.hrms.dto.EmployeDTO;
import com.example.hrms.mapper.EmployeMapper;
import com.example.hrms.service.EmployeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employes")
public class EmployeController {
    private final EmployeService service;
    private final EmployeMapper mapper;

    public EmployeController(EmployeService service, EmployeMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<EmployeDTO>> list() {
        List<EmployeDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<EmployeDTO> create(@RequestBody EmployeDTO employeDTO) {
        var entity = mapper.toEntity(employeDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeDTO> update(@PathVariable Integer id, @Valid @RequestBody EmployeDTO employeDTO) {
        return service.get(id).map(existing -> {
            employeDTO.setIdEmploye(id);
            var entity = mapper.toEntity(employeDTO);
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
