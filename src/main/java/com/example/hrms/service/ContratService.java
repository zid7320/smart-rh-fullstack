package com.example.hrms.service;

import com.example.hrms.model.Contrat;
import com.example.hrms.repository.ContratRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContratService {
    private final ContratRepository repo;

    public ContratService(ContratRepository repo) { this.repo = repo; }

    public Contrat create(Contrat c) { return repo.save(c); }
    public List<Contrat> list() { return repo.findAll(); }
    public Optional<Contrat> get(Integer id) { return repo.findById(id); }
    public Contrat update(Contrat c) { return repo.save(c); }
    public void delete(Integer id) { repo.deleteById(id); }
}
