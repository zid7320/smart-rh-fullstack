package com.smart.rh.repository;

import com.smart.rh.entity.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findByRecrutementId(Long recrutementId);

    Page<Candidate> findByRecrutementId(Long recrutementId, Pageable pageable);
}
