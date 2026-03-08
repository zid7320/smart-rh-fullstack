package com.smart.rh.controller;

import com.smart.rh.dto.conge.CongeDto;
import com.smart.rh.dto.conge.CongeRequest;
import com.smart.rh.security.UserDetailsImpl;
import com.smart.rh.service.CongeService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Leaves (Congés)", description = "Leave request management with approve/reject workflow")
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class CongeController {

    private final CongeService service;

    @Operation(summary = "List all leave requests (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<CongeDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get leave request by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CongeDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "List leave requests by employee")
    @GetMapping("/byEmployee/{employeId}")
    @PreAuthorize("isAuthenticated()")
    public Page<CongeDto> getByEmployee(
            @PathVariable Long employeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return service.findByEmploye(employeId, pageable);
    }

    @Operation(summary = "Submit a leave request")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CongeDto> create(@Valid @RequestBody CongeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Approve a leave request")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public CongeDto approve(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return service.approve(id, principal.getUser().getId());
    }

    @Operation(summary = "Reject a leave request")
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public CongeDto reject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal) {
        return service.reject(id, principal.getUser().getId());
    }
}
