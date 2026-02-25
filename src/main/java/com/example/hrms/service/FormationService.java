package com.example.hrms.service;

import com.example.hrms.model.Formation;
import com.example.hrms.repository.FormationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FormationService {
    private final FormationRepository repo;

    public FormationService(FormationRepository repo) { this.repo = repo; }

    public Formation create(Formation f) { return repo.save(f); }
    public List<Formation> list() { return repo.findAll(); }
    public Optional<Formation> get(Integer id) { return repo.findById(id); }
    public Formation update(Formation f) { return repo.save(f); }
    public void delete(Integer id) { repo.deleteById(id); }
}
