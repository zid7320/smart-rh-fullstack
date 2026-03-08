package com.smart.rh.controller;

import com.smart.rh.dto.employe.EmployeDto;
import com.smart.rh.dto.employe.EmployeRequest;
import com.smart.rh.service.EmployeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Employees", description = "Employee management")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeController {

    private final EmployeService service;

    @Operation(summary = "List all employees (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<EmployeDto> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "nom", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.findAll(search, pageable);
    }

    @Operation(summary = "Get employee by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH','EMPLOYEE')")
    public EmployeDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a new employee")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<EmployeDto> create(@Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update an employee")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public EmployeDto update(@PathVariable Long id, @Valid @RequestBody EmployeRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete an employee")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
