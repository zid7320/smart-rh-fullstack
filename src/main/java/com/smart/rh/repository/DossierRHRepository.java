package com.smart.rh.repository;

import com.smart.rh.entity.DossierRH;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DossierRHRepository extends JpaRepository<DossierRH, Long> {

    Optional<DossierRH> findByEmployeId(Long employeId);

    boolean existsByEmployeId(Long employeId);

    Page<DossierRH> findAll(Pageable pageable);
}
