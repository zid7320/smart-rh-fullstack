package com.example.hrms.service;

import com.example.hrms.model.Competence;
import com.example.hrms.repository.CompetenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompetenceService {
    private final CompetenceRepository repo;

    public CompetenceService(CompetenceRepository repo) { this.repo = repo; }

    public Competence create(Competence c) { return repo.save(c); }
    public List<Competence> list() { return repo.findAll(); }
    public Optional<Competence> get(Integer id) { return repo.findById(id); }
    public Competence update(Competence c) { return repo.save(c); }
    public void delete(Integer id) { repo.deleteById(id); }
}
