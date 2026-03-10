package com.smart.rh.controller;

import com.smart.rh.dto.planning.PlanningDto;
import com.smart.rh.dto.planning.PlanningRequest;
import com.smart.rh.service.PlanningService;
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

@Tag(name = "Planning", description = "Employee schedule planning")
@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService service;

    @Operation(summary = "List all planning records (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<PlanningDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get planning by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PlanningDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "List planning by employee")
    @GetMapping("/byEmployee/{employeId}")
    @PreAuthorize("isAuthenticated()")
    public Page<PlanningDto> getByEmployee(
            @PathVariable Long employeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByEmploye(employeId, pageable);
    }

    @Operation(summary = "Create a planning entry")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<PlanningDto> create(@Valid @RequestBody PlanningRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a planning entry")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public PlanningDto update(@PathVariable Long id, @Valid @RequestBody PlanningRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a planning entry")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
