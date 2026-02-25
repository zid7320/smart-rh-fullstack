package com.example.hrms.service;

import com.example.hrms.model.Employe;
import com.example.hrms.repository.EmployeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeService {
    private final EmployeRepository repo;

    public EmployeService(EmployeRepository repo) { this.repo = repo; }

    public Employe create(Employe e) { return repo.save(e); }
    public List<Employe> list() { return repo.findAll(); }
    public Optional<Employe> get(Integer id) { return repo.findById(id); }
    public Employe update(Employe e) { return repo.save(e); }
    public void delete(Integer id) { repo.deleteById(id); }
}
