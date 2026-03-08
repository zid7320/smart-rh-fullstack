package com.smart.rh.repository;

import com.smart.rh.entity.Paie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaieRepository extends JpaRepository<Paie, Long> {

    Page<Paie> findAll(Pageable pageable);

    Page<Paie> findByEmployeId(Long employeId, Pageable pageable);

    List<Paie> findByMoisAndAnnee(Integer mois, Integer annee);

    Optional<Paie> findByEmployeIdAndMoisAndAnnee(Long employeId, Integer mois, Integer annee);

    boolean existsByEmployeIdAndMoisAndAnnee(Long employeId, Integer mois, Integer annee);
}
