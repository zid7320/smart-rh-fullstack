package com.example.hrms.service;

import com.example.hrms.model.Poste;
import com.example.hrms.repository.PosteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PosteService {
    private final PosteRepository repo;

    public PosteService(PosteRepository repo) { this.repo = repo; }

    public Poste create(Poste p) { return repo.save(p); }
    public List<Poste> list() { return repo.findAll(); }
    public Optional<Poste> get(Integer id) { return repo.findById(id); }
    public Poste update(Poste p) { return repo.save(p); }
    public void delete(Integer id) { repo.deleteById(id); }
}
