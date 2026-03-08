package com.smart.rh.repository;

import com.smart.rh.entity.Formation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormationRepository extends JpaRepository<Formation, Long> {

    Page<Formation> findAll(Pageable pageable);

    Page<Formation> findByEmployeId(Long employeId, Pageable pageable);
}
