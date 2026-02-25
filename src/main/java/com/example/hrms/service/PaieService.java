package com.example.hrms.service;

import com.example.hrms.model.Paie;
import com.example.hrms.repository.PaieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaieService {
    private final PaieRepository repo;

    public PaieService(PaieRepository repo) { this.repo = repo; }

    public Paie create(Paie p) { return repo.save(p); }
    public List<Paie> list() { return repo.findAll(); }
    public Optional<Paie> get(Integer id) { return repo.findById(id); }
    public Paie update(Paie p) { return repo.save(p); }
    public void delete(Integer id) { repo.deleteById(id); }
}
