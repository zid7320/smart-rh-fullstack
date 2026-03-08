package com.smart.rh.controller;

import com.smart.rh.dto.paie.PaieDto;
import com.smart.rh.dto.paie.PayrollGenerateRequest;
import com.smart.rh.service.PaieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll (Paie)", description = "Payroll generation and PDF bulletins")
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PaieController {

    private final PaieService service;

    @Operation(summary = "List all payroll records (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public Page<PaieDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get payroll record by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PaieDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "List payroll records by employee")
    @GetMapping("/byEmployee/{employeId}")
    @PreAuthorize("isAuthenticated()")
    public Page<PaieDto> getByEmployee(
            @PathVariable Long employeId,
            @PageableDefault(size = 12) Pageable pageable) {
        return service.findByEmploye(employeId, pageable);
    }

    @Operation(summary = "Generate payroll for all employees for a given month/year")
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<List<PaieDto>> generate(@Valid @RequestBody PayrollGenerateRequest request) {
        List<PaieDto> created = service.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Download payslip PDF for a payroll record")
    @GetMapping("/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        byte[] pdf = service.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"bulletin-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
