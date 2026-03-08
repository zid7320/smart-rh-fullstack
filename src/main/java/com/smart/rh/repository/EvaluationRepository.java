package com.smart.rh.repository;

import com.smart.rh.entity.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    Page<Evaluation> findAll(Pageable pageable);

    Page<Evaluation> findByEmployeId(Long employeId, Pageable pageable);
}
