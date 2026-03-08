package com.smart.rh.controller;

import com.smart.rh.dto.evaluation.EvaluationDto;
import com.smart.rh.dto.evaluation.EvaluationRequest;
import com.smart.rh.service.EvaluationService;
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

@Tag(name = "Evaluations", description = "Employee performance evaluation management")
@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService service;

    @Operation(summary = "List all evaluations (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<EvaluationDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get evaluation by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public EvaluationDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "List evaluations by employee")
    @GetMapping("/byEmployee/{employeId}")
    @PreAuthorize("isAuthenticated()")
    public Page<EvaluationDto> getByEmployee(
            @PathVariable Long employeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByEmploye(employeId, pageable);
    }

    @Operation(summary = "Create a new evaluation")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<EvaluationDto> create(@Valid @RequestBody EvaluationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update an evaluation")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public EvaluationDto update(@PathVariable Long id, @Valid @RequestBody EvaluationRequest request) {
        return service.update(id, request);
    }
}
