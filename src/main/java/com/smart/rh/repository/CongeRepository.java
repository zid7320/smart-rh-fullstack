package com.smart.rh.repository;

import com.smart.rh.entity.Conge;
import com.smart.rh.entity.CongeStatut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CongeRepository extends JpaRepository<Conge, Long> {

    Page<Conge> findAll(Pageable pageable);

    Page<Conge> findByEmployeId(Long employeId, Pageable pageable);

    Page<Conge> findByStatut(CongeStatut statut, Pageable pageable);
}
