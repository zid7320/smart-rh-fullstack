package com.example.hrms.service;

import com.example.hrms.model.Candidate;
import com.example.hrms.repository.CandidateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CandidateService {
    private final CandidateRepository repo;

    public CandidateService(CandidateRepository repo) { this.repo = repo; }

    public Candidate create(Candidate c) { return repo.save(c); }
    public List<Candidate> list() { return repo.findAll(); }
    public Optional<Candidate> get(Integer id) { return repo.findById(id); }
    public Candidate update(Candidate c) { return repo.save(c); }
    public void delete(Integer id) { repo.deleteById(id); }
}
