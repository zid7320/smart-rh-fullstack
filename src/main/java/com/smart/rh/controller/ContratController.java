package com.smart.rh.controller;

import com.smart.rh.dto.contrat.ContratDto;
import com.smart.rh.dto.contrat.ContratRequest;
import com.smart.rh.service.ContratService;
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

@Tag(name = "Contracts", description = "Employee contract management")
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContratController {

    private final ContratService service;

    @Operation(summary = "List all contracts (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<ContratDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get contract by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ContratDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "List contracts by employee")
    @GetMapping("/byEmployee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN','RH','EMPLOYEE')")
    public Page<ContratDto> getByEmployee(
            @PathVariable Long employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByEmploye(employeeId, pageable);
    }

    @Operation(summary = "Create a new contract")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<ContratDto> create(@Valid @RequestBody ContratRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a contract")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ContratDto update(@PathVariable Long id, @Valid @RequestBody ContratRequest request) {
        return service.update(id, request);
    }
}
