package com.example.hrms.service;

import com.example.hrms.model.Evaluation;
import com.example.hrms.repository.EvaluationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EvaluationService {
    private final EvaluationRepository repo;

    public EvaluationService(EvaluationRepository repo) { this.repo = repo; }

    public Evaluation create(Evaluation e) { return repo.save(e); }
    public List<Evaluation> list() { return repo.findAll(); }
    public Optional<Evaluation> get(Integer id) { return repo.findById(id); }
    public Evaluation update(Evaluation e) { return repo.save(e); }
    public void delete(Integer id) { repo.deleteById(id); }
}
