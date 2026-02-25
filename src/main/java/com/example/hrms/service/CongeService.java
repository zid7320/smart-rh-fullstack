package com.example.hrms.service;

import com.example.hrms.model.Conge;
import com.example.hrms.repository.CongeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CongeService {
    private final CongeRepository repo;

    public CongeService(CongeRepository repo) { this.repo = repo; }

    public Conge create(Conge c) { return repo.save(c); }
    public List<Conge> list() { return repo.findAll(); }
    public Optional<Conge> get(Integer id) { return repo.findById(id); }
    public Conge update(Conge c) { return repo.save(c); }
    public void delete(Integer id) { repo.deleteById(id); }
}
