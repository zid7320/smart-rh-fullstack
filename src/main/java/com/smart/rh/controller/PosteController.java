package com.smart.rh.controller;

import com.smart.rh.dto.poste.PosteDto;
import com.smart.rh.dto.poste.PosteRequest;
import com.smart.rh.service.PosteService;
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

import java.util.List;

@Tag(name = "Posts (Postes)", description = "Job position management")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PosteController {

    private final PosteService service;

    @Operation(summary = "List all posts (paginated)")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<PosteDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @Operation(summary = "Get post by ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PosteDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Create a new post")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public ResponseEntity<PosteDto> create(@Valid @RequestBody PosteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Update a post")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public PosteDto update(@PathVariable Long id, @Valid @RequestBody PosteRequest request) {
        return service.update(id, request);
    }

    @Operation(summary = "Delete a post")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @Operation(summary = "Attach competences to a post (replaces current set)")
    @PutMapping("/{id}/competences")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public PosteDto attachCompetences(@PathVariable Long id, @RequestBody List<Long> competenceIds) {
        return service.attachCompetences(id, competenceIds);
    }

    @Operation(summary = "Detach a competence from a post")
    @DeleteMapping("/{id}/competences/{competenceId}")
    @PreAuthorize("hasAnyRole('ADMIN','RH')")
    public PosteDto detachCompetence(@PathVariable Long id, @PathVariable Long competenceId) {
        return service.detachCompetence(id, competenceId);
    }
}
