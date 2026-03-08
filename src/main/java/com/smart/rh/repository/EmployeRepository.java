package com.smart.rh.repository;

import com.smart.rh.entity.Employe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {

    boolean existsByEmail(String email);

    Optional<Employe> findByEmail(String email);

    Page<Employe> findAll(Pageable pageable);

    @Query("SELECT e FROM Employe e WHERE " +
           "LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Employe> search(@Param("search") String search, Pageable pageable);

    Page<Employe> findByPosteId(Long posteId, Pageable pageable);

    /** Used by AttendanceSecurity to resolve the logged-in user's Employe record. */
    Optional<Employe> findByUser_Id(Long userId);
}
