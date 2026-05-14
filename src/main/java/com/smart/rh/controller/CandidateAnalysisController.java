package com.smart.rh.controller;

import com.smart.rh.dto.recruitment.CandidateAnalysisDto;
import com.smart.rh.service.CandidateAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate-analysis")
@RequiredArgsConstructor
public class CandidateAnalysisController {

    private final CandidateAnalysisService candidateAnalysisService;

    @GetMapping
    @PreAuthorize("permitAll")
    public ResponseEntity<List<CandidateAnalysisDto>> findAll() {
        List<CandidateAnalysisDto> analyses = candidateAnalysisService.findAll();
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll")
    public ResponseEntity<CandidateAnalysisDto> findById(@PathVariable Long id) {
        return candidateAnalysisService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("permitAll")
    public ResponseEntity<List<CandidateAnalysisDto>> findByJobId(@PathVariable Integer jobId) {
        List<CandidateAnalysisDto> analyses = candidateAnalysisService.findByJobId(jobId);
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<CandidateAnalysisDto>> findByEmail(@PathVariable String email) {
        List<CandidateAnalysisDto> analyses = candidateAnalysisService.findByEmail(email);
        return ResponseEntity.ok(analyses);
    }

    @GetMapping("/job/{jobId}/recommendation/{recommendation}")
    public ResponseEntity<List<CandidateAnalysisDto>> findByJobIdAndRecommendation(
            @PathVariable Integer jobId,
            @PathVariable String recommendation) {
        List<CandidateAnalysisDto> analyses = candidateAnalysisService.findByJobIdAndRecommendation(jobId, recommendation);
        return ResponseEntity.ok(analyses);
    }

    @PostMapping
    public ResponseEntity<CandidateAnalysisDto> create(@RequestBody CandidateAnalysisDto dto) {
        CandidateAnalysisDto created = candidateAnalysisService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateAnalysisDto> update(@PathVariable Long id, @RequestBody CandidateAnalysisDto dto) {
        CandidateAnalysisDto updated = candidateAnalysisService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        candidateAnalysisService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        Long count = candidateAnalysisService.count();
        return ResponseEntity.ok(count);
    }
}
