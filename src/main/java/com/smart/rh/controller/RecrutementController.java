package com.smart.rh.controller;

import com.smart.rh.dto.employe.EmployeDto;
import com.smart.rh.dto.recrutement.*;
import com.smart.rh.service.RecrutementService;
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

@Tag(name = "Recruitments", description = "Recruitment & hiring management")
@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
public class RecrutementController {

    private final RecrutementService service;

    @Operation(summary = "List all recruitments (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<RecrutementDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get recruitment by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public RecrutementDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a recruitment")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<RecrutementDto> create(@Valid @RequestBody RecrutementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a recruitment")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public RecrutementDto update(@PathVariable Long id, @Valid @RequestBody RecrutementRequest request) {
        return service.update(id, request);
    }

    // ── Candidates sub-resource ────────────────────────────────────────────────

    @Operation(summary = "List candidates for a recruitment")
    @GetMapping("/{id}/candidates")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<CandidateDto> getCandidates(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findCandidates(id, pageable);
    }

    @Operation(summary = "Add a candidate to a recruitment")
    @PostMapping("/{id}/candidates")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<CandidateDto> addCandidate(
            @PathVariable Long id,
            @Valid @RequestBody CandidateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addCandidate(id, request));
    }

    // ── Hire ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Hire a candidate — converts candidate to employee")
    @PostMapping("/{id}/hire")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<EmployeDto> hire(
            @PathVariable Long id,
            @Valid @RequestBody HireRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.hire(id, request));
    }
}
