package com.example.hrms.service;

import com.example.hrms.model.ResponsableRH;
import com.example.hrms.repository.ResponsableRHRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResponsableRHService {
    private final ResponsableRHRepository repo;

    public ResponsableRHService(ResponsableRHRepository repo) { this.repo = repo; }

    public ResponsableRH create(ResponsableRH r) { return repo.save(r); }
    public List<ResponsableRH> list() { return repo.findAll(); }
    public Optional<ResponsableRH> get(Integer id) { return repo.findById(id); }
    public ResponsableRH update(ResponsableRH r) { return repo.save(r); }
    public void delete(Integer id) { repo.deleteById(id); }
}
