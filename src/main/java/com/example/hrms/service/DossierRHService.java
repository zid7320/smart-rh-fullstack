package com.example.hrms.service;

import com.example.hrms.model.DossierRH;
import com.example.hrms.repository.DossierRHRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DossierRHService {
    private final DossierRHRepository repo;

    public DossierRHService(DossierRHRepository repo) { this.repo = repo; }

    public DossierRH create(DossierRH d) { return repo.save(d); }
    public List<DossierRH> list() { return repo.findAll(); }
    public Optional<DossierRH> get(Integer id) { return repo.findById(id); }
    public DossierRH update(DossierRH d) { return repo.save(d); }
    public void delete(Integer id) { repo.deleteById(id); }
}
