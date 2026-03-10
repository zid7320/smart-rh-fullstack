package com.smart.rh.repository;

import com.smart.rh.entity.Recrutement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecrutementRepository extends JpaRepository<Recrutement, Long> {

    Page<Recrutement> findAll(Pageable pageable);

    Page<Recrutement> findByStatut(String statut, Pageable pageable);

    long countByStatut(String statut);
}
