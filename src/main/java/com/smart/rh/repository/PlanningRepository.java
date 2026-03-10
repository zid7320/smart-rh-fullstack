package com.smart.rh.repository;

import com.smart.rh.entity.Planning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanningRepository extends JpaRepository<Planning, Long> {

    Page<Planning> findByEmployeId(Long employeId, Pageable pageable);
}
