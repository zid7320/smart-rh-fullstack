package com.example.hrms.service;

import com.example.hrms.model.Recrutement;
import com.example.hrms.repository.RecrutementRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecruitmentService {
    private final RecrutementRepository repo;

    public RecruitmentService(RecrutementRepository repo) {
        this.repo = repo;
    }

    public Recrutement create(Recrutement r) { return repo.save(r); }
    public List<Recrutement> list() { return repo.findAll(); }
    public Optional<Recrutement> get(Integer id) { return repo.findById(id); }
    public Recrutement update(Recrutement r) { return repo.save(r); }
    public void delete(Integer id) { repo.deleteById(id); }
}
