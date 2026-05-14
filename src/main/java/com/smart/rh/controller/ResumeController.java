package com.smart.rh.controller;

import com.smart.rh.dto.recruitment.ResumeDto;
import com.smart.rh.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    @PreAuthorize("permitAll")
    public ResponseEntity<List<ResumeDto>> findAll() {
        List<ResumeDto> resumes = resumeService.findAll();
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll")
    public ResponseEntity<ResumeDto> findById(@PathVariable Long id) {
        return resumeService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("permitAll")
    public ResponseEntity<List<ResumeDto>> findByEmail(@PathVariable String email) {
        List<ResumeDto> resumes = resumeService.findByEmail(email);
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/sender-email/{senderEmail}")
    public ResponseEntity<List<ResumeDto>> findBySenderEmail(@PathVariable String senderEmail) {
        List<ResumeDto> resumes = resumeService.findBySenderEmail(senderEmail);
        return ResponseEntity.ok(resumes);
    }

    @PostMapping
    public ResponseEntity<ResumeDto> create(@RequestBody ResumeDto dto) {
        ResumeDto created = resumeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDto> update(@PathVariable Long id, @RequestBody ResumeDto dto) {
        ResumeDto updated = resumeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        Long count = resumeService.count();
        return ResponseEntity.ok(count);
    }
}
