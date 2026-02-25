package com.example.hrms.service;

import com.example.hrms.model.Planning;
import com.example.hrms.repository.PlanningRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanningService {
    private final PlanningRepository repo;

    public PlanningService(PlanningRepository repo) { this.repo = repo; }

    public Planning create(Planning p) { return repo.save(p); }
    public List<Planning> list() { return repo.findAll(); }
    public Optional<Planning> get(Integer id) { return repo.findById(id); }
    public Planning update(Planning p) { return repo.save(p); }
    public void delete(Integer id) { repo.deleteById(id); }
}
