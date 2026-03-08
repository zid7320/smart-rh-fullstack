package com.smart.rh.controller;

import com.smart.rh.dto.dossier.DossierRHDto;
import com.smart.rh.dto.dossier.DossierRHRequest;
import com.smart.rh.service.DossierRHService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Dossiers RH", description = "HR dossier management")
@RestController
@RequestMapping("/api/dossiers")
@RequiredArgsConstructor
public class DossierRHController {

    private final DossierRHService service;

    @Operation(summary = "List all dossiers (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<DossierRHDto> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get dossier by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH','EMPLOYEE')")
    public DossierRHDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Get dossier by employee ID")
    @GetMapping("/byEmployee/{employeId}")
    @PreAuthorize("hasAnyRole('ADMIN','RH','EMPLOYEE')")
    public DossierRHDto getByEmploye(@PathVariable Long employeId) {
        return service.findByEmployeId(employeId);
    }

    @Operation(summary = "Update a dossier")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public DossierRHDto update(@PathVariable Long id, @Valid @RequestBody DossierRHRequest request) {
        return service.update(id, request);
    }
}
