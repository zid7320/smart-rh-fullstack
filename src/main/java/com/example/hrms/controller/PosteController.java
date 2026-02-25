package com.example.hrms.controller;

import com.example.hrms.dto.PosteDTO;
import com.example.hrms.mapper.PosteMapper;
import com.example.hrms.service.PosteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/postes")
public class PosteController {
    private final PosteService service;
    private final PosteMapper mapper;

    public PosteController(PosteService service, PosteMapper mapper) { 
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<PosteDTO>> list() {
        List<PosteDTO> dtos = service.list().stream()
            .map(mapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<PosteDTO> create(@Valid @RequestBody PosteDTO posteDTO) {
        var entity = mapper.toEntity(posteDTO);
        var saved = service.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PosteDTO> get(@PathVariable Integer id) {
        return service.get(id)
            .map(e -> ResponseEntity.ok(mapper.toDTO(e)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PosteDTO> update(@PathVariable Integer id, @Valid @RequestBody PosteDTO posteDTO) {
        return service.get(id).map(existing -> {
            posteDTO.setIdPoste(id);
            var entity = mapper.toEntity(posteDTO);
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
