package com.smart.rh.controller;

import com.smart.rh.dto.formation.FormationDto;
import com.smart.rh.dto.formation.FormationRequest;
import com.smart.rh.service.FormationService;
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

@Tag(name = "Trainings (Formations)", description = "Employee training management")
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class FormationController {

    private final FormationService service;

    @Operation(summary = "List all trainings (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<FormationDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get training by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public FormationDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "List trainings by employee")
    @GetMapping("/byEmployee/{employeId}")
    @PreAuthorize("isAuthenticated()")
    public Page<FormationDto> getByEmployee(
            @PathVariable Long employeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByEmploye(employeId, pageable);
    }

    @Operation(summary = "Create a new training")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<FormationDto> create(@Valid @RequestBody FormationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a training")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public FormationDto update(@PathVariable Long id, @Valid @RequestBody FormationRequest request) {
        return service.update(id, request);
    }
}
