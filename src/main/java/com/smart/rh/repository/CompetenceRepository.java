package com.smart.rh.repository;

import com.smart.rh.entity.Competence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompetenceRepository extends JpaRepository<Competence, Long> {

    boolean existsByNom(String nom);

    Page<Competence> findAll(Pageable pageable);
}
