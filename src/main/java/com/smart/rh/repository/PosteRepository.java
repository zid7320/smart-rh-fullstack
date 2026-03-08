package com.smart.rh.repository;

import com.smart.rh.entity.Poste;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PosteRepository extends JpaRepository<Poste, Long> {

    boolean existsByTitre(String titre);

    Optional<Poste> findByTitre(String titre);

    Page<Poste> findAll(Pageable pageable);
}
