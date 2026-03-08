package com.smart.rh.controller;

import com.smart.rh.dto.competence.CompetenceDto;
import com.smart.rh.dto.competence.CompetenceRequest;
import com.smart.rh.service.CompetenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Competences", description = "Competence / skill management")
@RestController
@RequestMapping("/api/competences")
@RequiredArgsConstructor
public class CompetenceController {

    private final CompetenceService service;

    @Operation(summary = "List all competences (paginated)")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<CompetenceDto> getAll(@PageableDefault(size = 50) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get competence by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CompetenceDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a new competence")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<CompetenceDto> create(@Valid @RequestBody CompetenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a competence")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public CompetenceDto update(@PathVariable Long id, @Valid @RequestBody CompetenceRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a competence")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
