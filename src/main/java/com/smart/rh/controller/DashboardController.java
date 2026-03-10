package com.smart.rh.controller;

import com.smart.rh.entity.CongeStatut;
import com.smart.rh.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Dashboard", description = "KPI statistics for the dashboard")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeRepository      employeRepository;
    private final CongeRepository        congeRepository;
    private final RecrutementRepository  recrutementRepository;
    private final FormationRepository    formationRepository;

    @Operation(summary = "Get dashboard KPI statistics")
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> stats() {
        return Map.of(
            "totalEmployes",        employeRepository.count(),
            "congesEnAttente",      congeRepository.countByStatut(CongeStatut.PENDING),
            "recrutementOuverts",   recrutementRepository.countByStatut("OUVERT"),
            "formationsPlanifiees", formationRepository.count()
        );
    }
}
