package com.smart.rh.repository;

import com.smart.rh.entity.Contrat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratRepository extends JpaRepository<Contrat, Long> {

    List<Contrat> findByEmployeId(Long employeId);

    Page<Contrat> findAll(Pageable pageable);

    Page<Contrat> findByEmployeId(Long employeId, Pageable pageable);
}
